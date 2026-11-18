package com.pennywise.model;

public class BudgetCategory {

    private String name;
    private double balance;

    public BudgetCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Category name cannot be empty."
            );
        }

        this.name = name;
        this.balance = 0.0;
    }

    public String getName() {
        return name;
    }

    public double getBalance() {
        return balance;
    }

    public void addMoney(double amount) {
        if (amount <= 0 || !Double.isFinite(amount)) {
            throw new IllegalArgumentException(
                "Amount must be positive."
            );
        }

        balance += amount;
    }

    public void spendMoney(double amount) {
        if (amount <= 0 || !Double.isFinite(amount)) {
            throw new IllegalArgumentException(
                "Amount must be positive."
            );
        }

        if (amount > balance) {
            throw new IllegalArgumentException(
                "Insufficient funds in " + name + "."
            );
        }

        balance -= amount;
    }

    @Override
    public String toString() {
        return name + " Balance: $" + balance;
    }
}