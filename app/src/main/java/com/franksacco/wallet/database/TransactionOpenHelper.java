package com.franksacco.wallet.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Helper class for transactions table in database.
 */
public class TransactionOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "transaction_helper";

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "database.db";

    private static final String TABLE_NAME = "transactions";

    private static final String ID_COL = "id";
    private static final String DATETIME_COL = "datetime";

    private static final String DATABASE_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " ("
            + ID_COL + " INTEGER PRIMARY KEY NOT NULL"
            + ");";

    public TransactionOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d(TAG, "constructor launched");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate launched");
        db.execSQL(DATABASE_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade launched");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(DATABASE_CREATE_TABLE);
    }

}
