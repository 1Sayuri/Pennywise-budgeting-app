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
        loadCards(); // refresh every time we come back to this screen
    }

    private void loadCards() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<BankAccount> accounts = database.bankAccountDao().getAllAccounts();
            runOnUiThread(() -> showCards(accounts));
        });
    }

    private void showCards(List<BankAccount> accounts) {
        LinearLayout row = findViewById(R.id.cardRow);
        row.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(this);

        for (BankAccount a : accounts) {
            View card = inflater.inflate(R.layout.view_card, row, false);

            CardArt.bind(card, a.getCardImage(), a.getBankName(), a.getAccountName(),
                    a.getLastFour(), a.getAccountType(),
                    Money.format(a.getOpeningBalanceCents()), "");

            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) card.getLayoutParams();
            lp.setMarginEnd(Math.round(12 * getResources().getDisplayMetrics().density));
            row.addView(card, lp);
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

}