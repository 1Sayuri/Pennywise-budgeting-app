package com.pennywise.model;

import java.util.Arrays;
import java.util.List;

public class ChallengeTemplate {

    public static final String CHECK_IN = "check_in";   // user taps "I saved this"
    public static final String AUTOMATIC = "automatic"; // app checks transactions

    private final String key;
    private final String emoji;
    private final String title;
    private final String description;
    private final String tracking;
    private final int stepCount;      // weeks or days
    private final String stepLabel;   // "Week" or "Day"
    private final int durationDays;

    public ChallengeTemplate(String key, String emoji, String title, String description,
                             String tracking, int stepCount, String stepLabel, int durationDays) {
        this.key = key;
        this.emoji = emoji;
        this.title = title;
        this.description = description;
        this.tracking = tracking;
        this.stepCount = stepCount;
        this.stepLabel = stepLabel;
        this.durationDays = durationDays;
    }

    // How much to save in a given step (step starts at 1). 0 for automatic challenges.
    public long amountForStep(int step) {
        switch (key) {
            case "week52":     return step * 100L; // week 1 = $1, week 2 = $2...
            case "five_a_day": return 500L;        // $5 every day
            default:           return 0L;
        }
    }

    public long totalTargetCents() {
        long total = 0;
        for (int i = 1; i <= stepCount; i++) total += amountForStep(i);
        return total;
    }

    // ===== The library =====
    public static final List<ChallengeTemplate> ALL = Arrays.asList(
            new ChallengeTemplate("week52", "💪", "52-Week Challenge",
                    "Save $1 in week 1, $2 in week 2, and keep going. $1,378 in a year.",
                    CHECK_IN, 52, "Week", 364),
            new ChallengeTemplate("no_spend_week", "🙅", "No-Spend Week",
                    "Go 7 days without logging a single expense.",
                    AUTOMATIC, 7, "Day", 7),
            new ChallengeTemplate("five_a_day", "🪙", "$5 a Day",
                    "Set aside $5 every day for 30 days. That's $150.",
                    CHECK_IN, 30, "Day", 30),
            new ChallengeTemplate("food_less", "🥗", "Spend Less on Food",
                    "\"Spend less on Food in the next 30 days than in the 30 days before.",
                    AUTOMATIC, 1, "Month", 30)
    );

    public static ChallengeTemplate find(String key) {
        for (ChallengeTemplate t : ALL) {
            if (t.key.equals(key)) return t;
        }
        return null;
    }

    // Getters
    public String getKey() { return key; }
    public String getEmoji() { return emoji; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getTracking() { return tracking; }
    public int getStepCount() { return stepCount; }
    public String getStepLabel() { return stepLabel; }
    public int getDurationDays() { return durationDays; }
    public boolean isCheckIn() { return CHECK_IN.equals(tracking); }
}