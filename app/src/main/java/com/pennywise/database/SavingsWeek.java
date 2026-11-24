package com.pennywise.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "savings_weeks",
foreignKeys = @ForeignKey(
        entity = SavingsChallenge.class,
        parentColumns = "challengeId",
childColumns = "challengeId",
onDelete = ForeignKey.CASCADE
),
indices = {@Index("challengeId")}
        )
public class SavingsWeek {
    @PrimaryKey(autoGenerate = true)
    private int weekId;

    private int challengeId;
    private int weekNumber;
    private long targetAmountCents;
    private long savedAmountCents;
    private boolean isCompleted;

    public SavingsWeek() {
    }

    public int getWeekId() {
        return weekId;
    }

    public void setWeekId(int weekId) {
        this.weekId = weekId;
    }

    public int getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(int challengeId) {
        this.challengeId = challengeId;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public long getTargetAmountCents() {
        return targetAmountCents;
    }

    public void setTargetAmountCents(long targetAmountCents) {
        this.targetAmountCents = targetAmountCents;
    }

    public long getSavedAmountCents() {
        return savedAmountCents;
    }

    public void setSavedAmountCents(long savedAmountCents) {
        this.savedAmountCents = savedAmountCents;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}