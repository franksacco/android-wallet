package com.franksacco.wallet;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.franksacco.wallet.helpers.CSVReader;
import com.franksacco.wallet.helpers.DatabaseOpenHelper;
import com.franksacco.wallet.helpers.TransactionsManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;


/**
 * Import fragment class
 */
public class ImportFragment extends Fragment {

    private static final String TAG = "ImportFragment";

    /**
     * Write intent request code
     */
    private static final int READ_REQUEST_CODE = 140;
    /**
     * Request reading permissions code
     */
    private static final int PERMISSION_READ_STORAGE = 141;

    private Button mButton;
    private ProgressBar mProgressBar;

    public ImportFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.import_fragment, container, false);

        this.mProgressBar = view.findViewById(R.id.importProgressBar);
        this.mButton = view.findViewById(R.id.importButton);
        this.mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImportFragment.this.checkPermission();
            }
        });

        Log.i(TAG, "view created");
        return  view;
    }

    /**
     * Check read storage permission
     */
    private void checkPermission() {
        if (this.getActivity() == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            ImportFragment.this.searchFile();
        } else {
            this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    PERMISSION_READ_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_READ_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                this.searchFile();
            } else {
                new AlertDialog.Builder(this.getActivity())
                        .setMessage(R.string.import_no_permission)
                        .setPositiveButton(android.R.string.ok, null)
                        .create().show();
            }
        }
    }

    /**
     * Open a document with Storage Access Framework
     */
    private void searchFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        this.startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    Log.i(TAG, "Uri: " + uri.toString());
                    new Import(this).execute(uri);
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Asynchronous task to read data from importing file and insert it in database
     */
    private static class Import extends AsyncTask<Uri, Void, Boolean> {

        private WeakReference<ImportFragment> mFragment;

        private Import(ImportFragment fragment) {
            this.mFragment = new WeakReference<>(fragment);
        }

        @Override
        protected void onPreExecute() {
            ImportFragment fragment = this.mFragment.get();
            if (fragment != null) {
                fragment.mButton.setVisibility(View.GONE);
                fragment.mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(Uri... uris) {
            ImportFragment fragment = this.mFragment.get();
            if (fragment == null || fragment.getActivity() == null
                    || fragment.getActivity().isFinishing() || uris == null || uris.length != 1) {
                return false;
            }
            Uri uri = uris[0];
            try {
                ParcelFileDescriptor pfd = fragment.getActivity().getContentResolver()
                        .openFileDescriptor(uri, "r");
                if (pfd != null) {
                    FileInputStream stream = new FileInputStream(pfd.getFileDescriptor());
                    this.readFile(stream, fragment);
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
         * Read file data and insert it in database
         * @param stream File input stream
         * @param fragment Reference to this fragment
         */
        private void readFile(FileInputStream stream, ImportFragment fragment) throws IOException {
            InputStreamReader reader = new InputStreamReader(stream);
            SQLiteDatabase db = DatabaseOpenHelper.getInstance(fragment.getActivity())
                    .getWritableDatabase();
            ContentValues cv = new ContentValues();

            String[] row;
            CSVReader csvReader = new CSVReader(reader);
            while ((row = csvReader.readNext()) != null) {
                cv.clear();
                try {
                    cv.put(TransactionsManager.DATETIME_COL, row[0]);
                    cv.put(TransactionsManager.CATEGORY_COL, Integer.parseInt(row[1]));
                    cv.put(TransactionsManager.AMOUNT_COL,  Double.parseDouble(row[2]));
                    cv.put(TransactionsManager.CURRENCY_COL, row[3]);
                    cv.put(TransactionsManager.CHANGE_RATE_COL, Double.parseDouble(row[4]));
                    cv.put(TransactionsManager.NOTES_COL, row[5]);
                    cv.put(TransactionsManager.PAYMENT_TYPE_COL, Integer.parseInt(row[6]));

                    db.insert(TransactionsManager.TABLE_NAME, null, cv);
                } catch (IndexOutOfBoundsException | NumberFormatException | SQLiteException e) {
                    Log.e(TAG, e.toString());
                }
            }
            db.close();
            reader.close();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            ImportFragment fragment = this.mFragment.get();
            if (fragment != null) {
                fragment.mButton.setVisibility(View.VISIBLE);
                fragment.mProgressBar.setVisibility(View.GONE);
                if (fragment.getView() != null) {
                    Snackbar.make(fragment.getView().findViewById(R.id.importLayout),
                            result ? R.string.import_ok : R.string.import_error,
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        }

    }

}
