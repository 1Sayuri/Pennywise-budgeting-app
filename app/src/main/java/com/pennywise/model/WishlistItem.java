package com.pennywise.model;

//   WISHLIST ITEMS
//  Creating wishlist items and

public class WishlistItem {

    // Wishlist Item Variables
    private String name;
    private double price;
    private double saved;
    private String emoji;
    private String link;


    // CONSTRUCTION
    public WishlistItem(String name, double price, String emoji, String link) {

        // Checking for the valid name
        if ((name == null) || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name can't be empty");
        }

        // Checking for the valid price
        if (price <= 0 || !Double.isFinite(price)) {
            throw new IllegalArgumentException("Price must be positive.");
        }

        // Setting the variables in the construction
        this.name = name;
        this.price = price;
        this.saved = 0.0;
        this.emoji = (emoji == null || emoji.isEmpty()) ? "🎁" : emoji;
        this.link = link;
    }


    // Adding into the wislist item
    public void addSavings(double amount) {
        if (amount <= 0 || !Double.isFinite(amount)) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        if (saved + amount > price) {
            throw new IllegalArgumentException("That's more than the item needs.");
        }
        saved += amount;
    }

    // ========== GETTER AND SETTERS =====================
    public String getName() { return name; }
    public double getPrice() { return price; }
    public double getSaved() { return saved; }
    public String getEmoji() { return emoji; }
    public String getLink() { return link; }


    public double getProgress() {
        return saved / price * 100;
    }

    public boolean isComplete() {
        return saved >= price;
    }

}



