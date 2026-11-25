package com.pennywise;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.pennywise.ui.Anim;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.pennywise.challenges.ChallengeProgress;
import com.pennywise.challenges.ChallengeService;
import com.pennywise.database.AppDatabase;
import com.pennywise.database.BankAccount;
import com.pennywise.database.DatabaseClient;
import com.pennywise.database.SavingsChallenge;
import com.pennywise.database.SavingsWeek;
import com.pennywise.database.TransactionEntity;
import com.pennywise.model.ChallengeTemplate;
import com.pennywise.model.Money;
import com.pennywise.ui.CardArt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SavingsChallengeActivity extends AppCompatActivity {

    private AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Everything one row needs, computed in the background
    private static class Row {
        SavingsChallenge challenge;
        ChallengeTemplate template;
        ChallengeProgress progress;
        SavingsWeek nextStep; // next check-in, or null
        long saved;           // money reserved by this challenge
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_savings_challenge);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.challengeRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        db = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    // Load challenges + transactions, compute progress, then draw
    private void load() {
        executor.execute(() -> {
            List<SavingsChallenge> all = db.savingsChallengeDao().getAllChallenges();
            List<TransactionEntity> txs = db.transactionDao().getAllTransactions();

            List<Row> rows = new ArrayList<>();
            for (SavingsChallenge c : all) {
                Row r = new Row();
                r.challenge = c;
                r.template = ChallengeTemplate.find(c.getTemplateKey());
                if (r.template == null) continue; // old rows without a template
                r.progress = ChallengeService.progress(db, c, txs);
                if (r.template.isCheckIn()) {
                    for (SavingsWeek w : db.savingsChallengeDao().getWeeks(c.getChallengeId())) {
                        if (!w.isCompleted()) { r.nextStep = w; break; }
                    }
                }
                Long savedObj = db.savingsChallengeDao().getTotalSaved(c.getChallengeId());
                r.saved = savedObj == null ? 0 : savedObj;
                rows.add(r);
            }
            runOnUiThread(() -> draw(rows, all));
        });
    }

    private void draw(List<Row> rows, List<SavingsChallenge> all) {
        LayoutInflater inflater = LayoutInflater.from(this);

        // ===== Active (and finished) =====
        LinearLayout activeList = findViewById(R.id.activeList);
        activeList.removeAllViews();
        findViewById(R.id.activeEmpty).setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);

        for (Row r : rows) {
            View v = inflater.inflate(R.layout.item_challenge, activeList, false);
            ((TextView) v.findViewById(R.id.chEmoji)).setText(r.template.getEmoji());
            ((TextView) v.findViewById(R.id.chTitle)).setText(r.template.getTitle());
            ((TextView) v.findViewById(R.id.chSubtitle)).setText(r.progress.label);
            ((ProgressBar) v.findViewById(R.id.chProgress)).setProgress(r.progress.percent);

            TextView action = v.findViewById(R.id.chAction);
            switch (r.progress.status) {
                case "completed":
                    styleTag(action, "Completed 🎉", R.color.tag_green_bg, R.color.tag_green);
                    break;
                case "failed":
                    styleTag(action, "Failed", R.color.tag_red_bg, R.color.tag_red);
                    break;
                default:
                    if (r.nextStep != null) {
                        styleTag(action, "Check in · " + Money.format(r.nextStep.getTargetAmountCents()),
                                R.color.tag_pink_bg, R.color.tag_pink);
                        View emojiView = v.findViewById(R.id.chEmoji);
                        action.setOnClickListener(x -> checkIn(r, action, emojiView));
                    } else {
                        styleTag(action, "Active", R.color.tag_blue_bg, R.color.tag_blue);
                    }
            }

            // Trash only while the challenge is running
            View delete = v.findViewById(R.id.chDelete);
            if ("active".equals(r.progress.status)) {
                delete.setVisibility(View.VISIBLE);
                delete.setOnClickListener(x -> confirmQuit(r));
            } else {
                delete.setVisibility(View.GONE);
            }

            activeList.addView(v);
        }

        // ===== Browse (not running yet) =====
        LinearLayout browseList = findViewById(R.id.browseList);
        browseList.removeAllViews();

        for (ChallengeTemplate t : ChallengeTemplate.ALL) {
            if (ChallengeService.isRunning(t.getKey(), all)) continue;

            View v = inflater.inflate(R.layout.item_challenge, browseList, false);
            ((TextView) v.findViewById(R.id.chEmoji)).setText(t.getEmoji());
            ((TextView) v.findViewById(R.id.chTitle)).setText(t.getTitle());
            ((TextView) v.findViewById(R.id.chSubtitle)).setText(t.getDescription());
            v.findViewById(R.id.chProgress).setVisibility(View.GONE);

            TextView action = v.findViewById(R.id.chAction);
            styleTag(action, "Start", R.color.tag_green_bg, R.color.tag_green);
            action.setOnClickListener(x -> start(t));

            v.findViewById(R.id.chDelete).setVisibility(View.GONE);
            browseList.addView(v);
        }
    }

    private void start(ChallengeTemplate t) {
        if (!t.isCheckIn()) {
            startWithCard(t, null); // automatic: watches all cards
            return;
        }

        executor.execute(() -> {
            List<BankAccount> accounts = db.bankAccountDao().getAllAccounts();
            runOnUiThread(() -> {
                if (accounts.isEmpty()) {
                    Toast.makeText(this, "Add a card first", Toast.LENGTH_SHORT).show();
                    return;
                }
                showCardSheet(t, accounts);
            });
        });
    }

    private void showCardSheet(ChallengeTemplate t, List<BankAccount> accounts) {
        BottomSheetDialog sheet = new BottomSheetDialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.sheet_pick_card, null);

        ((TextView) view.findViewById(R.id.sheetSubtitle)).setText(
                t.getEmoji() + " " + t.getTitle() + " · " + Money.format(t.totalTargetCents()) + " total");

        LinearLayout options = view.findViewById(R.id.cardOptions);
        for (BankAccount a : accounts) {
            View row = LayoutInflater.from(this).inflate(R.layout.item_card_option, options, false);

            View art = row.findViewById(R.id.optionArt);
            art.setBackgroundResource(CardArt.drawableFor(a.getCardImage()));
            art.setClipToOutline(true);

            ((TextView) row.findViewById(R.id.optionBank)).setText(a.getBankName());
            String last = (a.getLastFour() == null || a.getLastFour().isEmpty()) ? "••••" : a.getLastFour();
            ((TextView) row.findViewById(R.id.optionDetails))
                    .setText(a.getAccountName() + " · •••• " + last);

            row.setOnClickListener(v -> {
                sheet.dismiss();
                startWithCard(t, a.getAccountId());
            });
            options.addView(row);
        }

        view.findViewById(R.id.sheetCancel).setOnClickListener(v -> sheet.dismiss());

        sheet.setContentView(view);
        sheet.show();

        // Let our rounded corners show (the default sheet background is a white square)
        View container = (View) view.getParent();
        container.setBackgroundColor(Color.TRANSPARENT);
    }

    private void startWithCard(ChallengeTemplate t, Integer accountId) {
        executor.execute(() -> {
            ChallengeService.startChallenge(db, t, accountId);
            runOnUiThread(() -> {
                Toast.makeText(this, t.getTitle() + " started!", Toast.LENGTH_SHORT).show();
                load();
            });
        });
    }

    private void checkIn(Row r, View button, View emoji) {
        long amount = r.nextStep.getTargetAmountCents();
        boolean lastStep = r.nextStep.getWeekNumber() == r.template.getStepCount();

        executor.execute(() -> {
            ChallengeService.checkIn(db, r.nextStep);
            runOnUiThread(() -> {
                Anim.floatText(this, button, "+" + Money.format(amount));
                Anim.pop(emoji);
                if (lastStep) {
                    Anim.confetti(this); // challenge complete!
                    Toast.makeText(this, r.template.getTitle() + " completed! 🎉",
                            Toast.LENGTH_SHORT).show();
                }
                button.postDelayed(this::load, 700);
            });
        });
    }

    private void confirmQuit(Row r) {
        String message;
        if (r.saved > 0 && r.challenge.getAccountId() != null) {
            message = "Your progress will be deleted, and "
                    + Money.format(r.saved) + " will be released back to your card.";
        } else {
            message = "Your progress will be deleted.";
        }

        new AlertDialog.Builder(this)
                .setTitle("Quit " + r.template.getTitle() + "?")
                .setMessage(message)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Quit", (dialog, which) ->
                        executor.execute(() -> {
                            db.savingsChallengeDao().deleteChallenge(r.challenge);
                            runOnUiThread(() -> {
                                Toast.makeText(this, r.template.getTitle() + " removed",
                                        Toast.LENGTH_SHORT).show();
                                load();
                            });
                        }))
                .show();
    }

    private void styleTag(TextView tag, String text, int bgColor, int textColor) {
        tag.setText(text);
        tag.setBackgroundTintList(ContextCompat.getColorStateList(this, bgColor));
        tag.setTextColor(ContextCompat.getColor(this, textColor));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}