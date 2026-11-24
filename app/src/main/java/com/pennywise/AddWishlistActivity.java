package com.pennywise;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.pennywise.database.AppDatabase;
import com.pennywise.database.BankAccount;
import com.pennywise.database.DatabaseClient;
import com.pennywise.database.WishlistEntity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
    private ImageView wishlistImagePreview;

    private String productImagePath;
    private String imageSourceUrl;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private Runnable previewRunnable;

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
        wishlistImagePreview = findViewById(R.id.wishlistImagePreview);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        setupEmojiSpinner();
        loadAccounts();
        setupProductLinkListener();

        saveButton.setOnClickListener(v -> saveWishlistItem());
    }

    private void setupEmojiSpinner() {

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                emojis
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        emojiSpinner.setAdapter(adapter);
    }

    private void loadAccounts() {

        executor.execute(() -> {

            List<BankAccount> savedAccounts =
                    database.bankAccountDao().getAllAccounts();

            runOnUiThread(() -> {

                accounts.clear();
                accounts.addAll(savedAccounts);

                List<String> accountNames = new ArrayList<>();

                for (BankAccount account : accounts) {
                    accountNames.add(
                            account.getBankName() + " - "
                                    + account.getAccountName()
                    );
                }

                if (accountNames.isEmpty()) {

                    Toast.makeText(
                            this,
                            "Please add a bank account first.",
                            Toast.LENGTH_LONG
                    ).show();

                    saveButton.setEnabled(false);
                    return;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        accountNames
                );

                adapter.setDropDownViewResource(
                        android.R.layout.simple_spinner_dropdown_item
                );

                accountSpinner.setAdapter(adapter);
                saveButton.setEnabled(true);
            });
        });
    }

    private void setupProductLinkListener() {

        linkInput.addTextChangedListener(new android.text.TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {

                productImagePath = null;
                imageSourceUrl = null;

                if (previewRunnable != null) {
                    handler.removeCallbacks(previewRunnable);
                }

                String url = s.toString().trim();

                if (url.isEmpty()) {
                    wishlistImagePreview.setImageResource(
                            android.R.drawable.ic_menu_gallery
                    );
                    return;
                }

                previewRunnable = () -> loadProductImage(url);

                handler.postDelayed(previewRunnable, 1000);
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });
    }

    private String normalizeUrl(String url) {

        if (!url.startsWith("http://")
                && !url.startsWith("https://")) {
            return "https://" + url;
        }

        return url;
    }

    private void loadProductImage(String url) {

        String normalizedUrl = normalizeUrl(url);

        executor.execute(() -> {

            try {

                String imagePath = fetchProductImage(normalizedUrl);

                if (imagePath != null) {

                    runOnUiThread(() -> {

                        if (normalizedUrl.equals(
                                normalizeUrl(linkInput.getText().toString().trim())
                        )) {

                            productImagePath = imagePath;
                            imageSourceUrl = normalizedUrl;

                            wishlistImagePreview.setImageURI(
                                    Uri.fromFile(new File(imagePath))
                            );

                            wishlistImagePreview.setScaleType(
                                    ImageView.ScaleType.CENTER_CROP
                            );
                        }
                    });
                }

            } catch (Exception e) {

                runOnUiThread(() -> {

                    if (!isFinishing()) {
                        Toast.makeText(
                                this,
                                "Could not retrieve product image.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
            }
        });
    }

    private String fetchProductImage(String productUrl) throws Exception {

        Document document = Jsoup.connect(productUrl)
                .userAgent(
                        "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 Chrome/120.0.0.0 Mobile Safari/537.36"
                )
                .timeout(15000)
                .followRedirects(true)
                .get();

        String imageUrl = null;

        if (document.selectFirst("meta[property=og:image]") != null) {

            imageUrl = document.selectFirst(
                    "meta[property=og:image]"
            ).attr("content");
        }

        if (imageUrl == null || imageUrl.isEmpty()) {

            if (document.selectFirst("meta[name=twitter:image]") != null) {

                imageUrl = document.selectFirst(
                        "meta[name=twitter:image]"
                ).attr("content");
            }
        }

        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        imageUrl = document.baseUri().isEmpty()
                ? imageUrl
                : document.baseUri().isEmpty()
                ? imageUrl
                : org.jsoup.parser.Parser.unescapeEntities(
                imageUrl, false
        );

        java.net.URL resolvedUrl = new java.net.URL(
                new java.net.URL(productUrl),
                imageUrl
        );

        byte[] imageBytes = Jsoup.connect(resolvedUrl.toString())
                .userAgent("Mozilla/5.0")
                .ignoreContentType(true)
                .timeout(15000)
                .maxBodySize(10 * 1024 * 1024)
                .execute()
                .bodyAsBytes();

        if (imageBytes.length == 0) {
            return null;
        }

        File directory = new File(
                getFilesDir(),
                "wishlist_images"
        );

        if (!directory.exists() && !directory.mkdirs()) {
            throw new Exception("Could not create image directory.");
        }

        File destination = new File(
                directory,
                UUID.randomUUID() + ".jpg"
        );

        try (FileOutputStream output =
                     new FileOutputStream(destination)) {

            output.write(imageBytes);
        }

        return destination.getAbsolutePath();
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

        if (accounts.isEmpty()
                || accountSpinner.getSelectedItemPosition() < 0) {

            Toast.makeText(
                    this,
                    "Please select a bank account",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String emoji = emojiSpinner.getSelectedItem().toString();

        BankAccount selectedAccount = accounts.get(
                accountSpinner.getSelectedItemPosition()
        );

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

            Toast.makeText(
                    this,
                    e.getMessage(),
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        saveButton.setEnabled(false);

        executor.execute(() -> {

            try {

                if (!link.isEmpty()) {

                    String normalizedUrl = normalizeUrl(link);

                    if (productImagePath == null
                            || !normalizedUrl.equals(imageSourceUrl)) {

                        try {
                            productImagePath =
                                    fetchProductImage(normalizedUrl);
                        } catch (Exception ignored) {
                            productImagePath = null;
                        }
                    }

                    if (productImagePath != null) {
                        item.setImagePath(productImagePath);
                    }
                }

                database.wishlistDao().insertItem(item);

                runOnUiThread(() -> {

                    Toast.makeText(
                            this,
                            "Wishlist item saved!",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    saveButton.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Could not save wishlist item.",
                            Toast.LENGTH_LONG
                    ).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        handler.removeCallbacksAndMessages(null);

        if (executor != null) {
            executor.shutdown();
        }
    }
}