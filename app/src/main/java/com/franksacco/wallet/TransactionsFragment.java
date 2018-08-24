package com.franksacco.wallet;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import com.franksacco.wallet.adapters.TransactionsAdapter;
import com.franksacco.wallet.entities.Transaction;
import com.franksacco.wallet.helpers.TransactionsManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Transaction fragment view
 */
@SuppressWarnings("RedundantCast")
public class TransactionsFragment extends Fragment
        implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "TransactionsFragment";

    /**
     * Transactions adapter for recycler view
     */
    private TransactionsAdapter mAdapter;
    /**
     * Current date
     */
    private Calendar mDate = Calendar.getInstance();

    private TextView mDateTextView;

    public TransactionsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.transactions_fragment, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.transactionRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);

        this.mAdapter = new TransactionsAdapter(this.getActivity(),
                new TransactionsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(TransactionsAdapter.ViewHolder item) {
                Intent i = new Intent(TransactionsFragment.this.getActivity(),
                        EditTransactionActivity.class);
                i.putExtra(EditTransactionActivity.TRANSACTION_ID, item.getItemId());
                TransactionsFragment.this.startActivityForResult(
                        i, EditTransactionActivity.REQUEST_CODE);
            }
        });
        recyclerView.setAdapter(this.mAdapter);

        this.initControlBar(view);
        new LoadTransactions(this).execute();

        Log.i(TAG, "view created");
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EditTransactionActivity.REQUEST_CODE) {
            Log.i(TAG, "EditTransaction activity finished with code " + resultCode);
            if (resultCode == EditTransactionActivity.RESULT_UPDATED) {
                new LoadTransactions(this).execute();
            } else if (resultCode == EditTransactionActivity.RESULT_DELETED) {
                new LoadTransactions(this).execute();
                if (this.getActivity() != null) {
                    Snackbar.make(this.getActivity().findViewById(R.id.transactionsLayout),
                            R.string.deleteTransaction_ok, Snackbar.LENGTH_SHORT).show();
                }
            } else if (resultCode == EditTransactionActivity.RESULT_DELETED_ERROR) {
                if (this.getActivity() != null) {
                    Snackbar.make(this.getActivity().findViewById(R.id.transactionsLayout),
                            R.string.deleteTransaction_error, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Initialize control bar and set click listeners
     */
    private void initControlBar(View view) {
        this.mDateTextView = (TextView) view.findViewById(R.id.transactionsSelectDay);
        this.updateDateTextView();
        this.mDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TransactionsFragment.this.getActivity() == null) {
                    return;
                }
                new DatePickerDialog(TransactionsFragment.this.getActivity(),
                                TransactionsFragment.this,
                                TransactionsFragment.this.mDate.get(Calendar.YEAR),
                                TransactionsFragment.this.mDate.get(Calendar.MONTH),
                                TransactionsFragment.this.mDate.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });
        view.findViewById(R.id.transactionsPreviousDay)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransactionsFragment.this.mDate.add(Calendar.DAY_OF_WEEK, -1);
                TransactionsFragment.this.updateDateTextView();
                new LoadTransactions(TransactionsFragment.this).execute();
            }
        });
        view.findViewById(R.id.transactionsNextDay)
                .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TransactionsFragment.this.mDate.add(Calendar.DAY_OF_WEEK, 1);
                TransactionsFragment.this.updateDateTextView();
                new LoadTransactions(TransactionsFragment.this).execute();
            }
        });
    }

    /**
     * Update date showed in control bar
     */
    private void updateDateTextView() {
        this.mDateTextView.setText(DateFormat.format("EEE dd MMM yyyy", this.mDate));
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        this.mDate.set(year, month, dayOfMonth);
        this.updateDateTextView();
        new LoadTransactions(this).execute();
    }

    /**
     * Asynchronous transactions loading
     */
    private static class LoadTransactions extends AsyncTask<Void, Void, ArrayList<Transaction>> {

        private WeakReference<TransactionsFragment> mReference;

        private LoadTransactions(TransactionsFragment context) {
            this.mReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<Transaction> doInBackground(Void... voids) {
            TransactionsFragment fragment = this.mReference.get();
            if (fragment == null) return null;
            Activity activity = fragment.getActivity();
            if (activity == null || activity.isFinishing()) return null;

            TransactionsManager manager = new TransactionsManager(activity);
            return manager.select(TransactionsManager.DATETIME_COL + " >= ? AND "
                            + TransactionsManager.DATETIME_COL + " <= ?",
                    null, null,
                    TransactionsManager.DATETIME_COL + " ASC", null,
                    new String[]{
                        DateFormat.format("yyyy-MM-dd 00:00", fragment.mDate).toString(),
                        DateFormat.format("yyyy-MM-dd 23:59", fragment.mDate).toString()});
        }

        @Override
        protected void onPostExecute(ArrayList<Transaction> transactions) {
            TransactionsFragment fragment = this.mReference.get();
            if (fragment != null) {
                fragment.mAdapter.setItems(transactions);
            }
        }

    }

}
