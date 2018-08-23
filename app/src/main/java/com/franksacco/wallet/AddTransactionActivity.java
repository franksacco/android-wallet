package com.franksacco.wallet;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.franksacco.wallet.entities.Transaction;
import com.franksacco.wallet.helpers.CategoriesManager;
import com.franksacco.wallet.entities.Category;
import com.franksacco.wallet.helpers.ChangeRateDownloader;
import com.franksacco.wallet.helpers.CurrencyManager;
import com.franksacco.wallet.helpers.TransactionsManager;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * Add a transaction activity
 */
@SuppressWarnings("RedundantCast")
public class AddTransactionActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
        ChangeRateDownloader.ChangeRateDownloaderListener {

    private static final String TAG = "AddTransactionActivity";

    /**
     * Activity request code
     */
    public static final int REQUEST_CODE = 100;
    /**
     * Result code when transaction is inserted successfully
     */
    public static final int RESULT_SAVED = 101;
    /**
     * Result code when an error occur during inserting
     */
    public static final int RESULT_ERROR = 102;

    private ImageView mTransactionTypeIcon;
    private TextView mDateText;
    private TextView mTimeText;

    private Transaction mTransaction;
    private int mTransactionType;
    private Calendar mDateTime = Calendar.getInstance();
    private Category mCategory;
    private String mCurrency;
    private int mPaymentType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.add_transaction_activity);

        Toolbar toolbar = (Toolbar) this.findViewById(R.id.addTransactionToolbar);
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        this.initTransactionTypeSpinner();
        this.initCategorySpinner();
        this.initCurrencySpinner();
        this.initPaymentTypeSpinner();
        this.initDate();
        this.initTime();
        Log.i(TAG, "activity created");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.add_transaction, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        this.exitWithoutSaving();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.exitWithoutSaving();
                return true;
            case R.id.addTransactionButton:
                this.prepareInsert();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void prepareInsert() {
        EditText amountInput = (EditText) this.findViewById(R.id.addTransaction_input_amount);
        double amount = (double) this.mTransactionType;
        try {
            amount *= Double.parseDouble(amountInput.getText().toString());
        } catch (NumberFormatException e) {
            Snackbar.make(this.findViewById(R.id.addTransactionLayout),
                    R.string.addTransaction_amountError, Snackbar.LENGTH_SHORT).show();
            return;
        }
        this.findViewById(R.id.addTransactionProgressBar).setVisibility(View.VISIBLE);

        EditText notesInput = (EditText) this.findViewById(R.id.addTransaction_input_notes);
        String notes = notesInput.getText().toString();

        this.mTransaction = new Transaction(mCategory, mDateTime, amount,
                this.mCurrency, 1.0, notes, mPaymentType);
        if (this.mCurrency.equals("EUR")) {
            this.insert();
        } else {
            new ChangeRateDownloader(this, this.mDateTime, this.mCurrency, this)
                    .execute();
        }
    }

    @Override
    public void onDownloadTerminated(ChangeRateDownloader.Result result) {
        if (result.getException() != null) {
            Snackbar.make(this.findViewById(R.id.editTransactionLayout),
                    R.string.rateDownload_error, Snackbar.LENGTH_LONG).show();
            return;
        }
        this.mTransaction.setChangeRate(1.0 / result.getChangeRate());
        this.insert();
    }

    private void insert() {
        Long id = new TransactionsManager(this).insert(this.mTransaction);
        this.findViewById(R.id.addTransactionProgressBar).setVisibility(View.GONE);
        this.setResult(id > 0 ? RESULT_SAVED : RESULT_ERROR);
        this.finish();
    }

    /**
     * Ask confirmation for exiting without saving.
     */
    private void exitWithoutSaving() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.confirmExit_dialog_title)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddTransactionActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null).create()
                .show();
    }

    /**
     * Initialize transaction type spinner
     */
    private void initTransactionTypeSpinner() {
        Spinner spinner = (Spinner) this.findViewById(R.id.addTransaction_input_transactionType);
        AddTransactionActivity.this.mTransactionTypeIcon =
                (ImageView) this.findViewById(R.id.addTransaction_icon_transactionType);

        spinner.setAdapter(ArrayAdapter.createFromResource(this,
                R.array.addTransaction_input_transactionType,
                android.R.layout.simple_spinner_dropdown_item
        ));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                AddTransactionActivity.this.mTransactionType = (position == 0 ? -1 : 1);
                int icon = (position == 0 ? R.drawable.ic_remove_circle_red_900_24dp
                        : R.drawable.ic_add_circle_green_900_24dp);
                AddTransactionActivity.this.mTransactionTypeIcon.setImageResource(icon);
                Log.i(TAG, "selected transaction type " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
    /**
     * Initialize category spinner
     */
    private void initCategorySpinner() {
        Spinner spinner = (Spinner) this.findViewById(R.id.addTransaction_input_category);

        CategoriesManager categoryHelper = new CategoriesManager(this);
        ArrayList<Category> categories = categoryHelper.select(CategoriesManager.ALL_COLUMNS,
                null, null, null, null, null, null);

        CategorySpinnerAdapter adapter = new CategorySpinnerAdapter(this);
        adapter.setItems(categories);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(adapter);
    }
    /**
     * Initialize currency spinner
     */
    private void initCurrencySpinner() {
        Spinner spinner = (Spinner) this.findViewById(R.id.addTransaction_input_currency);
        String[] currencies = this.getResources().getStringArray(R.array.currency_names);

        CurrencySpinnerAdapter adapter = new CurrencySpinnerAdapter(this, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(adapter);
        spinner.setSelection(0);
    }
    /**
     * Initialize payment type spinner
     */
    private void initPaymentTypeSpinner() {
        Spinner spinner = (Spinner) this.findViewById(R.id.addTransaction_input_paymentType);
        String[] options = this.getResources().getStringArray(R.array.payment_types);

        PaymentTypeSpinnerAdapter adapter = new PaymentTypeSpinnerAdapter(this, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(adapter);
    }
    /**
     * Initialize date text view
     */
    private void initDate() {
        this.mDateText = (TextView) this.findViewById(R.id.addTransaction_input_date);
        this.mDateText.setText(DateFormat.format("EEE dd MMM yyyy", this.mDateTime));
        this.mDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(AddTransactionActivity.this,
                        AddTransactionActivity.this,
                        AddTransactionActivity.this.mDateTime.get(Calendar.YEAR),
                        AddTransactionActivity.this.mDateTime.get(Calendar.MONTH),
                        AddTransactionActivity.this.mDateTime.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }
    /**
     * Initialize time text view
     */
    private void initTime() {
        this.mTimeText = (TextView) this.findViewById(R.id.addTransaction_input_time);
        this.mTimeText.setText(DateFormat.format("HH:mm", this.mDateTime));
        this.mTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(AddTransactionActivity.this,
                        AddTransactionActivity.this,
                        AddTransactionActivity.this.mDateTime.get(Calendar.HOUR_OF_DAY),
                        AddTransactionActivity.this.mDateTime.get(Calendar.MINUTE),
                        true).show();
            }
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.mDateTime.set(year, month, dayOfMonth);
        Log.i(TAG, "date changed to " + year + "-" + month + "-" + dayOfMonth);
        this.mDateText.setText(DateFormat.format("EEE dd MMM yyyy", mDateTime));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.mDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        this.mDateTime.set(Calendar.MINUTE, minute);
        Log.i(TAG, "time changed to " + hourOfDay + ":" + minute);
        this.mTimeText.setText(DateFormat.format("HH:mm", mDateTime));
    }

    /**
     * Adapter for category spinner
     */
    private class CategorySpinnerAdapter extends ArrayAdapter<Category>
            implements AdapterView.OnItemSelectedListener {

        CategorySpinnerAdapter(Context context) {
            super(context, R.layout.item_one_line, R.id.itemOneLine_title);
        }

        void setItems(ArrayList<Category> items) {
            this.addAll(items);
            this.notifyDataSetChanged();
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView,
                                    @NonNull ViewGroup parent) {
            View view = convertView;
            if(view == null) {
                view = (LinearLayout) getLayoutInflater()
                        .inflate(R.layout.item_one_line, parent, false);
            }
            Category category = this.getItem(position);
            if (category == null) {
                return view;
            }
            ImageView icon = (ImageView) view.findViewById(R.id.itemOneLine_icon);
            icon.setImageResource(category.getIconIdentifier());

            TextView textView = (TextView) view.findViewById(R.id.itemOneLine_title);
            textView.setText(category.getName());
            return view;
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if(view == null) {
                view = (LinearLayout) getLayoutInflater()
                        .inflate(R.layout.spinner_view, parent, false);
            }
            Category category = this.getItem(position);
            if (category == null) {
                return view;
            }
            TextView hint = (TextView) view.findViewById(R.id.spinnerView_hint);
            hint.setText(R.string.addTransaction_input_category_hint);

            TextView textView = (TextView) view.findViewById(R.id.spinnerView_value);
            textView.setText(category.getName());
            return view;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Category category = this.getItem(position);
            if (category != null) {
                AddTransactionActivity.this.mCategory = category;
                Log.i(TAG, "selected category " + category.getId());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    /**
     * Adapter for currency spinner
     */
    private class CurrencySpinnerAdapter extends ArrayAdapter<String>
            implements AdapterView.OnItemSelectedListener {

        CurrencySpinnerAdapter(Context context, String[] options) {
            super(context, android.R.layout.simple_spinner_dropdown_item, options);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = (LinearLayout) getLayoutInflater()
                        .inflate(R.layout.spinner_view, parent, false);
            }
            String currency = getItem(position);
            if (currency == null) {
                return view;
            }
            TextView hint = (TextView) view.findViewById(R.id.spinnerView_hint);
            hint.setText(R.string.addTransaction_input_currency_hint);
            TextView textView = (TextView) view.findViewById(R.id.spinnerView_value);
            textView.setText(currency);
            return view;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String code = CurrencyManager.getCode(position);
            AddTransactionActivity.this.mCurrency = code;
            Log.i(TAG, "selected currency " + code);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    /**
     * Adapter for payment type spinner
     */
    private class PaymentTypeSpinnerAdapter extends ArrayAdapter<String>
            implements AdapterView.OnItemSelectedListener {

        PaymentTypeSpinnerAdapter(Context context, String[] options) {
            super(context, android.R.layout.simple_spinner_dropdown_item, options);
        }

        @NonNull
        @Override
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
            View view = convertView;
            if(view == null) {
                view = (LinearLayout) getLayoutInflater()
                        .inflate(R.layout.spinner_view, parent, false);
            }
            String paymentType = getItem(position);
            if (paymentType == null) {
                return view;
            }
            TextView hint = (TextView) view.findViewById(R.id.spinnerView_hint);
            hint.setText(R.string.addTransaction_input_paymentType_hint);

            TextView textView = (TextView) view.findViewById(R.id.spinnerView_value);
            textView.setText(paymentType);
            return view;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String paymentType = getItem(position);
            if (paymentType != null) {
                AddTransactionActivity.this.mPaymentType = position;
                Log.i(TAG, "selected payment type " + paymentType);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

}
