package com.pennywise.model;

public class Transaction {

    private String description;
    private long amountCents;
    private String type;
    private String category;
    private long date;

    public Transaction(String description, long amountCents,
                       String type, String category, long date) {

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }
        if (amountCents <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        if (type == null ||
                (!type.equalsIgnoreCase("income") && !type.equalsIgnoreCase("expense"))) {
            throw new IllegalArgumentException("Type must be income or expense.");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Category cannot be empty.");
        }

        this.description = description;
        this.amountCents = amountCents;
        this.type = type.toLowerCase();
        this.category = category;
        this.date = date;
    }

    public String getDescription() { return description; }
    public long getAmountCents() { return amountCents; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public long getDate() { return date; }

    @Override
    public String toString() {
        return "Transaction{" +
                "description='" + description + '\'' +
                ", amountCents=" + amountCents +
                ", type='" + type + '\'' +
                ", category='" + category + '\'' +
                ", date=" + date +
                '}';
    }
}