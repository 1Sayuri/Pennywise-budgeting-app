package com.pennywise.model;

public class Transaction {

    private String description;
    private double amount;
    private String type;
    private String category;

    public Transaction(String description, double amount,
                       String type, String category) {

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Description cannot be empty."
            );
        }

        if (amount <= 0 || !Double.isFinite(amount)) {
            throw new IllegalArgumentException(
                "Amount must be a positive, finite number."
            );
        }

        if (type == null ||
            (!type.equalsIgnoreCase("income")
             && !type.equalsIgnoreCase("expense"))) {

            throw new IllegalArgumentException(
                "Type must be income or expense."
            );
        }

        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Category cannot be empty."
            );
        }

        this.description = description;
        this.amount = amount;
        this.type = type.toLowerCase();
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "description='" + description + '\'' +
                ", amount=" + amount +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}