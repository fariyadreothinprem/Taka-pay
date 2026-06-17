# Click 'n Go Bangladesh Fintech Laravel API Backend

This is the robust **Laravel API Backend Structure** designed to power the "Click 'n Go" digital wallet, fintech, and banking super app. It implements production-ready database migrations, Eloquent models, controllers, and transaction security mechanics for multi-role accounts (customer, merchant, admin) with support for dual-currency wallets (BDT and USD).

## Core Architecture Design
- **Users**: High-security user roles supporting national ID (NID) registration and eKYC verification flows.
- **Wallets**: Multi-wallet mapping for users allowing balance tracking in multiple currencies (BDT/USD) and dynamic loyalty points tracking.
- **Transactions**: Atomic financial ledger recording cash transfers, merchant payments, bill utilities, virtual cards, and admin approvals with anti-fraud fee calculations.

---

## File Structure Created

```text
/backend
├── app
│   ├── Http
│   │   └── Controllers
│   │       └── Api
│   │           ├── AuthController.php
│   │           ├── WalletController.php
│   │           └── TransactionController.php
│   └── Models
│       ├── User.php
│       ├── Wallet.php
│       └── Transaction.php
├── database
│   └── migrations
│       ├── 2026_06_17_000001_create_users_table.php
│       ├── 2026_06_17_000002_create_wallets_table.php
│       └── 2026_06_17_000003_create_transactions_table.php
├── routes
│   └── api.php
└── README.md
```

## Setup & Running Migrations

1. Ensure you have **PHP 8.2+** and **Composer** installed.
2. Initialize environment variables:
   ```bash
   cp .env.example .env
   ```
3. Set your database credentials inside `.env`:
   ```env
   DB_CONNECTION=mysql
   DB_HOST=127.0.0.1
   DB_PORT=3306
   DB_DATABASE=click_n_go
   DB_USERNAME=root
   DB_PASSWORD=your_password
   ```
4. Install dependencies:
   ```bash
   composer install
   ```
5. Run migrations to provision database tables and foreign key constraints:
   ```bash
   php artisan migrate
   ```
6. Start the Laravel development server:
   ```bash
   php artisan serve
   ```
