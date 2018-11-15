package ncsu.project15.ece484_project15_client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ManageDocument.OnManageDocumentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ManageDocument#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ManageDocument extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // Request code for getting files off device
    private static final int READ_REQUEST_CODE = 42;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnManageDocumentInteractionListener mListener;
    private Button openDocumentButton;
    private Button uploadDocumentButton;
    private Uri contentUri;

    private NetworkFragment documentNetworkFragment;

    public ManageDocument() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * param1 Parameter 1.
     * param2 Parameter 2.
     * @return A new instance of fragment ManageDocument.
     */
    // TODO: Rename and change types and number of parameters
    public static ManageDocument newInstance() {
        ManageDocument fragment = new ManageDocument();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_manage_document, container, false);
        openDocumentButton = v.findViewById(R.id.find_document);
        uploadDocumentButton = v.findViewById(R.id.upload_document);
        openDocumentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performFileSearch();
            }
        });

        uploadDocumentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (documentNetworkFragment != null) {
                    mListener.onManageDocumentInteraction(documentNetworkFragment);
                }
            }
        });
        Toolbar fragmentToolbar = (Toolbar) v.findViewById(R.id.toolbar);
        Toolbar activityToolbar = (Toolbar) getActivity().findViewById(R.id.my_toolbar);
        activityToolbar.setVisibility(View.GONE);
        ((AppCompatActivity)getActivity()).setSupportActionBar(fragmentToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fragmentToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        //((AppCompatActivity)getActivity()).getSupportActionBar().hide();

        // Inflate the layout for this fragment

        return v;
    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Integer btn) {
        if (mListener != null) {
            //mListener.onManageDocumentInteraction();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnManageDocumentInteractionListener) {
            mListener = (OnManageDocumentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnManageDocumentInteractionListener {
        // TODO: Update argument type and name
        void onManageDocumentInteraction(NetworkFragment networkFragment);
    }

    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("application/pdf");

        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (resultData != null) {
                contentUri = resultData.getData();
                File file = new File(contentUri.getPath());
                try {
                    InputStream is = getActivity().getContentResolver().openInputStream(contentUri);
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(is.read());
                    fos.close();
                    is.close();
                } catch(Exception e) {
                    Log.i("failed", "failed");
                }
                Log.i("FileExplorer", "Uri: " + contentUri.toString());
                Log.i("Manage Document", file.toString());
                String filePath = file.getAbsolutePath();
                Log.i("Manage Document", filePath);
                String fileName = filePath.substring(filePath.lastIndexOf(File.separator)+1);
                Log.i("File name", fileName);
                Toast.makeText(getContext(), "File name: " + fileName, Toast.LENGTH_SHORT ).show();
                dumpFileMetaData(contentUri);
                documentNetworkFragment = NetworkFragmentBuilder.build(getActivity().getSupportFragmentManager(), NetworkFragment.URL_UPLOAD, contentUri);
            }
        }
    }
    public void dumpFileMetaData(Uri uri) {

        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        Cursor cursor = getActivity().getContentResolver()
                .query(uri, null, null, null, null, null);

        try {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                String displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i("ManageDocument", "Display Name: " + displayName);
                Toast.makeText(getContext(), "File name: " + displayName, Toast.LENGTH_SHORT ).show();
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
                Toast.makeText(getContext(), "File name: " + displayName, Toast.LENGTH_SHORT ).show();
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
