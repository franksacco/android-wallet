package com.franksacco.wallet.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Helper class to manage database creation and version management
 */
public class DatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseOpenHelper";

    /**
     * Database version
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Database file name
     */
    private static final String DATABASE_NAME = "database.db";

    /**
     * Unique instance of this class
     */
    private static DatabaseOpenHelper instance;

    /**
     * Get unique instance of DatabaseOpenHelper
     * @return DatabaseOpenHelper instance
     */
    public static DatabaseOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseOpenHelper(context);
        }
        return instance;
    }

    /**
     * Private constructor for singleton pattern
     * @param context Application context
     */
    private DatabaseOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.i(TAG, "instance created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        CategoriesManager.onCreate(db);
        TransactionsManager.onCreate(db);

        Log.i(TAG, "=== database created ===");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        CategoriesManager.onUpgrade(db);
        TransactionsManager.onUpgrade(db);

        Log.i(TAG, "=== database upgraded ===");
    }

}
