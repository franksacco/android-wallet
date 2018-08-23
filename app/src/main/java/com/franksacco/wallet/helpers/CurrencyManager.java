package com.franksacco.wallet.helpers;

import java.util.Arrays;


/**
 * Helper class for currency management
 */
public class CurrencyManager {

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
     * Get currency iso code from index
     * @param index Currency index
     * @return Currency index or -1 if <i>code</i> is not found
     */
    public static String getCode(int index) {
        String code;
        try {
            code = CODES[index];
        } catch (IndexOutOfBoundsException e) {
            code = "";
        }
        return code;
    }

    /**
     * Get currency index from iso code
     * @param code Currency iso code
     * @return Currency index or -1 if <i>code</i> is not found
     */
    public static int getIndex(String code) {
        return Arrays.asList(CODES).indexOf(code);
    }

    /**
     * Get currency symbol from iso code
     * @param code Currency iso code
     * @return Currency symbol or '?' if <i>code</i> is not fount
     */
    public static String getSymbol(String code) {
        int index = getIndex(code);
        return index == -1 ? "?" : SYMBOLS[index];
    }

}
