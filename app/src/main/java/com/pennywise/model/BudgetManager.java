package com.pennywise.model;

// BUDGET MANAGER
// The brain class that handles all the elements in the program and their interactions

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BudgetManager {

    // ===== Singleton: one shared manager for every screen =====
    private static BudgetManager instance;

    public static BudgetManager getInstance() {
        if (instance == null) {
            instance = new BudgetManager();
        }
        return instance;
    }

    // ===== Data =====
    private List<BudgetCategory> categories = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private List<WishlistItem> wishlist = new ArrayList<>();

    // ===== Categories =====
    public void addCategory(String name) {
        categories.add(new BudgetCategory(name));
    }

    public BudgetCategory getCategory(String name) {
        for (BudgetCategory c : categories) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }

    // ===== Income & expenses =====
    public void addIncome(String description, long amountCents, String categoryName) {
        BudgetCategory category = getCategory(categoryName);
        if (category == null) {
            throw new IllegalArgumentException("Category not found: " + categoryName);
        }
        category.addMoney(amountCents);
        transactions.add(new Transaction(description, amountCents, "income",
                categoryName, System.currentTimeMillis()));
    }

    public void addExpense(String description, long amountCents, String categoryName) {
        BudgetCategory category = getCategory(categoryName);
        if (category == null) {
            throw new IllegalArgumentException("Category not found: " + categoryName);
        }
        category.spendMoney(amountCents);
        transactions.add(new Transaction(description, amountCents, "expense",
                categoryName, System.currentTimeMillis()));
    }

    // Split one income across categories by percentage (must add up to 100)
    public void allocateIncome(String description, long amountCents, Map<String, Double> percentages) {
        double totalPercent = 0;
        for (double p : percentages.values()) {
            totalPercent += p;
        }
        if (Math.abs(totalPercent - 100) > 0.01) {
            throw new IllegalArgumentException("Percentages must add up to 100.");
        }

        long allocated = 0;
        int index = 0;
        for (Map.Entry<String, Double> entry : percentages.entrySet()) {
            index++;
            long share;
            if (index == percentages.size()) {
                share = amountCents - allocated; // last one gets the leftover cents
            } else {
                share = Math.round(amountCents * entry.getValue() / 100);
            }
            allocated += share;
            if (share > 0) {
                addIncome(description, share, entry.getKey());
            }
        }
    }

    // ===== Totals =====
    public long getTotalBalanceCents() {
        long total = 0;
        for (BudgetCategory c : categories) {
            total += c.getBalanceCents();
        }
        return total;
    }

    public long getTotalIncomeCents() {
        long total = 0;
        for (Transaction t : transactions) {
            if (t.getType().equals("income")) total += t.getAmountCents();
        }
        return total;
    }

    public long getTotalExpensesCents() {
        long total = 0;
        for (Transaction t : transactions) {
            if (t.getType().equals("expense")) total += t.getAmountCents();
        }
        return total;
    }

    public long getReceivedInCategoryCents(String categoryName) {
        long total = 0;
        for (Transaction t : transactions) {
            if (t.getType().equals("income") && t.getCategory().equalsIgnoreCase(categoryName)) {
                total += t.getAmountCents();
            }
        }
        return total;
    }

    public long getSpentInCategoryCents(String categoryName) {
        long total = 0;
        for (Transaction t : transactions) {
            if (t.getType().equals("expense") && t.getCategory().equalsIgnoreCase(categoryName)) {
                total += t.getAmountCents();
            }
        }
        return total;
    }

    // ===== Wishlist =====
    public void addWishlistItem(String name, long priceCents, String emoji, String link) {
        wishlist.add(new WishlistItem(name, priceCents, emoji, link));
    }

    public WishlistItem getWishlistItem(String name) {
        for (WishlistItem item : wishlist) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    public void saveForItem(String itemName, long amountCents, String fromCategory) {
        WishlistItem item = getWishlistItem(itemName);
        if (item == null) {
            throw new IllegalArgumentException("Wishlist item not found: " + itemName);
        }
        if (amountCents > item.getRemainingCents()) {
            throw new IllegalArgumentException("That's more than the item needs.");
        }
        addExpense("Saved for " + itemName, amountCents, fromCategory);
        item.addSavings(amountCents);
    }

    // ===== Getters =====
    public List<BudgetCategory> getCategories() { return categories; }
    public List<Transaction> getTransactions() { return transactions; }
    public List<WishlistItem> getWishlist() { return wishlist; }
}