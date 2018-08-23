package com.franksacco.wallet;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.FileInputStream;
import java.io.IOException;


/**
 * Import fragment class
 */
public class ImportFragment extends Fragment {

    private static final String TAG = "ImportFragment";

    /**
     * Write intent request code
     */
    private static final int READ_REQUEST_CODE = 130;

    public ImportFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.import_fragment, container, false);

        view.findViewById(R.id.importButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImportFragment.this.searchFile();
            }
        });

        Log.i(TAG, "view created");
        return  view;
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
                    this.readFile(uri);
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Read file content
     * @param uri Selected file uri
     */
    private void readFile(Uri uri) {
        try {
            ParcelFileDescriptor pfd = this.getActivity().getContentResolver()
                    .openFileDescriptor(uri, "r");
            FileInputStream fileInputStream = new FileInputStream(pfd.getFileDescriptor());

            //fileInputStream.read();

            fileInputStream.close();
            pfd.close();
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

}
