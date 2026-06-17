<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\Wallet;
use App\Models\Transaction;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;

class TransactionController extends Controller
{
    /**
     * Retrieve transaction history.
     */
    public function history(Request $request)
    {
        $user = $request->user();
        $walletIds = $user->wallets->pluck('id');

        $transactions = Transaction::whereIn('sender_wallet_id', $walletIds)
            ->orWhereIn('receiver_wallet_id', $walletIds)
            ->with(['senderWallet.user', 'receiverWallet.user'])
            ->orderBy('created_at', 'desc')
            ->paginate(15);

        return response()->json([
            'success' => true,
            'transactions' => $transactions
        ], 200);
    }

    /**
     * Fetch a specific transaction invoice.
     */
    public function show(Request $request, $id)
    {
        $user = $request->user();
        $walletIds = $user->wallets->pluck('id');

        $transaction = Transaction::where('id', $id)
            ->where(function ($query) use ($walletIds) {
                $query->whereIn('sender_wallet_id', $walletIds)
                    ->orWhereIn('receiver_wallet_id', $walletIds);
            })
            ->with(['senderWallet.user', 'receiverWallet.user'])
            ->first();

        if (!$transaction) {
            return response()->json([
                'success' => false,
                'message' => 'Transaction not found or unauthorized access to record.'
            ], 404);
        }

        return response()->json([
            'success' => true,
            'transaction' => $transaction
        ], 200);
    }

    /**
     * Simulate depositing money (Cash In) via Credit Cards/Bank gateways into the user wallet.
     */
    public function addMoney(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'amount' => 'required|numeric|min:10', // Min deposit 10 BDT
            'currency' => 'required|in:BDT,USD',
            'card_brand' => 'required|string', // E.g. Visa, Mastercard, DBBL Nexus
            'reference' => 'nullable|string',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'errors' => $validator->errors()
            ], 422);
        }

        $user = $request->user();
        $amount = floatval($request->amount);
        $currency = $request->currency;

        return DB::transaction(function () use ($user, $amount, $currency, $request) {
            $wallet = $user->wallets()->where('currency', $currency)->lockForUpdate()->first();

            if (!$wallet) {
                return response()->json([
                    'success' => false,
                    'message' => 'Wallet of requested currency does not exist for user.'
                ], 400);
            }

            if ($wallet->status !== 'active') {
                return response()->json([
                    'success' => false,
                    'message' => 'Wallet ledger is frozen.'
                ], 403);
            }

            // Execute funding adjustment
            $wallet->balance += $amount;
            $wallet->save();

            // Store Audit entry
            $txn = Transaction::create([
                'txn_hash' => 'ADDM-' . strtoupper(Str::random(12)),
                'sender_wallet_id' => null, // Sourced from external card gateway
                'receiver_wallet_id' => $wallet->id,
                'amount' => $amount,
                'fee' => 0.00, // Zero fee card-in promotions
                'currency' => $currency,
                'type' => 'cash_in',
                'status' => 'approved',
                'description' => "Added money via external " . $request->card_brand,
                'reference' => $request->reference ?? 'CC_GATEWAY_FUND'
            ]);

            return response()->json([
                'success' => true,
                'message' => "Successfully loaded $amount $currency into your account.",
                'transaction' => $txn,
                'wallet' => $wallet
            ], 200);
        });
    }

    /**
     * Peer-to-Peer (P2P) Secure Transfer to another register mobile wallet.
     */
    public function transfer(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'receiver_phone' => 'required|string',
            'amount' => 'required|numeric|min:5', // Min transfer BDT 5
            'currency' => 'required|in:BDT,USD',
            'description' => 'nullable|string|max:255'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'errors' => $validator->errors()
            ], 422);
        }

        $user = $request->user();
        $receiverPhone = $request->receiver_phone;
        $amount = floatval($request->amount);
        $currency = $request->currency;

        if ($user->phone === $receiverPhone) {
            return response()->json([
                'success' => false,
                'message' => 'You cannot transfer money to your own phone number.'
            ], 400);
        }

        // Locate receiver profile
        $receiver = User::where('phone', $receiverPhone)->first();
        if (!$receiver) {
            return response()->json([
                'success' => false,
                'message' => 'Recipient phone number is not registered on Click \'n Go.'
            ], 404);
        }

        return DB::transaction(function () use ($user, $receiver, $amount, $currency, $request) {
            $senderWallet = $user->wallets()->where('currency', $currency)->lockForUpdate()->first();
            $receiverWallet = $receiver->wallets()->where('currency', $currency)->lockForUpdate()->first();

            if (!$senderWallet || !$receiverWallet) {
                return response()->json([
                    'success' => false,
                    'message' => 'Requested transfer currency mismatch on ledger levels.'
                ], 400);
            }

            if ($senderWallet->status !== 'active' || $receiverWallet->status !== 'active') {
                return response()->json([
                    'success' => false,
                    'message' => 'One of the participant wallets is suspended or frozen.'
                ], 403);
            }

            // Calculate dynamic transfer fee (flat 5 BDT / 0.1 USD or capped 1%)
            $fee = $currency === 'BDT' ? 5.00 : 0.05;
            $totalDebitRequired = $amount + $fee;

            if ($senderWallet->balance < $totalDebitRequired) {
                return response()->json([
                    'success' => false,
                    'message' => "Insufficient balance. Total charge requires $totalDebitRequired $currency (including fee $fee $currency)."
                ], 400);
            }

            // Double Entry balance posting
            $senderWallet->balance -= $totalDebitRequired;
            $senderWallet->save();

            $receiverWallet->balance += $amount;
            $receiverWallet->save();

            // Set transaction status (simulated limit review flagging if value > 25,000 BDT)
            $antiFraudLimit = $currency === 'BDT' ? 25000.00 : 500.00;
            $status = ($amount > $antiFraudLimit) ? 'pending' : 'approved';
            $descPrefix = ($status === 'pending') ? '[Under Review] ' : '';

            $txn = Transaction::create([
                'txn_hash' => 'P2P-' . strtoupper(Str::random(12)),
                'sender_wallet_id' => $senderWallet->id,
                'receiver_wallet_id' => $receiverWallet->id,
                'amount' => $amount,
                'fee' => $fee,
                'currency' => $currency,
                'type' => 'transfer',
                'status' => $status,
                'description' => $descPrefix . ($request->description ?? "P2P transfer to " . $receiver->name),
                'reference' => 'P2P_CONTACT'
            ]);

            return response()->json([
                'success' => true,
                'message' => $status === 'pending'
                    ? 'Transfer exceeds daily limit. Suspended in security desk pending review.'
                    : "Successfully sent $amount $currency to " . $receiver->name,
                'transaction' => $txn,
                'wallet' => $senderWallet
            ], 200);
        });
    }

    /**
     * Customer Merchant Payment (C2B QR Code/Cashier checkouts).
     */
    public function merchantPayment(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'merchant_wallet_number' => 'required|string',
            'amount' => 'required|numeric|min:1',
            'currency' => 'required|in:BDT,USD',
            'invoice_id' => 'nullable|string'
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'errors' => $validator->errors()
            ], 422);
        }

        $user = $request->user();
        $merchantWalletNum = $request->merchant_wallet_number;
        $amount = floatval($request->amount);
        $currency = $request->currency;

        $merchantWallet = Wallet::where('wallet_number', $merchantWalletNum)->first();
        if (!$merchantWallet || $merchantWallet->user->role !== 'merchant') {
            return response()->json([
                'success' => false,
                'message' => 'Specified wallet number is not registered under a valid Merchant business profile.'
            ], 404);
        }

        return DB::transaction(function () use ($user, $merchantWallet, $amount, $currency, $request) {
            $customerWallet = $user->wallets()->where('currency', $currency)->lockForUpdate()->first();
            $merchantWallet = Wallet::where('id', $merchantWallet->id)->lockForUpdate()->first();

            if (!$customerWallet) {
                return response()->json([
                    'success' => false,
                    'message' => 'Requested currency wallet not found in your account.'
                ], 400);
            }

            if ($customerWallet->balance < $amount) {
                return response()->json([
                    'success' => false,
                    'message' => 'Insufficient funds to complete payment.'
                ], 400);
            }

            // Zero transactional charge to clients on Merchant payments
            $customerWallet->balance -= $amount;
            $customerWallet->loyalty_points += intval($amount / 100); // Earn 1 client loyalty point per 100 BDT spent
            $customerWallet->save();

            $merchantWallet->balance += $amount;
            $merchantWallet->save();

            $txn = Transaction::create([
                'txn_hash' => 'MERCH-' . strtoupper(Str::random(12)),
                'sender_wallet_id' => $customerWallet->id,
                'receiver_wallet_id' => $merchantWallet->id,
                'amount' => $amount,
                'fee' => 0.00,
                'currency' => $currency,
                'type' => 'merchant_payment',
                'status' => 'approved',
                'description' => "Purchased goods at " . $merchantWallet->user->name,
                'reference' => $request->invoice_id ?? 'QR_SCANNER'
            ]);

            return response()->json([
                'success' => true,
                'message' => "Payment of $amount $currency to " . $merchantWallet->user->name . " completed successfully.",
                'transaction' => $txn,
                'wallet' => $customerWallet
            ], 200);
        });
    }

    /**
     * Pay utility bills or purchase mobile airtime credit packages.
     */
    public function payUtilityBill(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'operator' => 'required|string|max:100', // e.g., DESCO, WASA, Bakhrabad Gas, Rural Electrification Board
            'account_no' => 'required|string|max:50',
            'amount' => 'required|numeric|min:50',
            'currency' => 'required|in:BDT,USD',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'errors' => $validator->errors()
            ], 422);
        }

        $user = $request->user();
        $amount = floatval($request->amount);
        $currency = $request->currency;

        return DB::transaction(function () use ($user, $amount, $currency, $request) {
            $wallet = $user->wallets()->where('currency', $currency)->lockForUpdate()->first();

            if (!$wallet || $wallet->balance < $amount) {
                return response()->json([
                    'success' => false,
                    'message' => 'Insufficient balance or currency wallet unconfigured.'
                ], 400);
            }

            // Subtract balance and reward loyalty credit points
            $wallet->balance -= $amount;
            $wallet->loyalty_points += 5; // Flat 5 loyalty reward points for bills utilities
            $wallet->save();

            $txn = Transaction::create([
                'txn_hash' => 'BILL-' . strtoupper(Str::random(12)),
                'sender_wallet_id' => $wallet->id,
                'receiver_wallet_id' => null, // Deducted in favor of a utility corporation desk
                'amount' => $amount,
                'fee' => 0.00,
                'currency' => $currency,
                'type' => 'bill_payment',
                'status' => 'approved',
                'description' => "Settled utility invoice with " . $request->operator . " Account: " . $request->account_no,
                'reference' => $request->account_no
            ]);

            return response()->json([
                'success' => true,
                'message' => "Bill of $amount $currency payable to " . $request->operator . " completed successfully.",
                'transaction' => $txn,
                'wallet' => $wallet
            ], 200);
        });
    }
}
