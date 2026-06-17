<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\Wallet;
use App\Models\Transaction;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Facades\Validator;
use Illuminate\Support\Str;

class WalletController extends Controller
{
    /**
     * Display a listing of user wallets (Balances, points).
     */
    public function index(Request $request)
    {
        $wallets = $request->user()->wallets;

        return response()->json([
            'success' => true,
            'wallets' => $wallets
        ], 200);
    }

    /**
     * Display the specified wallet with direct transaction logs.
     */
    public function show(Request $request, $id)
    {
        $wallet = Wallet::where('id', $id)
            ->where('user_id', $request->user()->id)
            ->first();

        if (!$wallet) {
            return response()->json([
                'success' => false,
                'message' => 'Wallet not found'
            ], 404);
        }

        // Get merged transaction ledger
        $transactions = $wallet->allTransactions()->paginate(15);

        return response()->json([
            'success' => true,
            'wallet' => $wallet,
            'ledger' => $transactions
        ], 200);
    }

    /**
     * Convert currency from BDT to USD or vice versa within the user's multi-ledger.
     */
    public function convertCurrency(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'from_currency' => 'required|in:BDT,USD',
            'to_currency' => 'required|in:BDT,USD|different:from_currency',
            'amount' => 'required|numeric|min:0.01',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'errors' => $validator->errors()
            ], 422);
        }

        $user = $request->user();
        $fromCurrency = $request->from_currency;
        $toCurrency = $request->to_currency;
        $sellAmount = floatval($request->amount);

        // Constant Exchange rate: 1 USD = 117.50 BDT, buying support at 116.00
        $usdToBdtSalesRate = 117.50;
        $bdtToUsdBuyRate = 118.50; // Bank conversion buffer spreads

        // DB Transaction for atomic state integrity
        return DB::transaction(function () use ($user, $fromCurrency, $toCurrency, $sellAmount, $usdToBdtSalesRate, $bdtToUsdBuyRate) {
            $fromWallet = $user->wallets()->where('currency', $fromCurrency)->lockForUpdate()->first();
            $toWallet = $user->wallets()->where('currency', $toCurrency)->lockForUpdate()->first();

            if (!$fromWallet || !$toWallet) {
                return response()->json([
                    'success' => false,
                    'message' => 'Missing wallet configurations for currency exchange.'
                ], 400);
            }

            if ($fromWallet->status !== 'active' || $toWallet->status !== 'active') {
                return response()->json([
                    'success' => false,
                    'message' => 'One of your wallets is frozen or inactive.'
                ], 403);
            }

            if ($fromWallet->balance < $sellAmount) {
                return response()->json([
                    'success' => false,
                    'message' => 'Insufficient funds in (' . $fromCurrency . ') wallet to convert.'
                ], 400);
            }

            // Calculate converted proceeds
            if ($fromCurrency === 'BDT' && $toCurrency === 'USD') {
                $receiveAmount = round($sellAmount / $bdtToUsdBuyRate, 2);
            } else { // USD to BDT
                $receiveAmount = round($sellAmount * $usdToBdtSalesRate, 2);
            }

            // Execute Double-entry balance adjustments
            $fromWallet->balance -= $sellAmount;
            $fromWallet->save();

            $toWallet->balance += $receiveAmount;
            
            // Accrue loyalty swap rewards status points
            if ($fromCurrency === 'BDT') {
                $toWallet->loyalty_points += intval($sellAmount / 500); // 1 point per 500 BDT converted
            } else {
                $toWallet->loyalty_points += intval($sellAmount * 2); // 2 points per USD converted
            }
            $toWallet->save();

            // Store single financial transaction trace link in DB
            $txn = Transaction::create([
                'txn_hash' => 'CONV-' . strtoupper(Str::random(12)),
                'sender_wallet_id' => $fromWallet->id,
                'receiver_wallet_id' => $toWallet->id,
                'amount' => $sellAmount,
                'fee' => 0.00, // Zero commission exchange program
                'currency' => $fromCurrency,
                'type' => 'conversion',
                'status' => 'approved',
                'description' => "Exchanged $sellAmount $fromCurrency to receive $receiveAmount $toCurrency",
                'reference' => 'INTERNAL_SWAP'
            ]);

            return response()->json([
                'success' => true,
                'message' => "Successfully exchanged $sellAmount $fromCurrency and credited $receiveAmount $toCurrency to your wallet.",
                'transaction' => $txn,
                'wallets' => $user->wallets
            ], 200);
        });
    }
}
