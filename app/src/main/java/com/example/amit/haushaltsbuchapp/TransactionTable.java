package com.example.amit.haushaltsbuchapp;

import java.util.Date;

public class TransactionTable {
    /**
     * This class help to store the transaction information
     */
    private String category, title, payment, type;
    private Date transactionDate;
    private double amount;

    public TransactionTable(){}

    public TransactionTable(String category, String title, Date transactionDate, double amount, String payment, String type ) {
        this.category = category;
        this.title = title;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.payment = payment;
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public String getTitle() {
        return title;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public double getAmount() {
        return amount;
    }

    public String getPayment() { return payment; }

    public String getType() {
        return type;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
