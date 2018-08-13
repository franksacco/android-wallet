package com.franksacco.wallet.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateFormat;
import android.util.Log;

import com.franksacco.wallet.entities.Category;
import com.franksacco.wallet.entities.Transaction;

import java.util.ArrayList;
import java.util.Calendar;


/**
 * Helper class for transactions table in database
 */
public class TransactionsManager {

    private static final String TAG = "TransactionsManager";

    public static final String TABLE_NAME = "transactions";

    public static final String ID_COL = "id";
    public static final String CATEGORY_COL = "category";
    public static final String AMOUNT_COL = "amount";
    public static final String PAYMENT_TYPE_COL = "paymentType";
    public static final String DATETIME_COL = "datetime";
    public static final String NOTES_COL = "notes";

    /**
     * Instance of database helper
     */
    private DatabaseOpenHelper db;

    /**
     * Transactions manager initialization
     * @param context Application context
     */
    public TransactionsManager(Context context) {
        this.db = DatabaseOpenHelper.getInstance(context);
        Log.d(TAG, "created");
    }

    /**
     * Table creation
     * @param db Database reference
     */
    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                + CATEGORY_COL + " INTEGER NOT NULL, "
                + AMOUNT_COL + " DOUBLE NOT NULL, "
                + PAYMENT_TYPE_COL + " INTEGER NOT NULL, "
                + DATETIME_COL + " TEXT NOT NULL, "
                + NOTES_COL + " TEXT NOT NULL, "
                + "CONSTRAINT fk_" + CATEGORY_COL + " FOREIGN KEY (" + CATEGORY_COL + ")"
                + "REFERENCES " + CategoriesManager.TABLE_NAME + "(" + CategoriesManager.ID_COL + ")"
                + ");");
        Log.d(TAG, "table created");
    }

    /**
     * Table upgrade
     * @param db Database reference
     */
    public static void onUpgrade(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        Log.d(TAG, "database upgrade - table dropped");
        onCreate(db);
    }

    /**
     * Transform actual row pointed by <i>cursor</i> into a transaction object
     * @param cursor Cursor from a SELECT query
     * @return Transaction object
     */
    private Transaction cursorToObject(Cursor cursor) {
        Transaction transaction = new Transaction();
        transaction.setId(cursor.getInt(0));
        transaction.setAmount(cursor.getDouble(1));
        transaction.setPaymentTypeId(cursor.getInt(2));
        transaction.setDateTime(cursor.getString(3));
        transaction.setNotes(cursor.getString(4));
        transaction.setCategory(new Category(cursor.getInt(5),
                cursor.getString(6), cursor.getString(7)));
        return transaction;
    }

    /**
     * Insert a single transaction in database
     * @param transaction Transaction object to be inserted
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insert(Transaction transaction) {
        ContentValues values = new ContentValues();
        values.put(CATEGORY_COL, transaction.getCategory().getId());
        values.put(AMOUNT_COL, transaction.getAmount());
        values.put(PAYMENT_TYPE_COL, transaction.getPaymentTypeId());
        Calendar dateTime = transaction.getDateTime();
        values.put(DATETIME_COL, DateFormat.format("yyyy-MM-dd HH:mm", dateTime).toString());
        values.put(NOTES_COL, transaction.getNotes());

        SQLiteDatabase db = this.db.getWritableDatabase();
        long id = db.insert(TABLE_NAME, null, values);
        Log.d(TAG, "inserted transaction with id " + id);
        db.close();
        return id;
    }

    /**
     * Retrieve transaction from database
     * @param where Where clause
     * @param groupBy Group by clause
     * @param having Having clause
     * @param orderBy Order by clause
     * @param limit Limit clause
     * @param params Parameter values used in query
     * @return Transaction list
     */
    public ArrayList<Transaction> select(String where, String groupBy, String having,
                                         String orderBy, String limit, String[] params) {
        String query = this.prepareSelectQuery(where, groupBy, having, orderBy, limit);

        SQLiteDatabase db = this.db.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, params);
        cursor.moveToFirst();

        ArrayList<Transaction> list = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            list.add(this.cursorToObject(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return list;
    }

    /**
     * Create select raw query to join this entity with categories
     * @param where Where clause
     * @param groupBy Group by clause
     * @param having Having clause
     * @param orderBy Order by clause
     * @param limit Limit clause
     * @return Sql query to perform
     */
    private String prepareSelectQuery(String where, String groupBy, String having,
                                      String orderBy, String limit) {
        String query = "SELECT T." +ID_COL+ ", T." + AMOUNT_COL + ", T." + PAYMENT_TYPE_COL + ", T."
                + DATETIME_COL + ", T." + NOTES_COL + ", C." + CategoriesManager.ID_COL + ", C."
                + CategoriesManager.ICON_COL+", C." + CategoriesManager.NAME_COL + " FROM "
                + TABLE_NAME + " AS T INNER JOIN " + CategoriesManager.TABLE_NAME + " AS C"
                + " ON T." + CATEGORY_COL + " = C." + CategoriesManager.ID_COL;
        if (where != null) {
            query += " WHERE " + where;
        }
        if (groupBy != null) {
            query += " GROUP BY " + groupBy;
            if (having != null) {
                query += " HAVING " + having;
            }
        }
        if (orderBy != null) {
            query += " ORDER BY " + orderBy;
        }
        if (limit != null) {
            query += " LIMIT " + limit;
        }
        return query + ";";
    }

}
