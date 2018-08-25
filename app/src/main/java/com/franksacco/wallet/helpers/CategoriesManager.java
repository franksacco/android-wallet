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
@SuppressWarnings("WeakerAccess")
public class CategoriesManager {

    private static final String TAG = "CategoriesManager";

    public static final String TABLE_NAME = "categories";

    public static final String ID_COL = "id";
    public static final String ICON_COL = "icon";
    public static final String NAME_COL = "name";
    public static final String DELETED_COL = "deleted";

    public static final String[] ALL_COLUMNS = {
            ID_COL,
            ICON_COL,
            NAME_COL
    };

    /**
     * Instance of database helper
     */
    private DatabaseOpenHelper mDatabaseHelper;

    /**
     * Categories manager initialization
     * @param context Application context
     */
    public CategoriesManager(Context context) {
        this.mDatabaseHelper = DatabaseOpenHelper.getInstance(context);
    }

    /**
     * Table creation
     * @param db Database reference
     */
    public static void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ICON_COL + " TEXT NOT NULL DEFAULT 'ic_style_white_24dp', "
                + NAME_COL + " TEXT NOT NULL, "
                + DELETED_COL + " BOOLEAN NOT NULL DEFAULT 0"
                + ");");
        Log.i(TAG, "table created");

        insertDefaultCategories(db);
        Log.i(TAG, "default categories inserted");
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
                + "VALUES ('ic_local_grocery_store_white_24dp', 'Supermercato');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_local_mall_white_24dp', 'Abbigliamento');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_devices_other_white_24dp', 'Elettronica');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_local_hospital_white_24dp', 'Farmacia');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_local_movies_white_24dp', 'Tempo libero');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_fitness_center_white_24dp', 'Fitness');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_hotel_white_24dp', 'Hotel');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_wb_incandescent_white_24dp', 'Bollette/affitto');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_home_white_24dp', 'Casa/giardino');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_build_white_24dp', 'Manutenzione');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_work_white_24dp', 'Stipendio');");
        db.execSQL("INSERT INTO " + TABLE_NAME + " (" + ICON_COL + ", " + NAME_COL + ") "
                + "VALUES ('ic_card_giftcard_white_24dp', 'Regalo');");
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

        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        if (id > 0) {
            category.setId((int) id);
            Log.i(TAG, "inserted category with id " + id);
        }
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
        if (where == null) {
            where = DELETED_COL + " = 0";
        } else {
            where += " AND " + DELETED_COL + " = 0";
        }
        SQLiteDatabase db = this.mDatabaseHelper.getReadableDatabase();
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

    /**
     * Delete category from database
     * @param category Category object to be deleted
     * @return {@code true} if query affects only one row, {@code false} otherwise
     */
    public boolean delete(Category category) {
        ContentValues values = new ContentValues();
        values.put(DELETED_COL, 1);

        SQLiteDatabase db = this.mDatabaseHelper.getWritableDatabase();
        int result = db.update(TABLE_NAME, values, ID_COL + " = ?",
                new String[]{String.valueOf(category.getId())});
        db.close();
        Log.i(TAG, "deleted category with id " + category.getId());
        return result == 1;
    }

}
