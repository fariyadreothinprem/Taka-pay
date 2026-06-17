<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use App\Models\User;
use App\Models\Wallet;
use Illuminate\Http\Request;
use Illuminate\Support\Facades\Hash;
use Illuminate\Support\Facades\Validator;
use Illuminate\Validation\Rule;

class AuthController extends Controller
{
    /**
     * Register a new user and auto-initialize default BDT/USD wallets.
     */
    public function register(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'name' => 'required|string|max:255',
            'email' => 'required|string|email|max:255|unique:users',
            'phone' => 'required|string|regex:/^01[3-9]\d{8}$/|unique:users', // Bangladesh phone validation helper
            'password' => 'required|string|min:6|confirmed',
            'nid_number' => 'nullable|string|unique:users',
            'role' => ['sometimes', Rule::in(['customer', 'merchant'])],
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Validation error occur',
                'errors' => $validator->errors()
            ], 422);
        }

        // Create the profile
        $user = User::create([
            'name' => $request->name,
            'email' => $request->email,
            'phone' => $request->phone,
            'password' => Hash::make($request->password),
            'nid_number' => $request->nid_number,
            'role' => $request->role ?? 'customer',
            'status' => $request->nid_number ? 'active' : 'pending_ekyc',
        ]);

        // Auto-initialize Wallet Ledgers for dual-currency transactions
        $bdtWallet = Wallet::create([
            'user_id' => $user->id,
            'wallet_number' => $user->phone . '-BDT',
            'currency' => 'BDT',
            'balance' => 50.00, // Welcome signup bonus
            'loyalty_points' => 10
        ]);

        $usdWallet = Wallet::create([
            'user_id' => $user->id,
            'wallet_number' => $user->phone . '-USD',
            'currency' => 'USD',
            'balance' => 0.00,
            'loyalty_points' => 0
        ]);

        // Issue auth token (Sanctum)
        $token = $user->createToken('ClickNGoAuthToken')->plainTextToken;

        return response()->json([
            'success' => true,
            'message' => 'User registered successfully and digital wallets initialized.',
            'token' => $token,
            'user' => $user->load('wallets')
        ], 210);
    }

    /**
     * Authenticate and return Auth Token.
     */
    public function login(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'phone' => 'required|string',
            'password' => 'required|string',
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'message' => 'Phone or password key required',
                'errors' => $validator->errors()
            ], 422);
        }

        $user = User::where('phone', $request->phone)->first();

        if (!$user || !Hash::check($request->password, $user->password)) {
            return response()->json([
                'success' => false,
                'message' => 'Invalid mobile number or pin/password configuration.'
            ], 401);
        }

        if ($user->status === 'suspended') {
            return response()->json([
                'success' => false,
                'message' => 'This account has been flagged and suspended for safety review.'
            ], 403);
        }

        $token = $user->createToken('ClickNGoAuthToken')->plainTextToken;

        return response()->json([
            'success' => true,
            'message' => 'Logged in successfully.',
            'token' => $token,
            'user' => $user->load('wallets')
        ], 200);
    }

    /**
     * Retrieve user profile with multi-wallets.
     */
    public function profile(Request $request)
    {
        return response()->json([
            'success' => true,
            'user' => $request->user()->load('wallets')
        ], 200);
    }

    /**
     * Submit/verify National Identity Card (eKYC) status.
     */
    public function submitEkyc(Request $request)
    {
        $validator = Validator::make($request->all(), [
            'nid_number' => 'required|string|min:10|max:17|unique:users,nid_number,' . $request->user()->id,
        ]);

        if ($validator->fails()) {
            return response()->json([
                'success' => false,
                'errors' => $validator->errors()
            ], 422);
        }

        $user = $request->user();
        $user->nid_number = $request->nid_number;
        $user->status = 'active'; // In a real bank, this calls a government OCR gateway or sets review. Instantly approving for simulation.
        $user->save();

        return response()->json([
            'success' => true,
            'message' => 'NID document verified successfully. Identity state is ACTIVE.',
            'user' => $user
        ], 200);
    }

    /**
     * Revoke current API token (Logout).
     */
    public function logout(Request $request)
    {
        $request->user()->currentAccessToken()->delete();

        return response()->json([
            'success' => true,
            'message' => 'Logged out successfully, token deleted.'
        ], 200);
    }
}
