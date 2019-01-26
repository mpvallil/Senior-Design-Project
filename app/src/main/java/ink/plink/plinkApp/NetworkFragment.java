package ink.plink.plinkApp;

import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.maps.model.LatLng;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;


/**
 * Implementation of headless Fragment that runs an AsyncTask to fetch data from the network.
 */
public class NetworkFragment extends Fragment {
    public static final String URL_PRINT = "https://plink.ink/printrequest";
    public static final String URL_GET = "https://plink.ink/json"; // TODO: Change
    public static final String URL_POST = "https://plink.ink/upload";
    public static final String URL_UPLOAD = "https://plink.ink/upload";
    public static final String URL_PRINTER_STATUS = "plink.ink/status"; // TODO: Change
    public static final String URL_PRINT_JOB_STATUS = "plink.ink"; // TODO: Change
    public static final String URL_SIGN_IN_TOKEN = "https://plink.ink/tokensignin";
    public static final String URL_DATABASE_SIGN_IN_TOKEN = "https://plink.ink/createuser";
    public static final String URL_GET_LOCAL_PRINTERS = "https://plink.ink/getlocalprinters";
    public static final String URL_GET_PRINTERS_BY_OWNER = "https://plink.ink/getprintersbyowner";

    public static final String TAG = "NetworkFragment";

    private static final String URL_KEY = "Url Key";
    private static final String DOCUMENT_KEY = "Document Key";
    private static final String PRINTER_ID_KEY = "printer_id";
    private static final String LOCATION_KEY = "Location Key";
    private static final String TOKEN_KEY = "idToken";
    private static final String COOKIE_HEADER = "Set-Cookie";
    List<String> cookieHeaderList = new ArrayList<>();
    public static CookieManager msCookieManager = new CookieManager();

    //HTTP codes
    public static final String HTTP_NOT_FOUND = "404";
    public static final String HTTP_UNAUTHORIZED = "401";
    public static final String HTTP_SERVER_ERROR = "502";
    public static final String HTTP_OK = "OK";


    private DownloadCallback mCallback;
    private DownloadTask mDownloadTask;

    // Variables from arguments
    private String mUrlString;
    private String mPrinterId;
    private Uri mDocumentUri;
    private LatLng mLocation;
    private String mIdToken;

    // Strings for sending HTTP POST
    String attachmentName = "file";
    String crlf = "\r\n";
    String twoHyphens = "--";
    String boundary =  "*****";

    /**
     * Static initializer for NetworkFragment that sets the URL of the host it will be downloading
     * from.
     */
    public static NetworkFragment getInstance(FragmentManager fragmentManager, String url) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        networkFragment.setArguments(args);
        fragmentManager.beginTransaction().add(networkFragment, TAG).commit();
        fragmentManager.executePendingTransactions();
        return networkFragment;
    }

    public static NetworkFragment getTokenSigninInstance(FragmentManager fragmentManager, String idToken) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putString(TOKEN_KEY, idToken);
        args.putString(URL_KEY, URL_SIGN_IN_TOKEN);
        networkFragment.setArguments(args);
        fragmentManager.beginTransaction().add(networkFragment, URL_SIGN_IN_TOKEN).commit();
        fragmentManager.executePendingTransactions();
        return networkFragment;
    }

    public static NetworkFragment getTokenDatabaseSigninInstance(FragmentManager fragmentManager, String idToken) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putString(TOKEN_KEY, idToken);
        args.putString(URL_KEY, URL_DATABASE_SIGN_IN_TOKEN);
        networkFragment.setArguments(args);
        fragmentManager.beginTransaction().add(networkFragment, URL_DATABASE_SIGN_IN_TOKEN).commit();
        fragmentManager.executePendingTransactions();
        return networkFragment;
    }

    public static NetworkFragment getGetLocalPrintersInstance(FragmentManager fragmentManager, LatLng location) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putParcelable(LOCATION_KEY, location);
        args.putString(URL_KEY, URL_GET_LOCAL_PRINTERS);
        networkFragment.setArguments(args);
        fragmentManager.beginTransaction().add(networkFragment, URL_GET_LOCAL_PRINTERS).commit();
        fragmentManager.executePendingTransactions();
        return networkFragment;
    }

    public static NetworkFragment getPrintRequestInstance(FragmentManager fragmentManager, Uri uri, String printer_id) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putParcelable(DOCUMENT_KEY, uri);
        args.putString(PRINTER_ID_KEY, printer_id);
        args.putString(URL_KEY, URL_PRINT);
        networkFragment.setArguments(args);
        fragmentManager.beginTransaction().add(networkFragment, URL_PRINT).commit();
        fragmentManager.executePendingTransactions();
        return networkFragment;
    }

    public static NetworkFragment getGetPrintersByOwnerInstance(FragmentManager fragmentManager) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putString(URL_KEY, URL_GET_PRINTERS_BY_OWNER);
        networkFragment.setArguments(args);
        fragmentManager.beginTransaction().add(networkFragment, URL_GET_PRINTERS_BY_OWNER).commit();
        fragmentManager.executePendingTransactions();
        return networkFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUrlString = getArguments().getString(URL_KEY);
            mDocumentUri = getArguments().getParcelable(DOCUMENT_KEY);
            mLocation = getArguments().getParcelable(LOCATION_KEY);
            mPrinterId = getArguments().getString(PRINTER_ID_KEY);
            mIdToken = getArguments().getString(TOKEN_KEY);
        }
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Host Activity will handle callbacks from task.
        mCallback = (DownloadCallback) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // Clear reference to host Activity to avoid memory leak.
        mCallback = null;
    }

    @Override
    public void onDestroy() {
        // Cancel task when Fragment is destroyed.
        cancelDownload();
        super.onDestroy();
    }

    /**
     * Start non-blocking execution of DownloadTask.
     */
    public void startDownload() {
        cancelDownload();
        mDownloadTask = new DownloadTask(mCallback);
        mDownloadTask.execute(mUrlString);
        Log.i("nf startdownload", mUrlString);
    }

    /**
     * Cancel (and interrupt if necessary) any ongoing DownloadTask execution.
     */
    public void cancelDownload() {
        if (mDownloadTask != null) {
            mDownloadTask.cancel(true);
        }
    }


    /**
     * Implementation of AsyncTask designed to fetch data from the network.
     */
    private class DownloadTask extends AsyncTask<String, Integer, DownloadTask.Result> {

        private DownloadCallback mCallback;

        DownloadTask(DownloadCallback callback) {
            setCallback(callback);
        }

        void setCallback(DownloadCallback callback) {
            mCallback = callback;
        }

        /**
         * Wrapper class that serves as a union of a result value and an exception. When the download
         * task has completed, either the result value or exception can be a non-null value.
         * This allows you to pass exceptions to the UI thread that were thrown during doInBackground().
         */
        class Result {
            public String mResultValue;
            public Exception mException;
            public int mHTTPCode;
            public Result(String resultValue) {
                mResultValue = resultValue;
            }
            public Result(Exception exception) {
                mException = exception;
            }
            public Result(int httpCode) {
                mHTTPCode = httpCode;
            }
        }

        /**
         * Cancel background network operation if we do not have network connectivity.
         */
        @Override
        protected void onPreExecute() {
            if (mCallback != null) {
                NetworkInfo networkInfo = mCallback.getActiveNetworkInfo();
                if (networkInfo == null || !networkInfo.isConnected() ||
                        (networkInfo.getType() != ConnectivityManager.TYPE_WIFI
                                && networkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                    // If no connectivity, cancel task and update Callback with null data.
                    mCallback.updateFromDownload("No Network Info");
                    cancel(true);
                }
            }
        }

        /**
         * Defines work to perform on the background thread.
         */
        @Override
        protected DownloadTask.Result doInBackground(String... urls) {
            Result result = null;
            if (!isCancelled() && urls != null && urls.length > 0) {
                String urlString = urls[0];
                try {
                    URL url = new URL(urlString);
                    String resultString = downloadUrl(url);
                    if (resultString != null) {
                        result = new Result(resultString);
                    } else {
                        throw new IOException("No response received.");
                    }
                } catch(Exception e) {
                    mCallback.onProgressUpdate(DownloadCallback.Progress.ERROR, 0);
                    result = new Result(e);
                }
            }
            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            mCallback.onProgressUpdate(values[0], values[1]);
        }

        /**
         * Updates the DownloadCallback with the result.
         */
        @Override
        protected void onPostExecute(Result result) {
            if (result != null && mCallback != null) {
                if (result.mException != null) {
                    mCallback.updateFromDownload(result.mException.getMessage());
                } else if (result.mResultValue != null) {
                    mCallback.updateFromDownload(result.mResultValue);
                }
                mCallback.finishDownloading();
            }
        }

        /**
         * Override to add special behavior for cancelled AsyncTask.
         */
        @Override
        protected void onCancelled(Result result) {
            
        }


    }

    /**
     * Given a URL, sets up a connection and gets the HTTP response body from the server.
     * If the network request is successful, it returns the response body in String form. Otherwise,
     * it will throw an IOException.
     */
    private String downloadUrl(URL url) throws IOException {
        InputStream stream = null;
        DataOutputStream os = null;
        HttpsURLConnection connection = null;
        String result = null;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(3000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(3000);
            putCookieInConnection(connection);
            sendDataToServer(url, stream, os, connection);
            mCallback.onProgressUpdate(DownloadCallback.Progress.CONNECT_SUCCESS, 25);
            int responseCode = connection.getResponseCode();
            getCookieFromConnection(connection);
            if (responseCode != HttpsURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
                throw new IOException(Integer.toString(responseCode));
            }
            // Function to decide where the API call is going
            stream = connection.getInputStream();
            mCallback.onProgressUpdate(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 50);
            if (stream != null) {
                // Converts Stream to String with max length of 500.
                mCallback.onProgressUpdate(DownloadCallback.Progress.PROCESS_INPUT_STREAM_IN_PROGRESS, 75);
                result = new String(IOUtils.toByteArray(stream));
                mCallback.onProgressUpdate(DownloadCallback.Progress.PROCESS_INPUT_STREAM_SUCCESS, 100);
            }
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    private void sendDataToServer(URL url, InputStream stream, DataOutputStream os, HttpsURLConnection connection) throws IOException {
        switch(url.toString()) {
            case URL_GET: {
                connection.setRequestMethod("GET");
                connection.setDoInput(true);
                connection.connect();
                break;
            }
            case URL_UPLOAD: {

                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection-Type", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("Content-Transfer-Encoding", "multipart/form-data");
                connection.setDoInput(true);
                connection.connect();
                os = new DataOutputStream(connection.getOutputStream());
                os.writeBytes( twoHyphens + boundary + crlf);
                os.writeBytes("Content-Disposition: form-data; name="+attachmentName+";filename="+getDocumentName(mDocumentUri)+crlf);
                os.writeBytes(crlf);
                sendFileToBytes(os);
                os.writeBytes(crlf);
                os.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
                os.flush();
                os.close();
                break;
            }
            case URL_PRINT: {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection-Type", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                connection.setRequestProperty("Content-Transfer-Encoding", "multipart/form-data");
                connection.setDoInput(true);
                connection.connect();
                os = new DataOutputStream(connection.getOutputStream());
                os.writeBytes( twoHyphens+boundary + crlf);
                os.writeBytes("Content-Disposition: form-data; name="+PRINTER_ID_KEY+crlf);
                os.writeBytes("Content-Type: text/plain"+crlf+crlf);
                os.writeBytes(mPrinterId+crlf);
                os.writeBytes( twoHyphens+boundary + crlf);
                os.writeBytes("Content-Disposition: form-data; name="+attachmentName+";filename="+getDocumentName(mDocumentUri)+crlf);
                os.writeBytes(crlf);
                sendFileToBytes(os);
                os.writeBytes(crlf);
                os.writeBytes(twoHyphens+boundary + twoHyphens);
                os.flush();
                os.close();
                break;
            }
            case URL_PRINT_JOB_STATUS: {

                break;
            }

            case URL_PRINTER_STATUS: {

                break;
            }

            case URL_SIGN_IN_TOKEN: {
                String body_token = TOKEN_KEY + "=" + mIdToken;
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection-Type", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Transfer-Encoding", "binary");
                connection.setRequestProperty("Content-Length", "" + Integer.toString(body_token.getBytes().length));
                connection.setDoInput(true);
                connection.connect();
                os = new DataOutputStream(connection.getOutputStream());
                os.writeBytes(body_token);
                os.flush();
                os.close();
                break;
            }

            case URL_DATABASE_SIGN_IN_TOKEN: {
                String body_token = TOKEN_KEY + "=" + mIdToken;
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection-Type", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Transfer-Encoding", "binary");
                connection.setRequestProperty("Content-Length", "" + Integer.toString(body_token.getBytes().length));
                connection.setDoInput(true);
                connection.connect();
                os = new DataOutputStream(connection.getOutputStream());
                os.writeBytes(body_token);
                os.flush();
                os.close();
                break;
            }

            case URL_GET_LOCAL_PRINTERS: {
                String body = "lat="+ Double.toString(mLocation.latitude)+"&lng=" + Double.toString(mLocation.longitude);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Connection-Type", "Keep-Alive");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("Content-Transfer-Encoding", "binary");
                connection.setRequestProperty("Content-Length", "" + Integer.toString(body.getBytes().length));
                connection.setDoInput(true);
                connection.connect();
                os = new DataOutputStream(connection.getOutputStream());
                os.writeBytes(body);
                os.flush();
                os.close();
                break;
            }
        }
    }

    private void sendFileToBytes(OutputStream os) {
        //File file = new File(mDocumentUri.getPath());
        //Log.i("fileToByte", file.toString());
        InputStream fis = null;
        byte buffer[];
        try {
            fis = getActivity().getContentResolver().openInputStream(mDocumentUri);

            buffer = new byte[4096];
            int read = 0;

            while((read = fis.read(buffer)) != -1) {
                os.write(buffer, 0, read);
                Log.i("file", Integer.toString(read));
            }
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.toString());
        } catch (IOException e) {
            System.out.println("Exception reading file: " + e.toString());
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    public String getDocumentName(Uri uri) {
        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        String displayName = "no file name";
        Cursor cursor = getActivity().getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i("ManageDocument", "Display Name: " + displayName);
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null.  But since an
                // int can't be null in Java, the behavior is implementation-specific,
                // which is just a fancy term for "unpredictable".  So as
                // a rule, check if it's null before assigning to an int.  This will
                // happen often:  The storage API allows for remote files, whose
                // size might not be locally known.
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                } else {
                    size = "Unknown";
                }
                Log.i("ManageDocument", "Size: " + size);
            }
        } finally {
            cursor.close();
        }
        return displayName;
    }

    private void getCookieFromConnection(HttpsURLConnection connection) {
        Map<String, List<String>> connectionHeaders = connection.getHeaderFields();
        cookieHeaderList = connectionHeaders.get(COOKIE_HEADER);
        if (cookieHeaderList != null) {
            for (String cookie : cookieHeaderList) {
                msCookieManager.getCookieStore().add(null,HttpCookie.parse(cookie).get(0));
            }
        }

    }

    private void putCookieInConnection(HttpsURLConnection connection) {
        if (msCookieManager.getCookieStore().getCookies().size() > 0) {
            // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
            connection.setRequestProperty("Cookie",
                    TextUtils.join(";",  msCookieManager.getCookieStore().getCookies()));
        }
    }
}