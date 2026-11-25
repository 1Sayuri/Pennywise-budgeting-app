package com.pennywise.challenges;

import com.pennywise.database.AppDatabase;
import com.pennywise.database.SavingsChallenge;
import com.pennywise.database.SavingsWeek;
import com.pennywise.database.TransactionEntity;
import com.pennywise.model.ChallengeTemplate;
import com.pennywise.model.Money;


import java.util.ArrayList;
import java.util.List;

// All methods touch the database, so call them from a background thread.
public final class ChallengeService {

    private ChallengeService() {}

    public static final long DAY = 24L * 60 * 60 * 1000;

    // 1. Start a challenge from the library
    public static long startChallenge(AppDatabase db, ChallengeTemplate t, Integer accountId) {
        long now = System.currentTimeMillis();
        SavingsChallenge c = new SavingsChallenge();
        c.setTitle(t.getTitle());
        c.setTemplateKey(t.getKey());
        c.setTargetAmountCents(t.totalTargetCents());
        c.setStartDate(now);
        c.setEndDate(now + t.getDurationDays() * DAY);
        c.setStatus("active");
        c.setAccountId(accountId);   // NEW


        long id = db.savingsChallengeDao().insertChallenge(c);

        if (t.isCheckIn()) {
            List<SavingsWeek> steps = new ArrayList<>();
            for (int i = 1; i <= t.getStepCount(); i++) {
                SavingsWeek w = new SavingsWeek();
                w.setChallengeId((int) id);
                w.setWeekNumber(i);
                w.setTargetAmountCents(t.amountForStep(i));
                w.setSavedAmountCents(0);
                w.setCompleted(false);
                steps.add(w);
            }
            db.savingsChallengeDao().insertWeeks(steps);
        }
        return id;
    }

    // 2. Check in one step (week or day)
    public static void checkIn(AppDatabase db, SavingsWeek step) {
        step.setSavedAmountCents(step.getTargetAmountCents());
        step.setCompleted(true);
        db.savingsChallengeDao().updateWeek(step);
    }

    // Is this challenge already running?
    public static boolean isRunning(String templateKey, List<SavingsChallenge> all) {
        for (SavingsChallenge c : all) {
            if (templateKey.equals(c.getTemplateKey()) && "active".equals(c.getStatus())) {
                return true;
            }
        }
        return false;
    }

    // 3. Progress for any challenge type
    public static ChallengeProgress progress(AppDatabase db, SavingsChallenge c,
                                             List<TransactionEntity> txs) {
        ChallengeTemplate t = ChallengeTemplate.find(c.getTemplateKey());
        if (t == null) return new ChallengeProgress(0, "", c.getStatus());

        ChallengeProgress p;
        switch (t.getKey()) {
            case "no_spend_week": p = noSpend(c, txs); break;
            case "food_less":     p = foodLess(c, txs); break;
            default:              p = checkInProgress(db, c, t); break;
        }

        // 4. Save the status if it changed
        if (!p.status.equals(c.getStatus())) {
            c.setStatus(p.status);
            db.savingsChallengeDao().updateChallenge(c);
        }
        return p;
    }

    private static ChallengeProgress checkInProgress(AppDatabase db, SavingsChallenge c,
                                                     ChallengeTemplate t) {
        Long savedObj = db.savingsChallengeDao().getTotalSaved(c.getChallengeId());
        long saved = savedObj == null ? 0 : savedObj;
        int done = db.savingsChallengeDao().getCompletedWeeks(c.getChallengeId());

        int percent = c.getTargetAmountCents() == 0 ? 0
                : (int) (saved * 100 / c.getTargetAmountCents());
        String label = Money.format(saved) + " of " + Money.format(c.getTargetAmountCents())
                + " · " + done + "/" + t.getStepCount() + " "
                + t.getStepLabel().toLowerCase() + "s";
        String status = done >= t.getStepCount() ? "completed" : "active";
        return new ChallengeProgress(percent, label, status);
    }

    private static ChallengeProgress noSpend(SavingsChallenge c, List<TransactionEntity> txs) {
        long now = System.currentTimeMillis();
        long end = Math.min(now, c.getEndDate());

        for (TransactionEntity tx : txs) {
            if (tx.getType().equals("expense")
                    && tx.getDate() >= c.getStartDate() && tx.getDate() <= end) {
                int day = (int) ((tx.getDate() - c.getStartDate()) / DAY) + 1;
                return new ChallengeProgress(100,
                        "Spent on day " + day + ": " + tx.getDescription(), "failed");
            }
        }

        int days = (int) Math.min(7, (now - c.getStartDate()) / DAY + 1);
        if (now >= c.getEndDate()) {
            return new ChallengeProgress(100, "7 days, no spending!", "completed");
        }
        return new ChallengeProgress(days * 100 / 7,
                "Day " + days + " of 7 · no spending so far", "active");
    }

    private static ChallengeProgress foodLess(SavingsChallenge c, List<TransactionEntity> txs) {
        long now = System.currentTimeMillis();
        long start = c.getStartDate();
        long end = Math.min(now, c.getEndDate());
        long length = c.getEndDate() - start;

        long spentNow = 0, spentBefore = 0;
        for (TransactionEntity tx : txs) {
            if (!tx.getType().equals("expense")) continue;
            if (tx.getCategory() == null || !tx.getCategory().trim().equalsIgnoreCase("food")) continue;

            if (tx.getDate() >= start && tx.getDate() <= end) spentNow += tx.getAmountCents();
            else if (tx.getDate() >= start - length && tx.getDate() < start) spentBefore += tx.getAmountCents();
        }

        int timePercent = (int) ((end - start) * 100 / Math.max(1, length));
        String label = Money.format(spentNow) + " spent · beat " + Money.format(spentBefore);

        if (spentBefore > 0 && spentNow >= spentBefore) {
            return new ChallengeProgress(100, label, "failed");
        }
        if (now >= c.getEndDate()) {
            return new ChallengeProgress(100, label, "completed");
        }
        return new ChallengeProgress(timePercent, label, "active");
    }
}