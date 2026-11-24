package com.pennywise.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "savings_challenges")
public class SavingsChallenge {
    @PrimaryKey(autoGenerate = true)
    private int challengeId;

    private String title;
    private long targetAmountCents;
    private long startDate;
    private String status;

    public SavingsChallenge() {
    }

    public int getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(int challengeId) {
        this.challengeId = challengeId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getTargetAmountCents() {
        return targetAmountCents;
    }

    public void setTargetAmountCents(long targetAmountCents) {
        this.targetAmountCents = targetAmountCents;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}