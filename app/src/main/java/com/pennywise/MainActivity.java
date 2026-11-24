package com.pennywise;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import com.pennywise.database.AppDatabase;
import com.pennywise.database.DatabaseClient;
import android.util.Log;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.pennywise.database.BankAccount;
import com.pennywise.model.Money;
import com.pennywise.ui.CardArt;
import java.util.List;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import com.pennywise.database.TransactionEntity;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import android.widget.ProgressBar;
import com.pennywise.database.WishlistEntity;
import android.app.AlertDialog;
import android.widget.EditText;
import android.text.InputType;
import android.widget.Toast;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.File;


public class MainActivity extends AppCompatActivity {

    private AppDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = DatabaseClient
                .getInstance(getApplicationContext())
                .getAppDatabase();
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_main);
        findViewById(R.id.addTransactionButton).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddTransactionActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.addCardLink).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddCardActivity.class)));
        findViewById(R.id.addWishLink).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddWishlistActivity.class)));


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        checkSavedAccounts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
    }

    // 1. Load everything from the database in the background
    private void loadDashboard() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<BankAccount> accounts = database.bankAccountDao().getAllAccounts();
            List<TransactionEntity> txs = database.transactionDao().getAllTransactions();
            List<WishlistEntity> wishes = database.wishlistDao().getAllItems();
            runOnUiThread(() -> {
                showTotals(accounts, txs);
                showBuckets(txs);
                showWishlist(wishes, accounts);
                showCards(accounts, txs, wishes);
            });
        });
    }

    // 2. A card's live balance
    private long balanceFor(BankAccount a, List<TransactionEntity> txs) {
        long balance = a.getOpeningBalanceCents();
        for (TransactionEntity t : txs) {
            if (t.getAccountId() != a.getAccountId()) continue;
            if (t.getType().equals("income")) balance += t.getAmountCents();
            else balance -= t.getAmountCents();
        }
        return balance;
    }

    // 3 + 4. Top properties
    private void showTotals(List<BankAccount> accounts, List<TransactionEntity> txs) {
        long total = 0;
        for (BankAccount a : accounts) total += balanceFor(a, txs);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        long monthStart = c.getTimeInMillis();

        long monthIn = 0, monthOut = 0;
        for (TransactionEntity t : txs) {
            if (t.getDate() < monthStart) continue;
            if (t.getType().equals("income")) monthIn += t.getAmountCents();
            else monthOut += t.getAmountCents();
        }

        ((TextView) findViewById(R.id.balanceText)).setText(Money.format(total));
        ((TextView) findViewById(R.id.incomeText)).setText("+" + Money.format(monthIn));
        ((TextView) findViewById(R.id.expenseText)).setText("−" + Money.format(monthOut));
    }

    // 5. Buckets grouped by category
    private void showBuckets(List<TransactionEntity> txs) {
        Map<String, long[]> byCategory = new LinkedHashMap<>(); // name -> {received, spent}
        for (TransactionEntity t : txs) {
            String cat = (t.getCategory() == null || t.getCategory().trim().isEmpty())
                    ? "Other" : t.getCategory().trim();
            long[] sums = byCategory.get(cat);
            if (sums == null) {
                sums = new long[2];
                byCategory.put(cat, sums);
            }
            if (t.getType().equals("income")) sums[0] += t.getAmountCents();
            else sums[1] += t.getAmountCents();
        }

        LinearLayout list = findViewById(R.id.bucketList);
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);
        int[][] tagColors = {
                {R.color.tag_orange_bg, R.color.tag_orange},
                {R.color.tag_green_bg, R.color.tag_green},
                {R.color.tag_blue_bg, R.color.tag_blue}
        };

        int i = 0;
        for (Map.Entry<String, long[]> e : byCategory.entrySet()) {
            long received = e.getValue()[0];
            long spent = e.getValue()[1];

            View row = inflater.inflate(R.layout.item_bucket, list, false);
            ((TextView) row.findViewById(R.id.bucketName)).setText(e.getKey());
            ((TextView) row.findViewById(R.id.bucketLeft)).setText(Money.format(received - spent));

            TextView used = row.findViewById(R.id.bucketUsed);
            used.setText(received == 0 ? "—" : (spent * 100 / received) + "%");
            int[] colors = tagColors[i % tagColors.length];
            used.setBackgroundTintList(ContextCompat.getColorStateList(this, colors[0]));
            used.setTextColor(ContextCompat.getColor(this, colors[1]));

            list.addView(row);
            i++;
        }
    }

    // Cards row with live balances
    private void showCards(List<BankAccount> accounts, List<TransactionEntity> txs,
                           List<WishlistEntity> wishes) {
        LinearLayout row = findViewById(R.id.cardRow);
        row.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (BankAccount a : accounts) {
            long reserved = reservedFor(a.getAccountId(), wishes);
            long available = balanceFor(a, txs) - reserved;

            View card = inflater.inflate(R.layout.view_card, row, false);
            CardArt.bind(card, a.getCardImage(), a.getBankName(), a.getAccountName(),
                    a.getLastFour(), a.getAccountType(),
                    Money.format(available),
                    reserved > 0 ? Money.format(reserved) + " reserved" : "");

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) card.getLayoutParams();
            lp.setMarginEnd(Math.round(12 * getResources().getDisplayMetrics().density));
            row.addView(card, lp);
        }
    }

    // Money reserved on one card for wishlist items
    private long reservedFor(int accountId, List<WishlistEntity> wishes) {
        long sum = 0;
        for (WishlistEntity w : wishes) {
            if (w.getAccountId() == accountId) sum += w.getSavedCents();
        }
        return sum;
    }

    private String cardName(int accountId, List<BankAccount> accounts) {
        for (BankAccount a : accounts) {
            if (a.getAccountId() == accountId) return a.getAccountName();
        }
        return "";
    }

    private void showWishlist(List<WishlistEntity> wishes, List<BankAccount> accounts) {
        LinearLayout list = findViewById(R.id.wishList);
        list.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        findViewById(R.id.wishEmpty).setVisibility(wishes.isEmpty() ? View.VISIBLE : View.GONE);

        for (WishlistEntity w : wishes) {
            View row = inflater.inflate(R.layout.item_wish, list, false);
            ((TextView) row.findViewById(R.id.wishEmoji)).setText(w.getEmoji());
            ((TextView) row.findViewById(R.id.wishName)).setText(w.getName());
            ((ProgressBar) row.findViewById(R.id.wishProgress)).setProgress(w.getProgressPercent());
            ((TextView) row.findViewById(R.id.wishAmounts)).setText(
                    Money.format(w.getSavedCents()) + " / " + Money.format(w.getPriceCents())
                            + " · " + cardName(w.getAccountId(), accounts));

            TextView save = row.findViewById(R.id.wishSave);
            if (w.getRemainingCents() <= 0) {
                save.setText("Done ✓");
                save.setEnabled(false);
            } else {

                save.setOnClickListener(v -> showSaveDialog(w));

            }
            TextView delete = row.findViewById(R.id.wishDelete);

            delete.setOnClickListener(v -> {

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Wishlist Item")
                        .setMessage("Are you sure you want to delete "
                                + w.getName() + "?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (dialog, which) -> {

                            Executors.newSingleThreadExecutor().execute(() -> {

                                try {

                                    database.wishlistDao().deleteItem(w);

                                    String imagePath = w.getImagePath();

                                    if (imagePath != null && !imagePath.isEmpty()) {
                                        File imageFile = new File(imagePath);

                                        if (imageFile.exists()) {
                                            imageFile.delete();
                                        }
                                    }

                                    runOnUiThread(() -> {
                                        Toast.makeText(
                                                MainActivity.this,
                                                "Wishlist item deleted",
                                                Toast.LENGTH_SHORT
                                        ).show();

                                        loadDashboard();
                                    });

                                } catch (Exception e) {

                                    runOnUiThread(() ->
                                            Toast.makeText(
                                                    MainActivity.this,
                                                    "Could not delete wishlist item.",
                                                    Toast.LENGTH_SHORT
                                            ).show()
                                    );
                                }

                            });

                        })
                        .show();
            });
            list.addView(row);
        }

        // Callout describes the first item
        TextView callout = findViewById(R.id.calloutText);
        if (wishes.isEmpty()) {
            callout.setText("Add something to your wishlist and start saving toward it.");
        } else {
            WishlistEntity first = wishes.get(0);
            callout.setText(first.getName() + " is " + first.getProgressPercent()
                    + "% funded. " + Money.format(first.getRemainingCents()) + " to go.");
        }
    }
    private void checkSavedAccounts() {

        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {

            java.util.List<com.pennywise.database.BankAccount> accounts =
                    database.bankAccountDao().getAllAccounts();

            Log.d("ACCOUNT_CHECK", "Total accounts: " + accounts.size());

            for (com.pennywise.database.BankAccount account : accounts) {

                Log.d("ACCOUNT_CHECK",
                        "ID: " + account.getAccountId()
                                + " | Bank: " + account.getBankName()
                                + " | Name: " + account.getAccountName()
                                + " | Type: " + account.getAccountType()
                                + " | Balance: $" + account.getOpeningBalanceCents() / 100.0);
            }

            executor.shutdown();
        });
    }
    private void showSaveDialog(WishlistEntity item) {

        EditText amountInput = new EditText(this);
        amountInput.setHint("Enter amount ($)");
        amountInput.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        new AlertDialog.Builder(this)
                .setTitle("Save for " + item.getName())
                .setMessage("Remaining: " +
                        Money.format(item.getRemainingCents()))
                .setView(amountInput)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", (dialog, which) -> {

                    String input = amountInput.getText().toString().trim();

                    if (input.isEmpty()) {
                        Toast.makeText(this,
                                "Enter an amount",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        long amount = new BigDecimal(input)
                                .movePointRight(2)
                                .setScale(0, RoundingMode.HALF_UP)
                                .longValueExact();

                        if (amount <= 0) {
                            Toast.makeText(this,
                                    "Amount must be positive",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (amount > item.getRemainingCents()) {
                            Toast.makeText(this,
                                    "Amount exceeds remaining target",
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }

                        item.setSavedCents(item.getSavedCents() + amount);

                        Executors.newSingleThreadExecutor().execute(() -> {
                            database.wishlistDao().updateItem(item);

                            runOnUiThread(this::loadDashboard);
                        });

                    } catch (Exception e) {
                        Toast.makeText(this,
                                "Enter a valid amount",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}