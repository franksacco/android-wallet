package com.franksacco.wallet.entities;

import android.util.Log;

import com.franksacco.wallet.R;

/**
 * This class represent a category entity
 */
public class Category {

    private static final String TAG = "Category";

    /**
     * Category database id
     */
    private int id = 0;

    /**
     * Category icon name
     */
    private String icon = null;

    /**
     * Category name
     */
    private String name = null;

    /**
     * Category constructors
     */
    public Category() {}
    public Category(String icon, String name) {
        this.icon = icon;
        this.name = name;
    }
    public Category(int id, String icon, String name) {
        this.id = id;
        this.icon = icon;
        this.name = name;
    }

    /**
     * Get category id
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Set category id
     * @param id int
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get icon string identifier
     * @return Icon string identifier
     */
    public String getIcon() {
        return this.icon;
    }

    /**
     * Get drawable identifier for category icon
     * @return Icon identifier
     */
    public int getIconIdentifier() {
        try {
            return R.drawable.class.getField(this.icon).getInt(null);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Log.e(TAG, "invalid drawable resource identifier: " + icon);
        }
        return R.drawable.ic_style_white_24dp;
    }

    /**
     * Set icon string identifier
     * @param icon String
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * Get string identifier for category name
     * @return String
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set name identifier if valid
     * @param name String
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return TAG + ": id=" + id + ", icon=" + icon + ", name=" + name;
    }

}
