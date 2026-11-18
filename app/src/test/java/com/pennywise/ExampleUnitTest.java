package com.pennywise;

import com.pennywise.model.*;
import org.junit.Test;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExampleUnitTest {

    @Test
    public void testPennyWise() {
        BudgetManager manager = new BudgetManager();

        // Create categories
        manager.addCategory("Food");
        manager.addCategory("Outings");
        manager.addCategory("Savings");

        // Split $1000 income
        Map<String, Double> split = new LinkedHashMap<>();
        split.put("Food", 30.0);
        split.put("Outings", 20.0);
        split.put("Savings", 50.0);
        manager.allocateIncome("Paycheck", 1000, split);

        System.out.println("--- After income ---");
        for (BudgetCategory c : manager.getCategories()) {
            System.out.println(c);
        }

        // Spend on groceries
        manager.addExpense("Groceries", 35.50, "Food");

        // Wishlist
        manager.addWishlistItem("Headphones", 150, "🎧", "");
        manager.saveForItem("Headphones", 50, "Savings");

        System.out.println("\n--- After spending + saving ---");
        for (BudgetCategory c : manager.getCategories()) {
            System.out.println(c);
        }

        WishlistItem h = manager.getWishlistItem("Headphones");
        System.out.println("\n" + h.getEmoji() + " " + h.getName() + ": $"
                + h.getSaved() + " / $" + h.getPrice()
                + " (" + h.getProgress() + "%)");

        System.out.println("\n--- History ---");
        for (Transaction t : manager.getTransactions()) {
            System.out.println(t);
        }

        System.out.println("\nTotal balance: $" + manager.getTotalBalance());
        System.out.println("Total income: $" + manager.getTotalIncome());
        System.out.println("Total expenses: $" + manager.getTotalExpenses());
    }
}