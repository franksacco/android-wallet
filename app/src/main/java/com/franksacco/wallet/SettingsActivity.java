package com.franksacco.wallet;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;

import com.franksacco.wallet.database.Database;


/**
 * Application settings activity
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    private static final String TAG = "SettingsActivity";

    /**
     * Preference value change listener
     */
    private static Preference.OnPreferenceChangeListener
            changeListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference to value change listener
     */
    private static void bindPreferenceChange(Preference preference) {
        preference.setOnPreferenceChangeListener(changeListener);

        changeListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        Log.d(TAG, "activity created");
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SettingsFragment extends PreferenceFragment
            implements Preference.OnPreferenceClickListener {

        private static final String TAG = "SettingsFragment";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);
            setHasOptionsMenu(true);

            bindPreferenceChange(findPreference("settings_currency_preferred"));
            findPreference("settings_currency_update").setOnPreferenceClickListener(this);
            findPreference("settings_data_deleteAll").setOnPreferenceClickListener(this);

            SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
            Preference currencyUpdate = findPreference("settings_currency_update");
            currencyUpdate.setSummary(
                    preferences.getString("currency_rates_datetime", "0000-00-00 00:00")
            );

            Log.d(TAG, "fragment created");
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                NavUtils.navigateUpFromSameTask(getActivity());
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();

            switch (key) {
                case "settings_currency_update":
                    AppCompatPreferenceActivity activity =
                            (AppCompatPreferenceActivity) getActivity();
                    Snackbar.make(activity.getListView(),
                            "TODO: download currency rates", Snackbar.LENGTH_SHORT)
                            .show();
                    return true;
                case "settings_data_deleteAll":
                    DialogFragment dialog = new ConfirmDeleteDialog();
                    dialog.show(getFragmentManager(), "confirm_delete");
                    return true;
            }
            return false;
        }
    }

    public static class ConfirmDeleteDialog extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.dialog_confirmDelete_title)
                    .setMessage(R.string.dialog_confirmDelete_description)
                    .setIcon(R.drawable.ic_delete_forever_black_24dp)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteAllData();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null);
            return builder.create();
        }

        private void deleteAllData() {
            AppCompatPreferenceActivity activity = (AppCompatPreferenceActivity) getActivity();
            String message = activity.getResources().getString(R.string.deleteAll_ok);
            try {
                if (!activity.deleteDatabase(Database.DATABASE_NAME)) {
                    throw new Error("Error during database deletion");
                }
                PreferenceManager.getDefaultSharedPreferences(activity.getBaseContext())
                        .edit().clear().apply();
            } catch (Error e) {
                message = e.getMessage();
            }
            Snackbar.make(activity.getListView(), message, Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

}
