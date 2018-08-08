package com.franksacco.wallet;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Add a transaction activity.
 */
@SuppressWarnings("RedundantCast")
public class AddTransactionActivity extends AppCompatActivity {

    private static final String TAG = "AddTransactionActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_add_transaction);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
        }

        Log.d(TAG, "activity created");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_transaction, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        exitWithoutSaving();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                exitWithoutSaving();
                return true;
            case R.id.action_save_record:
                // todo save record in database
                Toast.makeText(this, R.string.save, Toast.LENGTH_SHORT).show();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Ask confirmation for exiting without saving.
     */
    private void exitWithoutSaving() {
        DialogFragment dialog = new ConfirmExitDialog();
        dialog.show(getFragmentManager(), "confirm_exit");
    }

    public static class ConfirmExitDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
