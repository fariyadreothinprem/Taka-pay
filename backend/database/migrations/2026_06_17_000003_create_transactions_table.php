<?php

use Illuminate\Database\Migrations\Migration;
use Illuminate\Database\Schema\Blueprint;
use Illuminate\Support\Facades\Schema;

return new class extends Migration
{
    /**
     * Run the migrations.
     */
    public function up(): void
    {
        Schema::create('transactions', function (Blueprint $table) {
            $table->id();
            $table->string('txn_hash')->unique(); // Unique human-readable financial reference like TXN-XMPT-1901
            $table->foreignId('sender_wallet_id')->nullable()->constrained('wallets')->onDelete('set null'); // Source wallet or cash in source
            $table->foreignId('receiver_wallet_id')->nullable()->constrained('wallets')->onDelete('set null'); // Destination wallet
            $table->decimal('amount', 15, 2); // Transaction amount
            $table->decimal('fee', 15, 2)->default(0.00); // Charged processing or transfer fee
            $table->enum('currency', ['BDT', 'USD'])->default('BDT');
            $table->enum('type', [
                'cash_in',
                'transfer', 
                'cash_out',
                'merchant_payment', 
                'bill_payment', 
                'conversion', 
                'card_settlement'
            ]);
            $table->enum('status', ['pending', 'approved', 'rejected', 'failed', 'flagged_fraud'])->default('pending');
            $table->string('description')->nullable();
            $table->string('reference')->nullable(); // External invoice/account identifier
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('transactions');
    }
};
