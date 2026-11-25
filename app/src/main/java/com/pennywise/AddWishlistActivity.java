package com.pennywise;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pennywise.database.AppDatabase;
import com.pennywise.database.BankAccount;
import com.pennywise.database.DatabaseClient;
import com.pennywise.database.WishlistEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddWishlistActivity extends AppCompatActivity {

    private AppDatabase database;
    private ExecutorService executor;

    private EditText itemNameInput;
    private EditText priceInput;
    private EditText linkInput;

    private Spinner emojiSpinner;
    private Spinner accountSpinner;

    private Button saveButton;

    private List<BankAccount> accounts = new ArrayList<>();

    private final String[] emojis = {
            "🎧", "💻", "📱", "👟", "👗",
            "👜", "🎮", "📷", "⌚", "📚",
            "🎁", "✈️", "💄", "🏠", "🚲"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wishlist);

        database = DatabaseClient.getInstance(getApplicationContext())
                .getAppDatabase();

        executor = Executors.newSingleThreadExecutor();

        itemNameInput = findViewById(R.id.itemNameInput);
        priceInput = findViewById(R.id.priceInput);
        linkInput = findViewById(R.id.linkInput);

        emojiSpinner = findViewById(R.id.emojiSpinner);
        accountSpinner = findViewById(R.id.accountSpinner);

        saveButton = findViewById(R.id.saveWishlistButton);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        setupEmojiSpinner();
        loadAccounts();

        saveButton.setOnClickListener(v -> saveWishlistItem());
    }

    private void setupEmojiSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                emojis
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        emojiSpinner.setAdapter(adapter);
    }

    private void loadAccounts() {
        executor.execute(() -> {
            List<BankAccount> savedAccounts = database.bankAccountDao().getAllAccounts();

            runOnUiThread(() -> {
                accounts.clear();
                accounts.addAll(savedAccounts);

                List<String> accountNames = new ArrayList<>();
                for (BankAccount account : accounts) {
                    accountNames.add(account.getBankName() + " - " + account.getAccountName());
                }

                if (accountNames.isEmpty()) {
                    Toast.makeText(this, "Please add a bank account first.",
                            Toast.LENGTH_LONG).show();
                    saveButton.setEnabled(false);
                    return;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        accountNames
                );
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                accountSpinner.setAdapter(adapter);
                saveButton.setEnabled(true);
            });
        });
    }

    private void saveWishlistItem() {
        String name = itemNameInput.getText().toString().trim();
        String priceText = priceInput.getText().toString().trim();
        String link = linkInput.getText().toString().trim();

        if (name.isEmpty()) {
            itemNameInput.setError("Enter an item name");
            return;
        }

        if (priceText.isEmpty()) {
            priceInput.setError("Enter a target price");
            return;
        }

        long priceCents;
        try {
            BigDecimal price = new BigDecimal(priceText);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                priceInput.setError("Price must be greater than zero");
                return;
            }
            priceCents = price.movePointRight(2)
                    .setScale(0, RoundingMode.HALF_UP)
                    .longValueExact();
        } catch (Exception e) {
            priceInput.setError("Enter a valid price");
            return;
        }

        if (priceCents <= 0) {
            priceInput.setError("Price must be at least $0.01");
            return;
        }

        if (accounts.isEmpty() || accountSpinner.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "Please select a bank account", Toast.LENGTH_SHORT).show();
            return;
        }

        String emoji = emojiSpinner.getSelectedItem().toString();
        BankAccount selectedAccount = accounts.get(accountSpinner.getSelectedItemPosition());

        WishlistEntity item;
        try {
            item = new WishlistEntity(
                    selectedAccount.getAccountId(),
                    name,
                    priceCents,
                    emoji,
                    link.isEmpty() ? null : link
            );
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

        saveButton.setEnabled(false);

        executor.execute(() -> {
            try {
                database.wishlistDao().insertItem(item);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Wishlist item saved!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    saveButton.setEnabled(true);
                    Toast.makeText(this, "Could not save wishlist item.",
                            Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}