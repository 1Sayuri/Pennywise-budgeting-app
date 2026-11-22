package com.pennywise;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.app.DatePickerDialog;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.pennywise.database.AppDatabase;
import com.pennywise.database.DatabaseClient;
import com.pennywise.database.BankAccount;
import com.pennywise.database.TransactionEntity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddTransactionActivity extends AppCompatActivity {

    private AppDatabase database;
    private ExecutorService executor;

    private EditText amountInput;
    private EditText descriptionInput;
    private RadioGroup typeRadioGroup;
    private Spinner categorySpinner;
    private Spinner accountSpinner;
    private Button dateButton;
    private Button saveButton;
    private TextView backButton;

    private List<BankAccount> accounts = new ArrayList<>();

    private long selectedDate = System.currentTimeMillis();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        database = DatabaseClient
                .getInstance(getApplicationContext())
                .getAppDatabase();

        executor = Executors.newSingleThreadExecutor();

        amountInput = findViewById(R.id.amountInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        typeRadioGroup = findViewById(R.id.typeRadioGroup);
        categorySpinner = findViewById(R.id.categorySpinner);
        accountSpinner = findViewById(R.id.accountSpinner);
        dateButton = findViewById(R.id.dateButton);
        saveButton = findViewById(R.id.saveTransactionButton);
        backButton = findViewById(R.id.backButton);

        setupCategories();
        loadAccounts();
        setupDatePicker();

        backButton.setOnClickListener(v -> finish());

        saveButton.setOnClickListener(v -> saveTransaction());
    }

    private void setupCategories() {

        String[] categories = {
                "Food",
                "Shopping",
                "Transportation",
                "Entertainment",
                "Bills",
                "Healthcare",
                "Education",
                "Salary",
                "Other"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        categorySpinner.setAdapter(adapter);
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
                            "No bank accounts found.",
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
            });
        });
    }

    private void setupDatePicker() {

        updateDateButton();

        dateButton.setOnClickListener(v -> {

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(selectedDate);

            DatePickerDialog picker = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {

                        Calendar selected = Calendar.getInstance();

                        selected.set(year, month, day, 12, 0, 0);
                        selected.set(Calendar.MILLISECOND, 0);

                        selectedDate = selected.getTimeInMillis();

                        updateDateButton();
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            picker.show();
        });
    }

    private void updateDateButton() {

        java.text.SimpleDateFormat format =
                new java.text.SimpleDateFormat(
                        "MMM dd, yyyy",
                        java.util.Locale.getDefault()
                );

        dateButton.setText(format.format(selectedDate));
    }

    private void saveTransaction() {

        String amountText = amountInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();

        if (amountText.isEmpty()) {
            amountInput.setError("Enter an amount");
            return;
        }

        if (description.isEmpty()) {
            descriptionInput.setError("Enter a description");
            return;
        }

        double amount;

        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            amountInput.setError("Enter a valid amount");
            return;
        }

        if (!Double.isFinite(amount) || amount <= 0
                || amount > 999999999999.99) {
            amountInput.setError("Enter a valid positive amount");
            return;
        }

        long amountCents = Math.round(amount * 100);

        if (amountCents <= 0) {
            amountInput.setError("Amount must be at least $0.01");
            return;
        }

        if (accounts.isEmpty() || accountSpinner.getSelectedItemPosition() < 0) {
            Toast.makeText(this, "Select a bank account", Toast.LENGTH_SHORT).show();
            return;
        }

        String type = typeRadioGroup.getCheckedRadioButtonId()
                == R.id.incomeRadio ? "income" : "expense";

        String category = categorySpinner.getSelectedItem().toString();

        BankAccount selectedAccount = accounts.get(
                accountSpinner.getSelectedItemPosition()
        );

        TransactionEntity transaction = new TransactionEntity(
                selectedAccount.getAccountId(),
                description,
                amountCents,
                type,
                category,
                selectedDate
        );

        saveButton.setEnabled(false);

        executor.execute(() -> {

            try {

                database.transactionDao().insertTransaction(transaction);

                runOnUiThread(() -> {

                    Toast.makeText(
                            this,
                            "Transaction saved!",
                            Toast.LENGTH_SHORT
                    ).show();

                    setResult(RESULT_OK);
                    finish();
                });

            } catch (Exception e) {

                runOnUiThread(() -> {

                    saveButton.setEnabled(true);

                    Toast.makeText(
                            this,
                            "Failed to save transaction: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
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