package com.franksacco.wallet;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.franksacco.wallet.entities.Category;
import com.franksacco.wallet.entities.Transaction;
import com.franksacco.wallet.helpers.CategoriesManager;
import com.franksacco.wallet.helpers.ChangeRateDownloader;
import com.franksacco.wallet.helpers.CurrencyManager;
import com.franksacco.wallet.helpers.TransactionsManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


/**
 * View, edit and delete transaction activity
 */
@SuppressWarnings("RedundantCast")
public class EditTransactionActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener,
        ChangeRateDownloader.ChangeRateDownloaderListener {

    private static final String TAG = "EditTransactionActivity";

    /**
     * Activity request code
     */
    public static final int REQUEST_CODE = 110;
    /**
     * Result code when transaction is updated
     */
    public static final int RESULT_UPDATED = 111;
    /**
     * Result code when transaction is deleted
     */
    public static final int RESULT_DELETED = 112;
    /**
     * Result code when an error occur during deleting
     */
    public static final int RESULT_DELETED_ERROR = 113;
    /**
     * Transaction id key in activity intent
     */
    public static final String TRANSACTION_ID = "transactionId";

    /**
     * Target transaction
     */
    private Transaction mTransaction;
    private boolean mChangesSaved = true;
    private String mInitialCurrency;
    private int mTransactionType;

    private ImageView mTransactionTypeIcon;
    private TextView mDateText;
    private TextView mTimeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.edit_transaction_activity);

        Toolbar toolbar = (Toolbar) this.findViewById(R.id.editTransactionToolbar);
        this.setSupportActionBar(toolbar);
        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        int id = (int) this.getIntent().getLongExtra(TRANSACTION_ID, -1);
        if (id > 0) {
            this.mTransaction = this.getTransaction(id);
        }
        if (this.mTransaction == null) {
            Log.e(TAG, "=== transaction not found ===");
            this.finish();
        }
        this.initTransactionTypeSpinner();
        this.initCategorySpinner();
        this.initAmount();
        this.initCurrencySpinner();
        this.initPaymentTypeSpinner();
        this.initDate();
        this.initTime();
        this.initNotes();

        Log.i(TAG, "created");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.edit_transaction, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (this.mChangesSaved) {
            super.onBackPressed();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.confirmExit_dialog_title)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            EditTransactionActivity.this.finish();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .create()
                    .show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        } else if (id == R.id.updateTransactionButton) {
            this.update();
            return true;
        } else if (id == R.id.deleteTransactionButton) {
            this.delete();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Get transaction object from database
     */
    @Nullable
    private Transaction getTransaction(int id) {
        TransactionsManager manager = new TransactionsManager(this);
        ArrayList<Transaction> result = manager.select(
                "T." + TransactionsManager.ID_COL + " = ?", null, null,
                null, null, new String[]{String.valueOf(id)});
        if (result.size() == 1) {
            return result.get(0);
        }
        return null;
    }

    /**
     * Update transaction in database
     */
    private void update() {
        this.findViewById(R.id.editTransactionProgressBar).setVisibility(View.VISIBLE);
        Transaction t = this.mTransaction;
        String currency = t.getCurrencyCode();
        if (this.mInitialCurrency.equals(currency)) {
            new UpdateTransaction(this).execute();
        } else {
            if (currency.equals("EUR")) {
                t.setChangeRate(1);
                new UpdateTransaction(this).execute();
            } else {
                new ChangeRateDownloader(this, t.getDateTime(), currency, this)
                        .execute();
            }
        }
    }

    @Override
    public void onDownloadTerminated(ChangeRateDownloader.Result result) {
        if (result.getException() != null) {
            this.findViewById(R.id.editTransactionProgressBar).setVisibility(View.GONE);
            Snackbar.make(this.findViewById(R.id.editTransactionLayout),
                    R.string.rateDownload_error, Snackbar.LENGTH_LONG).show();
            return;
        }
        this.mTransaction.setChangeRate(1.0 / result.getChangeRate());
        new UpdateTransaction(this).execute();
    }

    /**
     * Ask confirmation for transaction deleting
     */
    private void delete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.deleteTransaction_title)
                .setMessage(R.string.deleteTransaction_description)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        boolean result =
                                new TransactionsManager(EditTransactionActivity.this)
                                        .delete(EditTransactionActivity.this.mTransaction);
                        EditTransactionActivity.this.setResult(
                                result ? RESULT_DELETED : RESULT_DELETED_ERROR);
                        EditTransactionActivity.this.finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create()
                .show();
    }

    /**
     * Initialize transaction type spinner
     */
    private void initTransactionTypeSpinner() {
        Spinner spinner = (Spinner) this.findViewById(R.id.editTransaction_input_transactionType);
        this.mTransactionTypeIcon =
                (ImageView) this.findViewById(R.id.editTransaction_icon_transactionType);

        spinner.setAdapter(ArrayAdapter.createFromResource(this,
                R.array.addTransaction_input_transactionType,
                android.R.layout.simple_spinner_dropdown_item
        ));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            private boolean mIsInitialized = false;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int icon = position == 0 ? R.drawable.ic_remove_circle_red_900_24dp
                        : R.drawable.ic_add_circle_green_900_24dp ;
                EditTransactionActivity.this.mTransactionTypeIcon.setImageResource(icon);
                EditTransactionActivity.this.mTransactionType = (position == 0 ? -1 : 1);
                if (!this.mIsInitialized) {
                    this.mIsInitialized = true;
                    return;
                }
                double amount = EditTransactionActivity.this.mTransaction.getAmount();
                amount *= EditTransactionActivity.this.mTransactionType;
                EditTransactionActivity.this.mTransaction.setAmount(amount);
                Log.i(TAG, "transaction amount set to " + amount);
                EditTransactionActivity.this.mChangesSaved = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinner.setSelection(this.mTransaction.getAmount() < 0 ? 0 : 1);
    }
    /**
     * Initialize category spinner
     */
    private void initCategorySpinner() {
        Spinner spinner = (Spinner) this.findViewById(R.id.editTransaction_input_category);

        CategoriesManager categoryHelper = new CategoriesManager(this);
        ArrayList<Category> categories = categoryHelper.select(CategoriesManager.ALL_COLUMNS,
                null, null, null, null, null, null);

        CategorySpinnerAdapter adapter = new CategorySpinnerAdapter(this);
        adapter.setItems(categories);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(adapter);
        spinner.setSelection(
                this.findCategoryPosition(this.mTransaction.getCategory().getId(), categories));
    }
    /**
     * Find position of category with <i>id</i> in <i>list</i>
     * @param id Category id
     * @param list Category list
     * @return Category position or -1 if is not found
     */
    private int findCategoryPosition(int id, ArrayList<Category> list) {
        for (int i = 0; i < list.size(); i++) {
            if (id == list.get(i).getId()) {
                return i;
            }
        }
        return -1;
    }
    /**
     * Initialize amount text input
     */
    private void initAmount() {
        EditText input = (EditText) this.findViewById(R.id.editTransaction_input_amount);
        double amount = Math.abs(this.mTransaction.getAmount());
        input.setText(String.format(Locale.ENGLISH, "%.2f", amount));
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                double a = (double) EditTransactionActivity.this.mTransactionType;
                try {
                    a *= Double.parseDouble(s.toString());
                } catch (NumberFormatException e) {
                    a = 0.0;
                }
                EditTransactionActivity.this.mTransaction.setAmount(a);
                Log.i(TAG, "amount changed to " + a);
                EditTransactionActivity.this.mChangesSaved = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    /**
     * Initialize currency spinner
     */
    private void initCurrencySpinner() {
        Spinner spinner = (Spinner) this.findViewById(R.id.editTransaction_input_currency);
        String[] currencies = this.getResources().getStringArray(R.array.currency_names);

        CurrencySpinnerAdapter adapter = new CurrencySpinnerAdapter(this, currencies);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(adapter);
        this.mInitialCurrency = this.mTransaction.getCurrencyCode();
        spinner.setSelection(CurrencyManager.getIndex(this.mInitialCurrency));
    }
    /**
     * Initialize payment type spinner
     */
    private void initPaymentTypeSpinner() {
        Spinner spinner = (Spinner) this.findViewById(R.id.editTransaction_input_paymentType);
        String[] options = this.getResources().getStringArray(R.array.payment_types);

        PaymentTypeSpinnerAdapter adapter = new PaymentTypeSpinnerAdapter(this, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(adapter);
        spinner.setSelection(this.mTransaction.getPaymentTypeId());
    }
    /**
     * Initialize date text view
     */
    private void initDate() {
        this.mDateText = (TextView) this.findViewById(R.id.editTransaction_input_date);
        final Calendar dateTime = this.mTransaction.getDateTime();
        this.mDateText.setText(DateFormat.format("EEE dd MMM yyyy", dateTime));
        this.mDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(EditTransactionActivity.this,
                        EditTransactionActivity.this, dateTime.get(Calendar.YEAR),
                        dateTime.get(Calendar.MONTH), dateTime.get(Calendar.DAY_OF_MONTH));
                dialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
                dialog.show();
            }
        });
    }
    /**
     * Initialize time text view
     */
    private void initTime() {
        this.mTimeText = (TextView) this.findViewById(R.id.editTransaction_input_time);
        final Calendar dateTime = this.mTransaction.getDateTime();
        this.mTimeText.setText(DateFormat.format("HH:mm", dateTime));
        this.mTimeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TimePickerDialog(EditTransactionActivity.this,
                        EditTransactionActivity.this, dateTime.get(Calendar.HOUR_OF_DAY),
                        dateTime.get(Calendar.MINUTE), true).show();
            }
        });
    }

    /**
     * Initialize notes input
     */
    private void initNotes() {
        EditText notesInput = (EditText) this.findViewById(R.id.editTransaction_input_notes);
        notesInput.setText(this.mTransaction.getNotes());
        notesInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                EditTransactionActivity.this.mTransaction.setNotes(s.toString());
                Log.i(TAG, "notes changed to " + s);
                EditTransactionActivity.this.mChangesSaved = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.mTransaction.setDate(year, month, dayOfMonth);
        this.mDateText.setText(
                DateFormat.format("EEE d MMM yyyy", this.mTransaction.getDateTime()));
        Log.i(TAG, "date changed to " + year + "-" + month + "-" + dayOfMonth);
        this.mChangesSaved = false;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.mTransaction.setTime(hourOfDay, minute);
        this.mTimeText.setText(DateFormat.format("HH:mm", this.mTransaction.getDateTime()));
        Log.i(TAG, "time changed to " + hourOfDay + ":" + minute);
        this.mChangesSaved = false;
    }

    /**
     * Adapter for category spinner
     */
    private class CategorySpinnerAdapter extends ArrayAdapter<Category>
            implements AdapterView.OnItemSelectedListener {

        private boolean mIsInitialized = false;

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
            if (!this.mIsInitialized) {
                this.mIsInitialized = true;
                return;
            }
            Category category = this.getItem(position);
            if (category != null) {
                EditTransactionActivity.this.mTransaction.setCategory(category);
                Log.i(TAG, "selected category " + category.getName());
                EditTransactionActivity.this.mChangesSaved = false;
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

        private boolean mIsInitialized = false;

        CurrencySpinnerAdapter(Context context, String[] options) {
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
            String currency = this.getItem(position);
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
            if (!this.mIsInitialized) {
                this.mIsInitialized = true;
                return;
            }
            String code = CurrencyManager.getCode(position);
            EditTransactionActivity.this.mTransaction.setCurrencyCode(code);
            Log.i(TAG, "currency set to " + code);
            EditTransactionActivity.this.mChangesSaved = false;
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}

    }

    /**
     * Adapter for payment type spinner
     */
    private class PaymentTypeSpinnerAdapter extends ArrayAdapter<String>
            implements AdapterView.OnItemSelectedListener {

        private boolean mIsInitialized = false;

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
            String paymentType = this.getItem(position);
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
            if (!this.mIsInitialized) {
                this.mIsInitialized = true;
                return;
            }
            String paymentType = this.getItem(position);
            if (paymentType != null) {
                EditTransactionActivity.this.mTransaction.setPaymentTypeId(position);
                Log.i(TAG, "selected payment type " + paymentType);
                EditTransactionActivity.this.mChangesSaved = false;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    /**
     * Asynchronous transaction update
     */
    private static class UpdateTransaction extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<EditTransactionActivity> mReference;

        UpdateTransaction(EditTransactionActivity context) {
            this.mReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            EditTransactionActivity activity = this.mReference.get();
            return activity != null && !activity.isFinishing()
                    && new TransactionsManager(activity).update(activity.mTransaction);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            EditTransactionActivity activity = this.mReference.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            if (result) {
                activity.setResult(RESULT_UPDATED);
                activity.mChangesSaved = true;
            }
            activity.findViewById(R.id.editTransactionProgressBar).setVisibility(View.GONE);
            Snackbar.make(activity.findViewById(R.id.editTransactionLayout),
                    result ? R.string.editTransaction_ok : R.string.editTransaction_error,
                    Snackbar.LENGTH_SHORT).show();
        }

    }

}
