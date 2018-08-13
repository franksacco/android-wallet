package com.franksacco.wallet;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

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

    private Calendar mDate = Calendar.getInstance();
    private TextView mDateTextView;

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
                // todo create edit transaction activity and connect
                Toast.makeText(TransactionsFragment.this.getActivity(),
                        "Clicked item " + item.getAdapterPosition(),
                        Toast.LENGTH_SHORT).show();
            }
        });
        recyclerView.setAdapter(this.mAdapter);

        this.initControlBar(view);
        new LoadTransactions(this).execute();

        Log.d(TAG, "view created");
        return view;
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
                new DatePickerDialog(TransactionsFragment.this.getActivity(),
                        TransactionsFragment.this,
                        TransactionsFragment.this.mDate.get(Calendar.YEAR),
                        TransactionsFragment.this.mDate.get(Calendar.MONTH),
                        TransactionsFragment.this.mDate.get(Calendar.DAY_OF_MONTH)).show();
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

        LoadTransactions(TransactionsFragment context) {
            this.mReference = new WeakReference<>(context);
        }

        @Override
        protected ArrayList<Transaction> doInBackground(Void... voids) {
            TransactionsFragment fragment = this.mReference.get();
            if (fragment == null) return null;
            Activity activity = fragment.getActivity();
            if (activity.isFinishing()) return null;

            TransactionsManager manager = new TransactionsManager(activity.getApplicationContext());
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
