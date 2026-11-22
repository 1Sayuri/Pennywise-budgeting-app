package com.pennywise;

import static org.junit.Assert.assertEquals;

import com.pennywise.model.*;
import org.junit.Test;
import java.util.LinkedHashMap;
import java.util.Map;

public class ExampleUnitTest {

    @Test
    public void testPennyWise() {
        BudgetManager manager = new BudgetManager();

        manager.addCategory("Food");
        manager.addCategory("Outings");
        manager.addCategory("Savings");

        // Split $1,000 income
        Map<String, Double> split = new LinkedHashMap<>();
        split.put("Food", 30.0);
        split.put("Outings", 20.0);
        split.put("Savings", 50.0);
        manager.allocateIncome("Paycheck", Money.parse("1000"), split);

        assertEquals(30000, manager.getCategory("Food").getBalanceCents());
        assertEquals(20000, manager.getCategory("Outings").getBalanceCents());
        assertEquals(50000, manager.getCategory("Savings").getBalanceCents());

        // Spend on groceries, save for headphones
        manager.addExpense("Groceries", Money.parse("35.50"), "Food");
        manager.addWishlistItem("Headphones", Money.parse("150"), "🎧", "");
        manager.saveForItem("Headphones", Money.parse("50"), "Savings");

        WishlistItem h = manager.getWishlistItem("Headphones");
        assertEquals(5000, h.getSavedCents());
        assertEquals(33, h.getProgressPercent());

        assertEquals(91450, manager.getTotalBalanceCents());
        assertEquals(100000, manager.getTotalIncomeCents());
        assertEquals(8550, manager.getTotalExpensesCents());

        // Print a readable summary
        System.out.println("Balance: " + Money.format(manager.getTotalBalanceCents()));
        System.out.println(h.getName() + ": " + h.getProgressPercent() + "%");
    }

    @Test
    public void testLeftoverCent() {
        BudgetManager manager = new BudgetManager();
        manager.addCategory("A");
        manager.addCategory("B");
        manager.addCategory("C");

        Map<String, Double> split = new LinkedHashMap<>();
        split.put("A", 33.33);
        split.put("B", 33.33);
        split.put("C", 33.34);
        manager.allocateIncome("Test", 1000, split); // $10.00

        assertEquals(1000, manager.getTotalBalanceCents()); // no cent lost
    }

    @Test
    public void testMoneyHelper() {
        assertEquals(3550, Money.parse("35.50"));
        assertEquals(100000, Money.parse("$1,000"));
        assertEquals("$914.50", Money.format(91450));
    }
}