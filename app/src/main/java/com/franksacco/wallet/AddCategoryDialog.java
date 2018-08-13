package com.franksacco.wallet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;


/**
 * Add category dialog fragment
 */
@SuppressWarnings("RedundantCast")
public class AddCategoryDialog extends DialogFragment {

    private static final String TAG = "AddCategoryDialog";

    public interface AddCategoryDialogListener {
        void onDialogPositiveClick(DialogFragment dialog, String inputName);
    }

    private AddCategoryDialogListener mListener;

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        try {
            mListener = (AddCategoryDialogListener) getTargetFragment();
            if (mListener == null) {
                throw new ClassCastException();
            }
        } catch (ClassCastException e) {
            throw new ClassCastException("Parent must implement AddCategoryDialogListener");
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        LinearLayout view = (LinearLayout) inflater.inflate(
                R.layout.add_category_dialog, null);

        final TextInputEditText input =
                (TextInputEditText) view.findViewById(R.id.addCategory_input_name);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.addCategory_dialog_title)
                .setView(view)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(
                                AddCategoryDialog.this, input.getText().toString());
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        input.setText("");
                    }
                });
        Log.d(TAG, "dialog created");
        return builder.create();
    }

}
