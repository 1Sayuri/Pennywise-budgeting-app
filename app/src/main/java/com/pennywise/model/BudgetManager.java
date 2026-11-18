package com.pennywise.model;

// BUDGET MANAGER
// A brain class that handles all the elements in the program and their interractions

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BudgetManager {


    // A list for each element of the program
    private List<BudgetCategory> categories = new ArrayList<>();
    private List<Transaction> transactions = new ArrayList<>();
    private List<WishlistItem> wishlist = new ArrayList<>();


    // Adding a new category
    public void addCategory(String name) {
        categories.add(new BudgetCategory(name));
    }

    public void addWishlistItem(String name, double price, String emoji, String link) {
        wishlist.add(new WishlistItem(name, price, emoji, link));
    }

    // Finding a category
    public BudgetCategory getCategory(String name) {
        for (BudgetCategory c : categories) {
            if (c.getName().equalsIgnoreCase(name)) {
                return c;
            }
        }
        return null;
    }


    // ADDING OR EXTRACTING MONEY FROM A CATEGORY, RECORDING THE TRANSACTION
    public void addIncome(String description, double amount, String categoryName) {
        BudgetCategory category = getCategory(categoryName);
        if (category == null) {
            throw new IllegalArgumentException("Category not found: " + categoryName);
        }

        category.addMoney(amount);
        transactions.add(new Transaction(description, amount, "income", categoryName));
    }

    public void addExpense(String description, double amount, String categoryName) {
        BudgetCategory category = getCategory(categoryName);
        if (category == null) {
            throw new IllegalArgumentException("Category not found: " + categoryName);
        }

        category.spendMoney(amount);
        transactions.add(new Transaction(description, amount, "expense", categoryName));
    }

    // Splitting the income into the categories
    public void allocateIncome(String description, double amount, Map<String, Double> percentages) {
        double total = 0;
        for (double p : percentages.values()) {
            total += p;
        }
        if (Math.abs(total - 100) > 0.01) {
            throw new IllegalArgumentException("Percentages must add up to 100.");
        }

        for (Map.Entry<String, Double> entry : percentages.entrySet()) {
            String categoryName = entry.getKey();
            double share = amount * entry.getValue() / 100;
            addIncome(description, share, categoryName);
        }
    }

    public void saveForItem(String itemName, double amount, String fromCategory) {
        WishlistItem item = getWishlistItem(itemName);
        if (item == null) {
            throw new IllegalArgumentException("Wishlist item not found: " + itemName);
        }
        if (amount > item.getPrice() - item.getSaved()) {
            throw new IllegalArgumentException("That's more than the item needs.");
        }

        addExpense("Saved for " + itemName, amount, fromCategory);
        item.addSavings(amount);
    }


    // ============ GETTER AND SETTERS =====================
    public double getTotalBalance() {
        double total = 0;
        for (BudgetCategory c : categories) {
            total += c.getBalance();
        }
        return total;
    }

    public double getTotalIncome() {
        double total = 0;
        for (Transaction t : transactions) {
            if (t.getType().equals("income")) {
                total += t.getAmount();
            }
        }
        return total;
    }

    public WishlistItem getWishlistItem(String name) {
        for (WishlistItem item : wishlist) {
            if (item.getName().equalsIgnoreCase(name)) {
                return item;
            }
        }
        return null;
    }

    public double getTotalExpenses() {
        double total = 0;
        for (Transaction t : transactions) {
            if (t.getType().equals("expense")) {
                total += t.getAmount();
            }
        }
        return total;
    }

    public List<BudgetCategory> getCategories() {
        return categories;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }


    public List<WishlistItem> getWishlist() {
        return wishlist;
    }


}
