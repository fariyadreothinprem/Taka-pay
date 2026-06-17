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
        Schema::create('wallets', function (Blueprint $table) {
            $table->id();
            $table->foreignId('user_id')->constrained('users')->onDelete('cascade');
            $table->string('wallet_number')->unique(); // Unique account numbering structure
            $table->enum('currency', ['BDT', 'USD'])->default('BDT');
            $table->decimal('balance', 15, 2)->default(0.00); // 15 digits total, 2 decimal places
            $table->integer('loyalty_points')->default(0); // Reward system integration
            $table->enum('status', ['active', 'frozen', 'inactive'])->default('active');
            $table->timestamps();
        });
    }

    /**
     * Reverse the migrations.
     */
    public function down(): void
    {
        Schema::dropIfExists('wallets');
    }
};
