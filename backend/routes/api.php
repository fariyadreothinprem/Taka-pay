<?php

use App\Http\Controllers\Api\AuthController;
use App\Http\Controllers\Api\WalletController;
use App\Http\Controllers\Api\TransactionController;
use Illuminate\Support\Facades\Route;

/*
|--------------------------------------------------------------------------
| API Routes
|--------------------------------------------------------------------------
|
| Here is where you can register API routes for your application. These
| routes are loaded by the RouteServiceProvider and all of them will
| be assigned to the "api" middleware group. Make something great!
|
*/

// Public Authentication Routes
Route::post('/register', [AuthController::class, 'register']);
Route::post('/login', [AuthController::class, 'login']);

// Protected Core Banking Routes (Requires Sanctum API Token)
Route::middleware('auth:sanctum')->group(function () {
    
    // Auth & Profile actions
    Route::get('/user/profile', [AuthController::class, 'profile']);
    Route::post('/user/ekyc', [AuthController::class, 'submitEkyc']);
    Route::post('/logout', [AuthController::class, 'logout']);

    // Wallet Ledger Endpoints
    Route::get('/wallets', [WalletController::class, 'index']); // Retrieve BDT & USD balances
    Route::get('/wallets/{id}', [WalletController::class, 'show']); // Detailed wallet overview
    Route::post('/wallets/convert', [WalletController::class, 'convertCurrency']); // Multi-currency conversion (BDT <-> USD)

    // Core Banking / Transaction Flow Endpoints
    Route::get('/transactions', [TransactionController::class, 'history']); // Ledger history
    Route::post('/transactions/add-money', [TransactionController::class, 'addMoney']); // Debit/Credit card or Bank gateway simulation
    Route::post('/transactions/transfer', [TransactionController::class, 'transfer']); // P2P standard fund transfer
    Route::post('/transactions/merchant-payment', [TransactionController::class, 'merchantPayment']); // QR/Tap checkout
    Route::post('/transactions/bill-payment', [TransactionController::class, 'payUtilityBill']); // Utility, internet, mobile recharge
    Route::post('/transactions/{id}', [TransactionController::class, 'show']); // Detailed transaction receipt details
});
