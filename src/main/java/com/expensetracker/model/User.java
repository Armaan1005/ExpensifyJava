package com.expensetracker.model;

/**
 * Model class representing a registered user.
 * Maps to the Users table in the database.
 */
public class User {

    private int userId;
    private String username;
    private String password;       // stored as SHA-256 hash
    private double monthlyBudget;

    // ── Constructors ──────────────────────────────────────────

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(int userId, String username, String password, double monthlyBudget) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.monthlyBudget = monthlyBudget;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public double getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(double monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    @Override
    public String toString() {
        return "User{id=" + userId + ", username='" + username + "'}";
    }
}
