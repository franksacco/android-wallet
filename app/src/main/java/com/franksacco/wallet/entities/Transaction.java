package com.franksacco.wallet.entities;

/**
 * This class represent a transaction entity
 */
public class Transaction {

    private static final String TAG = "Transaction";

    /**
     * Category database id
     */
    private int id = 0;

    /**
     * Transaction constructor
     */
    public Transaction() {}

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

    @Override
    public String toString() {
        return "Transaction: id=" + id;
    }

}
