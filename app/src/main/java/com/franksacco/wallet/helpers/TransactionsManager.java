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
@SuppressWarnings("WeakerAccess")
public class TransactionsManager {

    private static final String TAG = "TransactionsManager";

    public static final String TABLE_NAME = "transactions";

    public static final String ID_COL = "id";
    public static final String CATEGORY_COL = "category";
    public static final String DATETIME_COL = "datetime";
    public static final String AMOUNT_COL = "amount";
    public static final String CURRENCY_COL = "currency";
    public static final String CHANGE_RATE_COL = "changeRate";
    public static final String NOTES_COL = "notes";
    public static final String PAYMENT_TYPE_COL = "paymentType";

    /**
     * Instance of database helper
     */
    private DatabaseOpenHelper mDatabaseHelper;

    /**
     * Transactions manager initialization
     * @param context Application context
     */
    public TransactionsManager(Context context) {
        this.mDatabaseHelper = DatabaseOpenHelper.getInstance(context);
    }

    /**
     * Table creation
     * @param db Database reference
     */
    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DATETIME_COL + " TEXT NOT NULL, "
                + CATEGORY_COL + " INTEGER NOT NULL, "
                + AMOUNT_COL + " DOUBLE NOT NULL, "
                + CURRENCY_COL + " CHAR(3) NOT NULL, "
                + CHANGE_RATE_COL + " DOUBLE NOT NULL, "
                + NOTES_COL + " TEXT NOT NULL, "
                + PAYMENT_TYPE_COL + " INTEGER NOT NULL, "
                + "FOREIGN KEY (" + CATEGORY_COL + ") REFERENCES " + CategoriesManager.TABLE_NAME
                + " (" + CategoriesManager.ID_COL + ") ON DELETE NO ACTION ON UPDATE CASCADE);");
        Log.i(TAG, "table created");
    }

    /**
     * Table upgrade
     * @param db Database reference
     */
    public static void onUpgrade(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        Log.i(TAG, "database upgrade - table dropped");
        onCreate(db);
    }

    /**
     * Transform actual row pointed by <i>c</i> into a transaction object
     * @param c Cursor from a SELECT query
     * @return Transaction object
     */
    private Transaction cursorToObject(Cursor c) {
        Transaction t = new Transaction();
        t.setId(c.getInt(0));
        t.setDateTime(c.getString(1));
        t.setCategory(new Category(c.getInt(2),
                c.getString(3), c.getString(4)));
        t.setAmount(c.getDouble(5));
        t.setCurrencyCode(c.getString(6));
        t.setChangeRate(c.getDouble(7));
        t.setNotes(c.getString(8));
        t.setPaymentTypeId(c.getInt(9));
        return t;
    }

    /**
     * Insert a single transaction in database
     * @param t Transaction object to be inserted
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insert(Transaction t) {
        ContentValues values = new ContentValues();
        Calendar dateTime = t.getDateTime();
        values.put(DATETIME_COL, DateFormat.format("yyyy-MM-dd HH:mm", dateTime).toString());
        values.put(CATEGORY_COL, t.getCategory().getId());
        values.put(AMOUNT_COL, t.getAmount());
        values.put(CURRENCY_COL, t.getCurrencyCode());
        values.put(CHANGE_RATE_COL, t.getChangeRate());
        values.put(NOTES_COL, t.getNotes());
        values.put(PAYMENT_TYPE_COL, t.getPaymentTypeId());

        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        long id = db.insert(TABLE_NAME, null, values);
        Log.i(TAG, "inserted transaction with id " + id);
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

        SQLiteDatabase db = this.mDatabaseHelper.getReadableDatabase();
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
        String query = "SELECT T." + ID_COL + ", T." + DATETIME_COL + ", C."
                + CategoriesManager.ID_COL + ", C." + CategoriesManager.ICON_COL + ", C."
                + CategoriesManager.NAME_COL + ",  T." + AMOUNT_COL + ", T." + CURRENCY_COL + ", T."
                + CHANGE_RATE_COL + ", T." + NOTES_COL + ", T." + PAYMENT_TYPE_COL
                + " FROM " + TABLE_NAME + " AS T INNER JOIN " + CategoriesManager.TABLE_NAME
                + " AS C ON T." + CATEGORY_COL + " = C." + CategoriesManager.ID_COL;
        if (where != null) query += " WHERE " + where;
        if (groupBy != null) {
            query += " GROUP BY " + groupBy;
            if (having != null) query += " HAVING " + having;
        }
        if (orderBy != null) query += " ORDER BY " + orderBy;
        if (limit != null) query += " LIMIT " + limit;
        return query + ";";
    }

    /**
     * Update transaction values in database
     * @param t Transaction object to be updated
     * @return {@code true} if query affects only one row, {@code false} otherwise
     */
    public boolean update(Transaction t) {
        ContentValues values = new ContentValues();
        Calendar dateTime = t.getDateTime();
        values.put(DATETIME_COL, DateFormat.format("yyyy-MM-dd HH:mm", dateTime).toString());
        values.put(CATEGORY_COL, t.getCategory().getId());
        values.put(AMOUNT_COL, t.getAmount());
        values.put(CURRENCY_COL, t.getCurrencyCode());
        values.put(CHANGE_RATE_COL, t.getChangeRate());
        values.put(NOTES_COL, t.getNotes());
        values.put(PAYMENT_TYPE_COL, t.getPaymentTypeId());

        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        int result = db.update(TABLE_NAME, values, ID_COL + " = ?",
                new String[]{String.valueOf(t.getId())});
        db.close();
        Log.i(TAG, "transaction with id " + t.getId() + " updated");
        return result == 1;
    }

    /**
     * Delete transaction in database
     * @param transaction Transaction object to be deleted
     * @return {@code true} if query affects only one row, {@code false} otherwise
     */
    public boolean delete(Transaction transaction) {
        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        int result = db.delete(TABLE_NAME, ID_COL + " = ?",
                new String[]{String.valueOf(transaction.getId())});
        db.close();
        return result == 1;
    }

}
