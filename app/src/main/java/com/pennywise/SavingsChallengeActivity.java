package com.pennywise;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.pennywise.challenges.ChallengeProgress;
import com.pennywise.challenges.ChallengeService;
import com.pennywise.database.AppDatabase;
import com.pennywise.database.DatabaseClient;
import com.pennywise.database.SavingsChallenge;
import com.pennywise.database.SavingsWeek;
import com.pennywise.database.TransactionEntity;
import com.pennywise.model.ChallengeTemplate;
import com.pennywise.model.Money;

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
                        action.setOnClickListener(x -> checkIn(r));
                    } else {
                        styleTag(action, "Active", R.color.tag_blue_bg, R.color.tag_blue);
                    }
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
            browseList.addView(v);
        }
    }

    private void start(ChallengeTemplate t) {
        executor.execute(() -> {
            ChallengeService.startChallenge(db, t);
            runOnUiThread(() -> {
                Toast.makeText(this, t.getTitle() + " started!", Toast.LENGTH_SHORT).show();
                load();
            });
        });
    }

    private void checkIn(Row r) {
        executor.execute(() -> {
            ChallengeService.checkIn(db, r.nextStep);
            runOnUiThread(() -> {
                Toast.makeText(this, r.template.getStepLabel() + " " + r.nextStep.getWeekNumber()
                        + " done!", Toast.LENGTH_SHORT).show();
                load();
            });
        });
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