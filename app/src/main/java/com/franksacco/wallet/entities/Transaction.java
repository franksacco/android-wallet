package com.franksacco.wallet.entities;

import android.content.Context;
import android.util.Log;

import com.franksacco.wallet.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * This class represent a transaction entity
 */
public class Transaction {

    private static final String TAG = "Transaction";

    /**
     * Transaction database mId
     */
    private int id = 0;

    /**
     * Transaction category
     */
    private Category category;

    /**
     * Transaction amount
     */
    private double amount = 0.0;

    /**
     * Transaction payment type id
     */
    private int paymentTypeId = 0;

    /**
     * Transaction date and time
     */
    private Calendar dateTime = Calendar.getInstance();

    /**
     * Transaction notes
     */
    private String notes = "";

    /**
     * Transaction constructor
     */
    public Transaction() {}
    public Transaction(Category category, double amount, int paymentTypeId,
                       Calendar dateTime, String notes) {
        this.category = category;
        this.amount = amount;
        this.paymentTypeId = paymentTypeId;
        this.dateTime = dateTime;
        this.notes = notes;
    }

    /**
     * Get category id
     * @return int
     */
    public int getId() {
        return this.id;
    }

    /**
     * Set category id
     * @param id int
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get transaction category
     * @return category object
     */
    public Category getCategory() {
        return this.category;
    }

    /**
     * Set transaction category
     * @param category Category object
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * Get transaction amount
     * @return Amount
     */
    public double getAmount() {
        return this.amount;
    }

    /**
     * Set transaction amount
     * @param amount Amount
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * Get transaction payment type id
     * @return Payment type id
     */
    public int getPaymentTypeId() {
        return this.paymentTypeId;
    }

    /**
     * Get transaction payment type
     * @param context Application context
     * @return Payment type string
     */
    public String getPaymentType(Context context) {
        String[] resource = context.getResources().getStringArray(R.array.payment_types);
        try {
            return resource[this.paymentTypeId];
        } catch (IndexOutOfBoundsException e) {
            return "";
        }
    }

    /**
     * Set transaction payment type id
     * @param paymentTypeId Payment type id
     */
    public void setPaymentTypeId(int paymentTypeId) {
        this.paymentTypeId = paymentTypeId;
    }

    /**
     * Get transaction date and time
     * @return Calendar object
     */
    public Calendar getDateTime() {
        return this.dateTime;
    }

    /**
     * Set transaction date and time
     * @param dateTime String representing a datetime in format yyyy-MM-dd HH:mm
     */
    public void setDateTime(String dateTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ITALY);
        try {
            Date date = dateFormat.parse(dateTime);
            this.dateTime.setTime(date);
        } catch (ParseException e) {
            Log.e(TAG, "invalid datetime format");
        }
    }

    /**
     * Set transaction date
     * @param year Year
     * @param month Month
     * @param date Day of month
     */
    public void setDate(int year, int month, int date) {
        this.dateTime.set(year, month, date);
    }

    /**
     * Set transaction time
     * @param hour Hour
     * @param minute Minute
     */
    public void setTime(int hour, int minute) {
        this.dateTime.set(Calendar.HOUR_OF_DAY, hour);
        this.dateTime.set(Calendar.MINUTE, minute);
    }

    /**
     * Get transaction notes
     * @return Notes string
     */
    public String getNotes() {
        return this.notes;
    }

    /**
     * Set transaction notes
     * @param notes Notes string
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return TAG + ": id=" + id + ", category=" + category + ", amount=" + amount
                + ", paymentType=" + paymentTypeId + ", datetime=" + dateTime + ", notes=" + notes;
    }

}
