package com.expensetracker.model;

/**
 * Model class representing an expense split between two users.
 */
public class Split {
    private int splitId;
    private int payerId;       // The user who created the split (who is owed money)
    private String payerName;
    private int payeeId;       // The user who owes the money
    private String payeeName;
    private double amount;
    private String description;
    private String date;
    private String status;     // 'PENDING' or 'PAID'

    public Split() {}

    public Split(int payerId, int payeeId, double amount, String description, String date, String status) {
        this.payerId = payerId;
        this.payeeId = payeeId;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.status = status;
    }

    public Split(int splitId, int payerId, String payerName, int payeeId, String payeeName, double amount, String description, String date, String status) {
        this.splitId = splitId;
        this.payerId = payerId;
        this.payerName = payerName;
        this.payeeId = payeeId;
        this.payeeName = payeeName;
        this.amount = amount;
        this.description = description;
        this.date = date;
        this.status = status;
    }

    // Getters and Setters
    public int getSplitId() { return splitId; }
    public void setSplitId(int splitId) { this.splitId = splitId; }

    public int getPayerId() { return payerId; }
    public void setPayerId(int payerId) { this.payerId = payerId; }

    public String getPayerName() { return payerName; }
    public void setPayerName(String payerName) { this.payerName = payerName; }

    public int getPayeeId() { return payeeId; }
    public void setPayeeId(int payeeId) { this.payeeId = payeeId; }

    public String getPayeeName() { return payeeName; }
    public void setPayeeName(String payeeName) { this.payeeName = payeeName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
