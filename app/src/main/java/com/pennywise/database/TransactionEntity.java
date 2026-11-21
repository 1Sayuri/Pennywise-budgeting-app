package com.pennywise.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.Ignore;


@Entity(
        tableName = "transactions",
        foreignKeys = @ForeignKey(
                entity = BankAccount.class,
                parentColumns = "accountId",
                childColumns = "accountId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("accountId")}
)

public class TransactionEntity {

    @PrimaryKey(autoGenerate = true)
    private int transactionId;

    private int accountId;
    private String description;
    private long amountCents;
    private String type;
    private String category;
    private long date;

    public TransactionEntity() {
    }

    @Ignore
    public TransactionEntity(
            int accountId,
            String description,
            long amountCents,
            String type,
            String category,
            long date) {

        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be empty.");
        }

        if (amountCents <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }

        if (type == null ||
                (!type.equalsIgnoreCase("income")
                        && !type.equalsIgnoreCase("expense"))) {
            throw new IllegalArgumentException("Invalid transaction type.");
        }

        this.accountId = accountId;
        this.description = description;
        this.amountCents = amountCents;
        this.type = type.toLowerCase();
        this.category = category;
        this.date = date;
    }

    // Getters

    public int getTransactionId() { return transactionId; }
    public int getAccountId() { return accountId; }
    public String getDescription() { return description; }
    public long getAmountCents() { return amountCents; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public long getDate() { return date; }

    // Setters

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAmountCents(long amountCents) {
        this.amountCents = amountCents;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDate(long date) {
        this.date = date;
    }
}