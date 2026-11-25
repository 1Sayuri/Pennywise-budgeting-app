package com.pennywise.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.Delete;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SavingsChallengeDao {
    @Insert
    long insertChallenge(SavingsChallenge challenge);

    @Insert
    void insertWeeks(List<SavingsWeek> weeks);

    @Update
    void updateChallenge(SavingsChallenge challenge);

    @Update
    void updateWeek(SavingsWeek week);

    @Delete
    void deleteChallenge(SavingsChallenge challenge);

    @Query("SELECT * FROM savings_challenges LIMIT 1")
    SavingsChallenge getChallenge();

    @Query("SELECT * FROM savings_weeks WHERE challengeId = :challengeId ORDER BY weekNumber ASC")
    List<SavingsWeek> getWeeks(int challengeId);

    @Query("SELECT SUM(savedAmountCents) FROM savings_weeks WHERE challengeId = :challengeId")
    Long getTotalSaved(int challengeId);

    @Query("SELECT COUNT(*) FROM savings_weeks WHERE challengeId = :challengeId AND isCompleted = 1")
    int getCompletedWeeks(int challengeId);

    @Query("SELECT * FROM savings_challenges ORDER BY startDate DESC")
    List<SavingsChallenge> getAllChallenges();
}