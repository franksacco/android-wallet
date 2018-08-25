package com.franksacco.wallet.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;


/**
 * Asynchronous task for currency rates download
 */
public class ChangeRateDownloader extends AsyncTask<Void, Void, ChangeRateDownloader.Result> {

    private static final String TAG = "ChangeRateDownloader";

    /**
     * Url for change rates xml file
     */
    private final static String
            URL = "https://tassidicambio.bancaditalia.it/terzevalute-wf-web/rest/v1.0/dailyRates";

    /**
     * Weak reference to application context
     */
    private WeakReference<Context> mContext;
    /**
     * Reference date object
     */
    private Calendar referenceDate;
    /**
     * Currency iso code
     */
    private String currencyCode;
    /**
     * Weak reference to optional listener
     */
    private WeakReference<ChangeRateDownloaderListener> mListener;

    /**
     * Recursion level for unavailable rate
     */
    private int mRecursionLevel = 0;

    /**
     * Initialize currency updater
     * @param context Application context
     */
    public ChangeRateDownloader(Context context, Calendar referenceDate,
                                String currencyCode, ChangeRateDownloaderListener listener) {
        this.mContext = new WeakReference<>(context);
        this.referenceDate = (Calendar) referenceDate.clone();
        this.currencyCode = currencyCode;
        this.mListener = new WeakReference<>(listener);
    }

    @Override
    protected Result doInBackground(Void... voids) {
        HttpURLConnection connection = null;
        Result result;
        if (this.mRecursionLevel > 5) {
            return new Result(new RuntimeException("Maximum level of recursion exceeded"));
        }
        try {
            connection = this.connect();
            InputStream stream = connection.getInputStream();
            result = new Result(this.parse(stream));
            stream.close();
        } catch (UnavailableRateException e) {
            this.mRecursionLevel++;
            this.referenceDate.add(Calendar.DAY_OF_MONTH, -1);
            return this.doInBackground();
        } catch (IOException | JSONException e) {
            Log.e(TAG, e.toString());
            result = new Result(e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    @Override
    protected void onPostExecute(Result result) {
        ChangeRateDownloaderListener listener = this.mListener.get();
        if (listener != null) {
            listener.onDownloadTerminated(result);
        }
        Log.i(TAG, "download terminated");
    }

    /**
     * Check the device's network status
     * @return {@code true} if device is connected, {@code false} otherwise
     */
    private boolean checkConnectivity() {
        Context context = this.mContext.get();
        if (context == null) {
            return false;
        }
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }
        return networkInfo != null && networkInfo.isConnected() &&
                (networkInfo.getType() == ConnectivityManager.TYPE_WIFI
                        || networkInfo.getType() == ConnectivityManager.TYPE_MOBILE);
    }

    /**
     * Establish connection to api
     * @return Open http connection to given url
     * @throws IOException If a connection error occur
     */
    private HttpURLConnection connect() throws IOException {
        if (!this.checkConnectivity()) {
            throw new IOException("Device not connected");
        }
        String url = URL + "?referenceDate="
                + DateFormat.format("yyyy-MM-dd", this.referenceDate)
                + "&baseCurrencyIsoCode=" + this.currencyCode + "&currencyIsoCode=EUR";
        Log.i(TAG, "connecting to api at " + url);

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setReadTimeout(5000);
        connection.setConnectTimeout(5000);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setDoInput(true);
        connection.connect();

        int responseCode = connection.getResponseCode();
        Log.i(TAG, "connected to api with response code " + responseCode);
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + responseCode);
        }
        return connection;
    }

    /**
     * Parse Json response and get change rate
     * @param input Input stream from url
     * @return Currency rate
     * @throws IOException If an error occur with stream given
     * @throws JSONException If an error occur during stream parsing
     */
    private Double parse(InputStream input)
            throws UnavailableRateException, IOException, JSONException {
        Double rate;
        try {
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(input));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                result.append(line);
            }
            String body = result.toString();
            try {
                JSONObject rootObject = (JSONObject) new JSONTokener(body).nextValue();
                JSONObject resultInfo = rootObject.getJSONObject("resultsInfo");
                if (resultInfo.getInt("totalRecords") == 0) {
                    throw new UnavailableRateException();
                }
                JSONObject rateObject = (JSONObject) rootObject.getJSONArray("rates").get(0);
                rate = rateObject.getDouble("avgRate");
            } catch (JSONException e) {
                Log.e(TAG, "response body: " + body);
                throw e;
            }
        } finally {
            input.close();
        }
        return rate;
    }

    /**
     * Wrapper class that serves as a union of a result value and an exception
     */
    public static class Result {

        private Double mChangeRate = null;
        private Exception mException = null;

        private Result(Double changeRate) {
            this.mChangeRate = changeRate;
        }
        private Result(Exception exception) {
            this.mException = exception;
        }

        public Double getChangeRate() {
            return this.mChangeRate;
        }
        public Exception getException() {
            return this.mException;
        }
    }

    /**
     * Interface to provide interaction
     */
    public interface ChangeRateDownloaderListener {
        /**
         * Method called when task finishes execution
         * @param result Task result object
         */
        void onDownloadTerminated(Result result);
    }

    private class UnavailableRateException extends Exception {}

}
