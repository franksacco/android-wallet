package com.franksacco.wallet.database;

/**
 *
 */
class Database {

    private static final Database ourInstance = new Database();

    static Database getInstance() {
        return ourInstance;
    }

    private Database() {
    }
}
