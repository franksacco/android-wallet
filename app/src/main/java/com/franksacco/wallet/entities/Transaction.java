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
    private int id;
    /**
     * Transaction date and time
     */
    private Calendar dateTime;
    /**
     * Transaction category
     */
    private Category category;
    /**
     * Transaction amount
     */
    private double amount;
    /**
     * Currency code referred to amount value
     */
    private String currencyCode;
    /**
     * Currency change rate to transform amount in EUR
     */
    private double changeRate;
    /**
     * Transaction notes
     */
    private String notes;
    /**
     * Transaction payment type id
     */
    private int paymentTypeId;

    /**
     * Create a transaction with default values
     */
    public Transaction() {
        this(null, Calendar.getInstance(), 0, "EUR",
                1, "", 0);
    }
    /**
     * Create a new transaction
     * @param category Category object
     * @param dateTime Calendar object
     * @param amount Transaction amount
     * @param currencyCode Currency code
     * @param changeRate Currency change rate
     * @param notes Transaction notes
     * @param paymentTypeId Payment type id
     */
    public Transaction(Category category, Calendar dateTime, double amount, String currencyCode,
                       double changeRate, String notes, int paymentTypeId) {
        this.id = -1;
        this.category = category;
        this.dateTime = dateTime;
        this.amount = amount;
        this.currencyCode = currencyCode;
        this.changeRate = changeRate;
        this.notes = notes;
        this.paymentTypeId = paymentTypeId;
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
     * @return Category object
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
        SimpleDateFormat dateFormat =
                new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
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
     * Get currency code
     * @return Three letter string that identify a currency
     */
    public String getCurrencyCode() {
        return this.currencyCode;
    }
    /**
     * Set currency code
     * @param currencyCode Three letter string that identify a currency
     */
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    /**
     * Get currency change rate to transform amount in euros
     * @return Change rate
     */
    public double getChangeRate() {
        return this.changeRate;
    }
    /**
     * Set transaction change rate
     * @param changeRate Change rate to transform amount in EUR
     */
    public void setChangeRate(double changeRate) {
        this.changeRate = changeRate;
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
     * @return Payment type as string
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

    @Override
    public String toString() {
        return TAG + ": id=" + id + ", category=" + category + ", amount=" + amount
                + ", paymentType=" + paymentTypeId + ", datetime=" + dateTime + ", notes=" + notes;
    }

}
