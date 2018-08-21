package com.franksacco.wallet.helpers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Xml;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 *
 */
public class CurrencyUpdater extends AsyncTask<Void, Void, CurrencyUpdater.Result> {

    private final static String
            URL = "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml";

    /**
     * Weak reference to application context
     */
    private WeakReference<Context> mContext;

    /**
     * Initialize currency updater
     * @param context Application context
     */
    public CurrencyUpdater(Context context) {
        this.mContext = new WeakReference<>(context);
    }

    /**
     * Wrapper class that serves as a union of a result value and an exception
     */
    static class Result {
        ArrayList<Double> mValue;
        Exception mException;
        Result(ArrayList<Double> resultValue) {
            this.mValue = resultValue;
        }
        Result(Exception exception) {
            this.mException = exception;
        }
    }

    @Override
    protected Result doInBackground(Void... voids) {
        HttpURLConnection connection = null;
        Result result;
        try {
            connection = this.connect();
            InputStream stream = connection.getInputStream();
            result = new Result(this.parse(stream));
            stream.close();
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
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
        Context context = this.mContext.get();
        if (context == null) {
            return;
        }
        if (result.mException != null) {
            Toast.makeText(context, result.mException.getLocalizedMessage(),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Founded " + result.mValue.size() + " rates",
                    Toast.LENGTH_SHORT).show();
        }
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
     * Establish a connection to host
     * @return Open http connection to given url
     * @throws IOException If a connection error occur
     */
    private HttpURLConnection connect() throws IOException {
        if (!this.checkConnectivity()) {
            throw new IOException("Device not connected");
        }
        URL url = new URL(URL);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setReadTimeout(10000);
        connection.setConnectTimeout(5000);
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + responseCode);
        }
        return connection;
    }

    /**
     *
     * @param input Input stream from url
     * @return Currency rate list
     * @throws XmlPullParserException If an error occur during file parsing
     * @throws IOException In an error occur with stream given
     */
    private ArrayList<Double> parse(InputStream input) throws XmlPullParserException, IOException {
        ArrayList<Double> currencies = new ArrayList<>();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(input, null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, null, "gesmes:Envelope");

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                if (parser.getName().equals("Cube") && parser.getAttributeCount() == 2) {
                    String rate = parser.getAttributeValue(null, "rate");
                    try {
                        currencies.add(Double.parseDouble(rate));
                    } catch (NullPointerException e) {
                        throw new XmlPullParserException("Rate attribute not found");
                    } catch (NumberFormatException e) {
                        throw new XmlPullParserException("Invalid rate format");
                    }
                }
            }
        } finally {
            input.close();
        }
        return currencies;
    }

}
