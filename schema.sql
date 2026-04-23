-- ============================================
-- Expense Tracker Database Schema (SQLite)
-- ============================================

-- Users table: stores registered user accounts
CREATE TABLE IF NOT EXISTS Users (
    user_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    username    TEXT    NOT NULL UNIQUE,
    password    TEXT    NOT NULL,               -- SHA-256 hashed
    monthly_budget REAL DEFAULT 0.0             -- monthly budget limit
);

-- Expenses table: stores individual expense records
CREATE TABLE IF NOT EXISTS Expenses (
    expense_id  INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    amount      REAL    NOT NULL,
    category    TEXT    NOT NULL,
    date        TEXT    NOT NULL,               -- ISO 8601 format: YYYY-MM-DD
    description TEXT    DEFAULT '',
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
        ON DELETE CASCADE
);

-- Index for faster queries by user and date
CREATE INDEX IF NOT EXISTS idx_expenses_user   ON Expenses(user_id);
CREATE INDEX IF NOT EXISTS idx_expenses_date   ON Expenses(date);
CREATE INDEX IF NOT EXISTS idx_expenses_category ON Expenses(user_id, category);
