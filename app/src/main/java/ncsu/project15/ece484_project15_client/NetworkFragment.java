package ncsu.project15.ece484_project15_client;

import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.net.ssl.HttpsURLConnection;


/**
 * Implementation of headless Fragment that runs an AsyncTask to fetch data from the network.
 */
public class NetworkFragment extends Fragment {
    public static final String URL_PRINT = "https://plink.ink/printdoc";
    public static final String URL_GET = "https://plink.ink/json"; // TODO: Change
    public static final String URL_POST = "https://plink.ink/upload";
    public static final String URL_UPLOAD = "https://plink.ink/upload";
    public static final String URL_PRINTER_STATUS = "plink.ink/status"; // TODO: Change
    public static final String URL_PRINT_JOB_STATUS = "plink.ink"; // TODO: Change
    public static final String URL_SIGN_IN_TOKEN = "https://plink.ink/tokensignin";

    public static final String TAG = "NetworkFragment";

    private static final String URL_KEY = "Url Key";
    private static final String DOCUMENT_KEY = "Document Key";
    //private static final String CLIENT_TOKEN_KEY = "Client Token Key";
    private static final String PRINTER_NAME_KEY = "Printer Name Key";
    private static final String LOCATION_KEY = "Location Key";
    private static final String TOKEN_KEY = "idToken";


    private DownloadCallback mCallback;
    private DownloadTask mDownloadTask;

    // Variables from arguments
    private String mUrlString;
    private String mPrinterName;
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
        fragmentManager.beginTransaction().add(networkFragment, TAG).commit();
        fragmentManager.executePendingTransactions();
        return networkFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUrlString = getArguments().getString(URL_KEY);
            mDocumentUri = getArguments().getParcelable(NetworkFragmentBuilder.DOCUMENT_KEY);
            mLocation = getArguments().getParcelable(NetworkFragmentBuilder.LOCATION_KEY);
            mPrinterName = getArguments().getString(NetworkFragmentBuilder.PRINTER_NAME_KEY);
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

        private DownloadCallback<String> mCallback;

        DownloadTask(DownloadCallback<String> callback) {
            setCallback(callback);
        }

        void setCallback(DownloadCallback<String> callback) {
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
            public Result(String resultValue) {
                mResultValue = resultValue;
            }
            public Result(Exception exception) {
                mException = exception;
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
                    Log.i("nf dt doinbackground", e.toString());
                    result = new Result(e);
                }
            }
            return result;
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
        DataOutputStream os;
        HttpsURLConnection connection = null;
        String result = null;
        try {
            connection = (HttpsURLConnection) url.openConnection();
            // Timeout for reading InputStream arbitrarily set to 3000ms.
            connection.setReadTimeout(3000);
            // Timeout for connection.connect() arbitrarily set to 3000ms.
            connection.setConnectTimeout(3000);
            // For this use case, set HTTP method to GET.

            Log.i("downloadURL", "arrived at switch");
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
                    connection.setRequestProperty("Content-Type", "application/pdf;charset=UTF-8");
                    connection.setRequestProperty("Content-Transfer-Encoding", "binary");
                    connection.setRequestProperty("Content-Disposition", "attachment;filename=\"actb.txt\"");
                    connection.setDoInput(true);
                    connection.connect();
                    os = new DataOutputStream(connection.getOutputStream());
                    sendFileToBytes(os);
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
                    String body = TOKEN_KEY + "=" + mIdToken;
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
                }
            }
            mCallback.onProgressUpdate(DownloadCallback.Progress.CONNECT_SUCCESS, 0);
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpsURLConnection.HTTP_OK && responseCode != HttpURLConnection.HTTP_CREATED) {
                throw new IOException("HTTPs error code: " + responseCode);
            }
            // Retrieve the response body as an InputStream.
            stream = connection.getInputStream();
            mCallback.onProgressUpdate(DownloadCallback.Progress.GET_INPUT_STREAM_SUCCESS, 0);
            if (stream != null) {
                // Converts Stream to String with max length of 500.
                result = readStream(stream, 500);
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



    /**
     * Converts the contents of an InputStream to a String.
     */
    public String readStream(InputStream stream, int maxReadSize)
            throws IOException {
        Reader reader;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] rawBuffer = new char[maxReadSize];
        int readSize;
        StringBuilder buffer = new StringBuilder();
        while (((readSize = reader.read(rawBuffer)) != -1) && maxReadSize > 0) {
            if (readSize > maxReadSize) {
                readSize = maxReadSize;
            }
            buffer.append(rawBuffer, 0, readSize);
            maxReadSize -= readSize;
        }
        return buffer.toString();
    }



    public List<Printer> readJsonStream(String in) throws IOException {
        JsonReader reader = new JsonReader(new StringReader(in));
        try {
            return readMessagesArray(reader);
        } finally {
            reader.close();
        }
    }

    private List<Printer> readMessagesArray(JsonReader reader) throws IOException {
        List<Printer> messages = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()) {
            messages.add(readMessage(reader));
        }
        reader.endArray();
        return messages;
    }

    private Printer readMessage(JsonReader reader) throws IOException {
        String printerName = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("name")) {
                printerName = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        Printer printer = new Printer();
        printer.setName(printerName);
        return printer;
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
}