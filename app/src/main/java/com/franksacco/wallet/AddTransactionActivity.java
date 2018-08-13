package com.franksacco.wallet;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
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
import com.franksacco.wallet.helpers.CurrencyManager;
import com.franksacco.wallet.helpers.TransactionsManager;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * Add a transaction activity
 */
@SuppressWarnings("RedundantCast")
public class AddTransactionActivity extends AppCompatActivity
        implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private static final String TAG = "AddTransactionActivity";

    private ImageView mTransactionTypeIcon;
    private TextView mDateText;
    private TextView mTimeText;

    private int mTransactionType = -1;
    private Category mCategory = null;
    private int mCurrencyId = 0;
    private int mPaymentType = 0;
    private Calendar mDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.add_transaction_activity);

        Toolbar toolbar = (Toolbar) this.findViewById(R.id.toolbar_main);
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

        Log.d(TAG, "activity created");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.save_transaction, menu);
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
            case R.id.action_save_record:
                EditText amountInput =
                        (EditText) this.findViewById(R.id.addTransaction_input_amount);
                double amount = Double.parseDouble(amountInput.getText().toString());
                CurrencyManager currencyManager = new CurrencyManager(getApplicationContext());
                amount = mTransactionType * currencyManager.convertFrom(mCurrencyId, amount);

                EditText notesInput = (EditText) this.findViewById(R.id.addTransaction_input_notes);
                String notes = notesInput.getText().toString().trim();

                TransactionsManager helper = new TransactionsManager(this);
                Long id = helper.insert(
                        new Transaction(mCategory, amount, mPaymentType, mDateTime, notes));

                this.setResult(id > 0 ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Initialize transaction type spinner
     */
    private void initTransactionTypeSpinner() {
        Spinner spinner = (Spinner) this.findViewById(R.id.addTransaction_input_transactionType);
        mTransactionTypeIcon =
                (ImageView) this.findViewById(R.id.addTransaction_icon_transactionType);

        spinner.setAdapter(ArrayAdapter.createFromResource(this,
                R.array.addTransaction_input_transactionType,
                android.R.layout.simple_spinner_dropdown_item
        ));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mTransactionType = position == 0 ? -1 : 1 ;
                int icon = position == 0 ? R.drawable.ic_remove_circle_red_900_24dp
                        : R.drawable.ic_add_circle_green_900_24dp ;
                AddTransactionActivity.this.mTransactionTypeIcon.setImageResource(icon);
                Log.d(TAG, "selected transaction type " + position);
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
        spinner.setSelection(new CurrencyManager(getApplicationContext()).getPreferredIndex());
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
        Log.d(TAG, "date changed to " + mDateTime.toString());
        this.mDateText.setText(DateFormat.format("EEE dd MMM yyyy", mDateTime));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.mDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        this.mDateTime.set(Calendar.MINUTE, minute);
        Log.d(TAG, "time changed to " + mDateTime.toString());
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
            addAll(items);
            notifyDataSetChanged();
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView,
                                    @NonNull ViewGroup parent) {
            View view = convertView;
            if(view == null) {
                view = (LinearLayout) getLayoutInflater()
                        .inflate(R.layout.item_one_line, parent, false);
            }
            Category category = getItem(position);
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
            Category category = getItem(position);
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
            Category category = getItem(position);
            if (category != null) {
                AddTransactionActivity.this.mCategory = category;
                Log.d(TAG, "selected category " + category.getId());
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
            if(view == null) {
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
            String currency = getItem(position);
            if (currency != null) {
                AddTransactionActivity.this.mCurrencyId = position;
                Log.d(TAG, "selected currency " + currency);
            }
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
                Log.d(TAG, "selected payment type " + paymentType);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {}
    }

    /**
     * Ask confirmation for exiting without saving.
     */
    private void exitWithoutSaving() {
        DialogFragment dialog = new ConfirmExitDialog();
        dialog.show(this.getFragmentManager(), "confirm_exit");
    }

    /**
     * Dialog for exit confirmation
     */
    public static class ConfirmExitDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
            builder.setTitle(R.string.confirmExit_dialog_title)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            NavUtils.navigateUpFromSameTask(getActivity());
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);
            return builder.create();
        }
    }

}
