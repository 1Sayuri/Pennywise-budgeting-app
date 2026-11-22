package com.pennywise.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public final class Money {

    private Money() {} // no objects, just helper methods

    // 91450 -> "$914.50"
    public static String format(long cents) {
        NumberFormat f = NumberFormat.getCurrencyInstance(Locale.US);
        return f.format(cents / 100.0);
    }

    // "35.50" or "$1,000" -> 3550 or 100000
    public static long parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Amount is empty.");
        }
        String clean = text.replace("$", "").replace(",", "").trim();
        if (clean.isEmpty()) {
            throw new IllegalArgumentException("Amount is empty.");
        }
        BigDecimal value = new BigDecimal(clean).setScale(2, RoundingMode.HALF_UP);
        return value.movePointRight(2).longValueExact();
    }
}