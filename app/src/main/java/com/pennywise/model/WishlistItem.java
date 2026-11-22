package com.pennywise.model;

public class WishlistItem {

    private String name;
    private long priceCents;
    private long savedCents;
    private String emoji;
    private String link;

    public WishlistItem(String name, long priceCents, String emoji, String link) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty.");
        }
        if (priceCents <= 0) {
            throw new IllegalArgumentException("Price must be positive.");
        }
        this.name = name;
        this.priceCents = priceCents;
        this.savedCents = 0;
        this.emoji = (emoji == null || emoji.isEmpty()) ? "🎁" : emoji;
        this.link = link;
    }

    public void addSavings(long amountCents) {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        if (savedCents + amountCents > priceCents) {
            throw new IllegalArgumentException("That's more than the item needs.");
        }
        savedCents += amountCents;
    }

    public String getName() { return name; }
    public long getPriceCents() { return priceCents; }
    public long getSavedCents() { return savedCents; }
    public long getRemainingCents() { return priceCents - savedCents; }
    public String getEmoji() { return emoji; }
    public String getLink() { return link; }

    public int getProgressPercent() {
        return (int) (savedCents * 100 / priceCents);
    }

    public boolean isComplete() {
        return savedCents >= priceCents;
    }
}