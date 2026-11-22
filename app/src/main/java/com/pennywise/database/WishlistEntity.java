package com.pennywise.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "wishlist_items",
        foreignKeys = @ForeignKey(
                entity = BankAccount.class,
                parentColumns = "accountId",
                childColumns = "accountId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("accountId")}
)
public class WishlistEntity {

    @PrimaryKey(autoGenerate = true)
    private int itemId;

    private int accountId;
    private String name;
    private long priceCents;
    private long savedCents;
    private String emoji;
    private String link;

    public WishlistEntity() {}

    @Ignore
    public WishlistEntity(int accountId, String name, long priceCents, String emoji, String link) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty.");
        }
        if (priceCents <= 0) {
            throw new IllegalArgumentException("Price must be positive.");
        }
        this.accountId = accountId;
        this.name = name.trim();
        this.priceCents = priceCents;
        this.savedCents = 0;
        this.emoji = (emoji == null || emoji.isEmpty()) ? "🎁" : emoji;
        this.link = link;
    }

    public long getRemainingCents() { return priceCents - savedCents; }
    public int getProgressPercent() { return (int) (savedCents * 100 / priceCents); }

    // Getters & setters (Room needs both)
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public long getPriceCents() { return priceCents; }
    public void setPriceCents(long priceCents) { this.priceCents = priceCents; }
    public long getSavedCents() { return savedCents; }
    public void setSavedCents(long savedCents) { this.savedCents = savedCents; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public String getLink() { return link; }
    public void setLink(String link) { this.link = link; }
}