package com.franksacco.wallet.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Arrays;


/**
 * Helper class for currency management
 */
public class CurrencyManager {

    /**
     * Preference key for preferred currency
     */
    public static final String PREFERRED_CURRENCY_PREFERENCE = "preferred_currency";

    /**
     * Preference key for currency rates
     */
    public static final String CHANGE_RATES_PREFERENCE = "currency_rates";

    /**
     * Preference key for currency rates last update
     */
    public static final String CHANGE_RATES_UPDATE_PREFERENCE = "currency_rates_update";

    /**
     * Default currency rates (2018-08-10)
     */
    private static final double[] DEFAULT_CURRENCY_RATES = {1.0,
            1.1456, 127.07, 1.9558, 25.635, 7.4544, 0.89675, 322.77, 4.2911,
            4.6583, 10.4258, 1.1391, 124.60, 9.5330, 7.4333, 76.8097, 6.9309,
            1.5675, 4.3904, 1.5002, 7.8468, 8.9928, 16583.56, 4.2485, 78.8520,
            1293.54, 21.6533, 4.6809, 1.7365, 60.833, 1.5708, 38.114, 15.9272
    };

    /**
     * Currency code list
     */
    private static final String[] CODES = {
            "EUR", "USD", "JPY", "BGN", "CZK", "DKK", "GBP", "HUF", "PLN",
            "RON", "SEK", "CHF", "ISK", "NOK", "HRK", "RUB", "TRY", "AUD",
            "BRL", "CAD", "CNY", "HKD", "IDR", "ILS", "INR", "KRW", "MXN",
            "MYR", "NZD", "PHP", "SGD", "THB", "ZAR"
    };

    /**
     * Currency symbol list
     */
    private static final String[] SYMBOLS = {
            "€", "$", "¥", "лв", "Kč", "kr", "£", "Ft", "zł", "lei", "kr",
            "CHF", "kr", "kr", "kn", "₽", "TRY", "$", "R$", "$", "¥", "$",
            "Rp", "₪", "INR", "₩", "$", "RM", "$", "₱", "$", "฿", "R"
    };

    /**
     * Preferred currency index
     */
    private int mPreferredIndex;

    /**
     * Currency change rates
     */
    private double[] mChangeRates;

    /**
     * Initialize currency manager
     * @param context Application context
     */
    public CurrencyManager(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        String preferredCode = preferences.getString(PREFERRED_CURRENCY_PREFERENCE, "EUR");
        int index = Arrays.asList(CODES).indexOf(preferredCode);
        this.mPreferredIndex = index > 0 ? index : 0;

        if (!preferences.contains(CHANGE_RATES_PREFERENCE)) {
            this.mChangeRates = DEFAULT_CURRENCY_RATES;
        } else {
            this.mChangeRates = DEFAULT_CURRENCY_RATES;
            // todo fix
        }
    }

    /**
     * Get preferred currency index
     * @return Preferred currency index
     */
    public int getPreferredIndex() {
        return this.mPreferredIndex;
    }

    /**
     * Get symbol for preferred currency
     * @return Symbol in a string
     */
    public String getPreferredSymbol() {
        return SYMBOLS[this.mPreferredIndex];
    }

    /**
     * Convert <i>amount</i> from EUR to preferred currency
     * @param amount Amount in Euro
     * @return Amount in preferred currency
     */
    public double convertToPreferred(double amount) {
        return amount * this.mChangeRates[this.mPreferredIndex];
    }

    /**
     * Convert <i>amount</i> from <i>sourceCurrency</i> to EUR
     * @param sourceCurrencyId Source currency index
     * @param amount Amount in preferred currency
     * @return Amount in Euro
     */
    public double convertFrom(int sourceCurrencyId, double amount) {
        return amount * (1.0 / this.mChangeRates[sourceCurrencyId]);
    }

}
