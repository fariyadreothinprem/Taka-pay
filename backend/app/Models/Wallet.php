<?php

namespace App\Models;

use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Model;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;

class Wallet extends Model
{
    use HasFactory;

    protected $fillable = [
        'user_id',
        'wallet_number',
        'currency',
        'balance',
        'loyalty_points',
        'status',
    ];

    protected $casts = [
        'balance' => 'decimal:2',
        'loyalty_points' => 'integer',
        'status' => 'string',
    ];

    /**
     * Get the user that owns this wallet.
     */
    public function user(): BelongsTo
    {
        return $this->belongsTo(User::class);
    }

    /**
     * Transactions sent from this wallet.
     */
    public function sentTransactions(): HasMany
    {
        return $this->hasMany(Transaction::class, 'sender_wallet_id');
    }

    /**
     * Transactions received by this wallet.
     */
    public function receivedTransactions(): HasMany
    {
        return $this->hasMany(Transaction::class, 'receiver_wallet_id');
    }

    /**
     * Helper to retrieve all transaction history combined.
     */
    public function allTransactions()
    {
        return Transaction::where('sender_wallet_id', $this->id)
            ->orWhere('receiver_wallet_id', $this->id)
            ->orderBy('created_at', 'desc');
    }
}
