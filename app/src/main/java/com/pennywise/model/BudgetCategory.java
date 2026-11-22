package com.pennywise.model;

public class BudgetCategory {

    private String name;
    private long balanceCents;

    public BudgetCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        this.name = name;
        this.balanceCents = 0;
    }

    public String getName() { return name; }
    public long getBalanceCents() { return balanceCents; }

    public void addMoney(long amountCents) {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        balanceCents += amountCents;
    }

    public void spendMoney(long amountCents) {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        if (amountCents > balanceCents) {
            throw new IllegalArgumentException("Insufficient funds in " + name + ".");
        }
        balanceCents -= amountCents;
    }

    @Override
    public String toString() {
        return name + " Balance: " + balanceCents + " cents";
    }
}