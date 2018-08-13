package com.franksacco.wallet.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.franksacco.wallet.entities.Category;

import java.util.ArrayList;


/**
 * Helper class for categories management in database
 */
public class CategoriesManager {

    private static final String TAG = "CategoriesManager";

    public static final String TABLE_NAME = "categories";

    public static final String ID_COL = "id";
    public static final String ICON_COL = "icon";
    public static final String NAME_COL = "name";

    public static final String[] ALL_COLUMNS = {
            ID_COL,
            ICON_COL,
            NAME_COL
    };

    /**
     * Instance of database helper
     */
    private DatabaseOpenHelper db;

    /**
     * Categories manager initialization
     * @param context Application context
     */
    public CategoriesManager(Context context) {
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
                + ICON_COL + " TEXT NOT NULL DEFAULT 'ic_style_white_24dp', "
                + NAME_COL + " TEXT NOT NULL"
                + ");");
        Log.d(TAG, "table created");

        insertDefaultCategories(db);
        Log.d(TAG, "default categories inserted");
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
     * Populate table with default categories
     */
    private static void insertDefaultCategories(SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_local_cafe_white_24dp', 'Bar');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_local_bar_white_24dp', 'Drink');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_local_pizza_white_24dp', 'Fast food');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_restaurant_white_24dp', 'Ristorante');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_local_gas_station_white_24dp', 'Carburante');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_train_white_24dp', 'Trasporto pubblico');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_local_airport_white_24dp', 'Aereo');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_build_white_24dp', 'Manutenzione');");
        /*
         * todo add all default categories
         */
    }

    /**
     * Transform actual row pointed by <i>cursor</i> into a category object
     * @param cursor Cursor from a SELECT query
     * @return Category object
     */
    private Category cursorToObject(Cursor cursor) {
        Category category = new Category();
        category.setId(cursor.getInt(0));
        category.setIcon(cursor.getString(1));
        category.setName(cursor.getString(2));
        return category;
    }

    /**
     * Insert a single category in database
     * @param category Category object to be inserted
     * @return the row ID of the newly inserted row, or -1 if an error occurred
     */
    public long insert(Category category) {
        ContentValues values = new ContentValues();
        values.put(ICON_COL, category.getIcon());
        values.put(NAME_COL, category.getName());

        SQLiteDatabase db = this.db.getWritableDatabase();
        long id = db.insert(TABLE_NAME, null, values);
        Log.d(TAG, "inserted category with id " + id);
        db.close();
        return id;
    }

    /**
     * Retrieve categories from database
     * @param columns Columns list to be selected
     * @param where Where clause
     * @param whereParams Params used in <i>where</i> clause
     * @param groupBy Group by clause
     * @param having Having clause
     * @param orderBy Order by clause
     * @param limit Limit clause
     * @return Category list
     */
    public ArrayList<Category> select(String[] columns, String where,
                                       String[] whereParams, String groupBy,
                                       String having, String orderBy, String limit) {
        SQLiteDatabase db = this.db.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME, columns, where, whereParams,
                groupBy, having, orderBy, limit);
        cursor.moveToFirst();

        ArrayList<Category> list = new ArrayList<>();
        while (!cursor.isAfterLast()) {
            list.add(cursorToObject(cursor));
            cursor.moveToNext();
        }
        cursor.close();
        db.close();
        return list;
    }

}
