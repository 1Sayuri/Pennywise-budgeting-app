package com.pennywise.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore;

@Entity(tableName = "bank_accounts")
public class BankAccount {

    @PrimaryKey(autoGenerate = true)
    private int accountId;

    private String bankName;
    private String accountName;
    private String accountType;

    private String lastFour;
    private String cardImage;

    private long openingBalanceCents;
    @Ignore
    public BankAccount(String bankName, String accountName,
                       String accountType, long openingBalanceCents,
                       String lastFour, String cardImage) {
        this.bankName = bankName;
        this.accountName = accountName;
        this.accountType = accountType;
        this.openingBalanceCents = openingBalanceCents;
        this.lastFour = lastFour;
        this.cardImage = cardImage;
    }
    public BankAccount() {
    }
    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    public long getOpeningBalanceCents() {
        return openingBalanceCents;
    }
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getLastFour() { return lastFour; }
    public void setLastFour(String lastFour) { this.lastFour = lastFour; }

    public String getCardImage() { return cardImage; }
    public void setCardImage(String cardImage) { this.cardImage = cardImage; }
    public void setOpeningBalanceCents(long openingBalanceCents) {
        this.openingBalanceCents = openingBalanceCents;
    }
}