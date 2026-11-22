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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        checkSavedAccounts();
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