package com.pennywise.challenges;

public class ChallengeProgress {
    public final int percent;     // 0-100, for the progress bar
    public final String label;    // e.g. "$6 of $1,378 · 3/52 weeks"
    public final String status;   // "active", "completed" or "failed"

    public ChallengeProgress(int percent, String label, String status) {
        this.percent = Math.max(0, Math.min(100, percent));
        this.label = label;
        this.status = status;
    }
}