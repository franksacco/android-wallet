package com.franksacco.wallet;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.franksacco.wallet.entities.Transaction;
import com.franksacco.wallet.helpers.TransactionsManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Import fragment class
 */
@SuppressWarnings("RedundantCast")
public class ExportFragment extends Fragment {

    private static final String TAG = "ExportFragment";

    /**
     * Write intent request code
     */
    private static final int WRITE_REQUEST_CODE = 130;

    /**
     * Starting date of exporting period
     */
    private Calendar mStartDate = Calendar.getInstance();
    /**
     * Ending date of exporting period
     */
    private Calendar mEndDate = Calendar.getInstance();
    /**
     * Says if export all transactions in database
     */
    private boolean mAllData = false;

    private Button mButton;
    private ProgressBar mProgressBar;

    public ExportFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.export_fragment, container, false);

        this.mProgressBar = view.findViewById(R.id.exportProgressBar);
        this.mButton = view.findViewById(R.id.exportButton);
        this.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View clickedView) {
                if (ExportFragment.this.mStartDate.after(ExportFragment.this.mEndDate)) {
                    Snackbar.make(view.findViewById(R.id.exportLayout),
                            R.string.export_errorDates, Snackbar.LENGTH_SHORT).show();
                    return;
                }
                ExportFragment.this.createFile();
            }
        });
        this.mStartDate.add(Calendar.MONTH, -1);
        this.initStartDate(view);
        this.initEndDate(view);
        this.initCheckBox(view);

        Log.i(TAG, "view created");
        return view;
    }

    /**
     * Initialize starting date picker
     * @param view Fragment view
     */
    private void initStartDate(View view) {
        final TextView startDateView = (TextView) view.findViewById(R.id.exportStartDateInput);
        startDateView.setText(DateFormat.format("EEEE d MMMM yyyy", this.mStartDate));

        final DatePickerDialog.OnDateSetListener listener =
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        ExportFragment.this.mStartDate.set(year, month, dayOfMonth);
                        startDateView.setText(
                                DateFormat.format("EEEE d MMMM yyyy",
                                        ExportFragment.this.mStartDate));
                        Log.i(TAG, "starting date: " + year + "-" + month + "-" + dayOfMonth);
                    }
                };
        startDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ExportFragment.this.getActivity(), listener,
                        ExportFragment.this.mStartDate.get(Calendar.YEAR),
                        ExportFragment.this.mStartDate.get(Calendar.MONTH),
                        ExportFragment.this.mStartDate.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });
    }
    /**
     * Initialize ending date picker
     * @param view Fragment view
     */
    private void initEndDate(View view) {
        final TextView endDateView = (TextView) view.findViewById(R.id.exportEndDateInput);
        endDateView.setText(DateFormat.format("EEEE d MMMM yyyy", this.mEndDate));

        final DatePickerDialog.OnDateSetListener listener =
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        ExportFragment.this.mEndDate.set(year, month, dayOfMonth);
                        endDateView.setText(
                                DateFormat.format("EEEE d MMMM yyyy",
                                        ExportFragment.this.mEndDate));
                        Log.i(TAG, "ending date: " + year + "-" + month + "-" + dayOfMonth);
                    }
                };
        endDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(ExportFragment.this.getActivity(), listener,
                        ExportFragment.this.mEndDate.get(Calendar.YEAR),
                        ExportFragment.this.mEndDate.get(Calendar.MONTH),
                        ExportFragment.this.mEndDate.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });
    }
    /**
     * Initialize checkbox
     * @param view Fragment view
     */
    private void initCheckBox(View view) {
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.exportCheckBox);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ExportFragment.this.mAllData = isChecked;
            }
        });
    }

    /**
     * Create a new document with Storage Access Framework
     */
    private void createFile() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, "MyWalletData.csv");
        this.startActivityForResult(intent, WRITE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WRITE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    Log.i(TAG, "Uri: " + uri.toString());
                    new Export(this).execute(uri);
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Asynchronous task to retrieve data from database and write it on exporting file
     */
    private static class Export extends AsyncTask<Uri, Void, Boolean> {

        private WeakReference<ExportFragment> mFragment;

        private Export(ExportFragment fragment) {
            this.mFragment = new WeakReference<>(fragment);
        }

        @Override
        protected void onPreExecute() {
            ExportFragment fragment = this.mFragment.get();
            if (fragment != null) {
                fragment.mButton.setVisibility(View.GONE);
                fragment.mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(Uri... uris) {
            ExportFragment fragment = this.mFragment.get();
            if (fragment == null || fragment.getActivity().isFinishing()
                    || uris == null || uris.length != 1) {
                return false;
            }
            Uri uri = uris[0];
            try {
                ParcelFileDescriptor pfd = fragment.getActivity().getContentResolver()
                        .openFileDescriptor(uri, "w");
                if (pfd != null) {
                    FileOutputStream stream = new FileOutputStream(pfd.getFileDescriptor());
                    this.writeData(stream, fragment);
                    stream.close();
                    pfd.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.toString());
                return false;
            }
            return true;
        }

        /**
         * Retrieve data from database and write it on stream
         * @param stream File output stream
         * @param fragment Reference to this fragment
         * @throws IOException If an error occur with stream
         */
        private void writeData(FileOutputStream stream, ExportFragment fragment)
                throws IOException {
            String where = null;
            String[] params = null;
            if (!fragment.mAllData) {
                where = TransactionsManager.DATETIME_COL + " >= ? AND " +
                        TransactionsManager.DATETIME_COL + " <= ?";
                params = new String[]{
                        DateFormat.format("yyyy-MM-dd 00:00", fragment.mStartDate).toString(),
                        DateFormat.format("yyyy-MM-dd 23:59", fragment.mEndDate).toString()
                };
            }
            TransactionsManager tm = new TransactionsManager(fragment.getActivity());
            ArrayList<Transaction> objects = tm.select(where, null, null,
                    TransactionsManager.DATETIME_COL + " ASC", null, params);
            Log.i(TAG, "exporting " + objects.size() + " transactions");

            OutputStreamWriter writer = new OutputStreamWriter(stream);
            for (Transaction t: objects) {
                writer.write(DateFormat.format(
                        "yyyy-MM-dd HH:mm:ss", t.getDateTime()) + ",");
                writer.write(t.getCategory().getId() + ",");
                writer.write(t.getAmount() + ",");
                writer.write(t.getCurrencyCode() + ",");
                writer.write(t.getChangeRate() + ",");
                writer.write("\"" + t.getNotes() + "\",");
                writer.write(t.getPaymentTypeId() + "\n");
            }
            writer.close();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            ExportFragment fragment = this.mFragment.get();
            if (fragment != null) {
                fragment.mButton.setVisibility(View.VISIBLE);
                fragment.mProgressBar.setVisibility(View.GONE);
                if (fragment.getView() != null) {
                    Snackbar.make(fragment.getView().findViewById(R.id.exportLayout),
                            result ? R.string.export_ok : R.string.export_error,
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        }

    }

}
