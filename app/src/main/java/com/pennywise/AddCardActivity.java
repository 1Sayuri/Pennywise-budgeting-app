package com.pennywise;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.pennywise.database.BankAccount;
import com.pennywise.database.DatabaseClient;
import com.pennywise.model.Money;
import com.pennywise.ui.CardArt;

import java.util.concurrent.Executors;

public class AddCardActivity extends AppCompatActivity {

    private View preview;
    private EditText bankInput, nicknameInput, lastFourInput, balanceInput;
    private RadioGroup typeGroup;
    private LinearLayout artPicker;
    private String selectedArt = CardArt.NAMES[0]; // sage by default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.enableEdgeToEdge(getWindow());
        setContentView(R.layout.activity_add_card);

        // Keep content clear of the status bar and keyboard
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addCardRoot), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // Find the views from the layout
        preview = findViewById(R.id.cardPreview);
        bankInput = findViewById(R.id.bankInput);
        nicknameInput = findViewById(R.id.nicknameInput);
        lastFourInput = findViewById(R.id.lastFourInput);
        balanceInput = findViewById(R.id.balanceInput);
        typeGroup = findViewById(R.id.typeGroup);
        artPicker = findViewById(R.id.artPicker);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.saveCardButton).setOnClickListener(v -> saveCard());

        // 1. Live preview: update the card on every keystroke
        TextWatcher watcher = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) { updatePreview(); }
        };
        bankInput.addTextChangedListener(watcher);
        nicknameInput.addTextChangedListener(watcher);
        lastFourInput.addTextChangedListener(watcher);
        balanceInput.addTextChangedListener(watcher);

        // 3. Type switch also updates the preview
        typeGroup.setOnCheckedChangeListener((group, id) -> updatePreview());

        // 2. Build the card art swatches
        buildArtPicker();
        updatePreview();
    }

    // 2. Art picker: 5 mini cards, ring around the selected one
    private void buildArtPicker() {
        artPicker.removeAllViews();
        for (String art : CardArt.NAMES) {
            FrameLayout ring = new FrameLayout(this);
            int pad = dp(3);
            ring.setPadding(pad, pad, pad, pad);
            if (art.equals(selectedArt)) {
                ring.setBackgroundResource(R.drawable.bg_swatch_ring);
            }

            View swatch = new View(this);
            swatch.setBackgroundResource(CardArt.drawableFor(art));
            swatch.setClipToOutline(true);
            ring.addView(swatch, new FrameLayout.LayoutParams(dp(54), dp(36)));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(dp(8));
            ring.setContentDescription(art.replace("card_art_", "") + " card art");

            ring.setOnClickListener(v -> {
                selectedArt = art;
                buildArtPicker(); // redraw rings
                updatePreview();
            });
            artPicker.addView(ring, lp);
        }
    }

    // 3. Which type is selected
    private String selectedType() {
        int id = typeGroup.getCheckedRadioButtonId();
        if (id == R.id.typeCredit) return "credit";
        if (id == R.id.typeSavings) return "savings";
        return "debit";
    }

    // 1. Redraw the preview card from the current inputs
    private void updatePreview() {
        String bank = textOr(bankInput, "PennyBank");
        String nickname = textOr(nicknameInput, "Everyday Card");
        String lastFour = lastFourInput.getText().toString().trim();

        String balance;
        try {
            balance = Money.format(Money.parse(balanceInput.getText().toString()));
        } catch (Exception e) {
            balance = "$0.00"; // empty or half-typed number
        }

        CardArt.bind(preview, selectedArt, bank, nickname, lastFour, selectedType(), balance, "");
    }

    // 4 + 5 + 6. Validate, save in the background, close
    private void saveCard() {
        String bank = bankInput.getText().toString().trim();
        String nickname = nicknameInput.getText().toString().trim();
        String lastFour = lastFourInput.getText().toString().trim();

        if (bank.isEmpty()) { bankInput.setError("Enter a bank name"); return; }
        if (nickname.isEmpty()) { nicknameInput.setError("Enter a nickname"); return; }
        if (!lastFour.isEmpty() && lastFour.length() != 4) {
            lastFourInput.setError("Use exactly 4 digits"); return;
        }

        long balanceCents;
        try {
            balanceCents = Money.parse(balanceInput.getText().toString());
        } catch (Exception e) {
            balanceInput.setError("Enter an amount like 1000.00"); return;
        }

        BankAccount account = new BankAccount(bank, nickname, selectedType(),
                balanceCents, lastFour, selectedArt);

        Executors.newSingleThreadExecutor().execute(() -> {
            DatabaseClient.getInstance(getApplicationContext())
                    .getAppDatabase()
                    .bankAccountDao()
                    .insertAccount(account);

            runOnUiThread(() -> {
                Toast.makeText(this, "Card saved", Toast.LENGTH_SHORT).show();
                finish(); // back to the dashboard
            });
        });
    }

    private String textOr(EditText input, String fallback) {
        String t = input.getText().toString().trim();
        return t.isEmpty() ? fallback : t;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}