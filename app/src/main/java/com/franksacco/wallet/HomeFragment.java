package com.franksacco.wallet;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.franksacco.wallet.helpers.DatabaseOpenHelper;
import com.franksacco.wallet.helpers.TransactionsManager;

import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Locale;


/**
 * Home fragment class
 */
@SuppressWarnings("RedundantCast")
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    public HomeFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        FloatingActionButton fabAddTransaction =
                (FloatingActionButton) view.findViewById(R.id.addTransactionFab);
        fabAddTransaction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        HomeFragment.this.getActivity(), AddTransactionActivity.class);
                HomeFragment.this.startActivityForResult(i, AddTransactionActivity.REQUEST_CODE);
            }
        });

        new LoadStatistics(this).execute();

        Log.i(TAG, "view created");
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AddTransactionActivity.REQUEST_CODE) {
            Log.i(TAG, "AddTransaction activity finished with code " + resultCode);
            if (resultCode == AddTransactionActivity.RESULT_SAVED) {
                new LoadStatistics(this).execute();
                if (this.getActivity() != null) {
                    Snackbar.make(this.getActivity().findViewById(R.id.homeLayout),
                            R.string.addTransaction_ok, Snackbar.LENGTH_SHORT).show();
                }
            } else if (resultCode == AddTransactionActivity.RESULT_ERROR) {
                if (this.getActivity() != null) {
                    Snackbar.make(this.getActivity().findViewById(R.id.homeLayout),
                            R.string.addTransaction_error, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Asynchronous statistics values loading
     */
    private static class LoadStatistics extends AsyncTask<Void, Void, String[]> {

        private WeakReference<HomeFragment> mReference;

        private LoadStatistics(HomeFragment context) {
            this.mReference = new WeakReference<>(context);
        }

        @Override
        protected String[] doInBackground(Void... voids) {
            HomeFragment fragment = this.mReference.get();
            if (fragment == null) return null;
            Activity activity = fragment.getActivity();
            if (activity == null || activity.isFinishing()) return null;

            double[] values = new double[7];
            SQLiteDatabase db = DatabaseOpenHelper.getInstance(activity).getReadableDatabase();

            Cursor cursor1 = db.rawQuery("SELECT TOTAL(" + TransactionsManager.AMOUNT_COL
                    + "*" + TransactionsManager.CHANGE_RATE_COL + ") FROM "
                    + TransactionsManager.TABLE_NAME + ";", null);
            cursor1.moveToFirst();
            values[0] = cursor1.getDouble(0);
            cursor1.close();

            Cursor cursor2 = db.rawQuery("SELECT TOTAL(" + TransactionsManager.AMOUNT_COL
                    + "*" + TransactionsManager.CHANGE_RATE_COL + ") FROM "
                    + TransactionsManager.TABLE_NAME + " WHERE "
                    + TransactionsManager.AMOUNT_COL + " >= ?;", new String[]{"0"});
            cursor2.moveToFirst();
            values[1] = cursor2.getDouble(0);
            cursor2.close();

            Cursor cursor3 = db.rawQuery("SELECT -TOTAL(" + TransactionsManager.AMOUNT_COL
                    + "*" + TransactionsManager.CHANGE_RATE_COL + ") FROM "
                    + TransactionsManager.TABLE_NAME + " WHERE "
                    + TransactionsManager.AMOUNT_COL + " < ?;", new String[]{"0"});
            cursor3.moveToFirst();
            values[2] = cursor3.getDouble(0);
            cursor3.close();

            Calendar today = Calendar.getInstance();
            Cursor cursor4 = db.rawQuery("SELECT -TOTAL(" + TransactionsManager.AMOUNT_COL
                            + "*" + TransactionsManager.CHANGE_RATE_COL + ") FROM "
                            + TransactionsManager.TABLE_NAME + " WHERE "
                            + TransactionsManager.AMOUNT_COL + " < ? AND "
                            + TransactionsManager.DATETIME_COL + " >= ? AND "
                            + TransactionsManager.DATETIME_COL + " <= ?;",
                    new String[]{"0",
                            DateFormat.format("yyyy-MM-dd 00:00", today).toString(),
                            DateFormat.format("yyyy-MM-dd 23:59", today).toString()});
            cursor4.moveToFirst();
            values[3] = cursor4.getDouble(0);
            cursor4.close();

            Calendar yesterday = (Calendar) today.clone();
            yesterday.add(Calendar.DAY_OF_MONTH, -1);
            Cursor cursor5 = db.rawQuery("SELECT -TOTAL(" + TransactionsManager.AMOUNT_COL
                            + "*" + TransactionsManager.CHANGE_RATE_COL + ") FROM "
                            + TransactionsManager.TABLE_NAME + " WHERE "
                            + TransactionsManager.AMOUNT_COL + " < ? AND "
                            + TransactionsManager.DATETIME_COL + " >= ? AND "
                            + TransactionsManager.DATETIME_COL + " <= ?;",
                    new String[]{"0",
                            DateFormat.format("yyyy-MM-dd 00:00", yesterday).toString(),
                            DateFormat.format("yyyy-MM-dd 23:59", yesterday).toString()});
            cursor5.moveToFirst();
            values[4] = cursor5.getDouble(0);
            cursor5.close();

            Calendar oneWeekAgo = (Calendar) today.clone();
            oneWeekAgo.add(Calendar.WEEK_OF_YEAR, -1);
            Cursor cursor6 = db.rawQuery("SELECT -TOTAL(" + TransactionsManager.AMOUNT_COL
                            + "*" + TransactionsManager.CHANGE_RATE_COL + ") FROM "
                            + TransactionsManager.TABLE_NAME + " WHERE "
                            + TransactionsManager.AMOUNT_COL + " < ? AND "
                            + TransactionsManager.DATETIME_COL + " >= ? AND "
                            + TransactionsManager.DATETIME_COL + " <= ?;",
                    new String[]{"0",
                            DateFormat.format("yyyy-MM-dd 00:00", oneWeekAgo).toString(),
                            DateFormat.format("yyyy-MM-dd 23:59", today).toString()});
            cursor6.moveToFirst();
            values[5] = cursor6.getDouble(0);
            cursor6.close();

            Calendar oneMonthAgo = (Calendar) today.clone();
            oneMonthAgo.add(Calendar.MONTH, -1);
            Cursor cursor7 = db.rawQuery("SELECT -TOTAL(" + TransactionsManager.AMOUNT_COL
                            + "*" + TransactionsManager.CHANGE_RATE_COL + ") FROM "
                            + TransactionsManager.TABLE_NAME + " WHERE "
                            + TransactionsManager.AMOUNT_COL + " < ? AND "
                            + TransactionsManager.DATETIME_COL + " >= ? AND "
                            + TransactionsManager.DATETIME_COL + " <= ?;",
                    new String[]{"0",
                            DateFormat.format("yyyy-MM-dd 00:00", oneMonthAgo).toString(),
                            DateFormat.format("yyyy-MM-dd 23:59", today).toString()});
            cursor7.moveToFirst();
            values[6] = cursor7.getDouble(0);
            cursor7.close();

            db.close();

            String[] outValues = new String[7];
            for (int i = 0; i < 7; i++) {
                outValues[i] = String.format(Locale.getDefault(), "%.2f", values[i]) + " €";
            }
            return outValues;
        }

        @Override
        protected void onPostExecute(String[] values) {
            HomeFragment fragment = this.mReference.get();
            if (values == null || fragment == null) return;
            Activity activity = fragment.getActivity();
            if (activity == null || activity.isFinishing()) return;

            ((TextView) activity.findViewById(R.id.balance)).setText(values[0]);
            ((TextView) activity.findViewById(R.id.balance_in)).setText(values[1]);
            ((TextView) activity.findViewById(R.id.balance_out)).setText(values[2]);

            ((TextView) activity.findViewById(R.id.expense_today)).setText(values[3]);
            ((TextView) activity.findViewById(R.id.expense_yesterday)).setText(values[4]);
            ((TextView) activity.findViewById(R.id.expense_this_week)).setText(values[5]);
            ((TextView) activity.findViewById(R.id.expense_this_month)).setText(values[6]);
        }

    }

}
