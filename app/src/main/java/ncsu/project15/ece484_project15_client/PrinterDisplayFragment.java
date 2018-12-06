package ncsu.project15.ece484_project15_client;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PrinterDisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PrinterDisplayFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PRINTER = "param1";

    //Callback For MainActivity
    private OnPrinterDisplayInteractionListener mListener;

    // Request code for getting files off device
    private static final int READ_REQUEST_CODE = 42;

    //Printer Used
    private Printer printer;

    //Buttons in the Display
    private Button printButton;
    private Uri contentUri;
    private TextView documentNameText;

    //Toolbar
    Toolbar thisToolbar;

    public PrinterDisplayFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
     * @return A new instance of fragment PrinterSettingsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PrinterDisplayFragment newInstance(Printer printer) {
        PrinterDisplayFragment fragment = new PrinterDisplayFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void setPrinter(Printer printer) {
        this.printer = printer;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof PrinterDisplayFragment.OnPrinterDisplayInteractionListener) {
            mListener = (PrinterDisplayFragment.OnPrinterDisplayInteractionListener) context;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = null;
        setHasOptionsMenu(true);
        if (printer != null) {
            v = inflater.inflate(R.layout.fragment_printer_display, container, false);
            //Set the toolbar
            setToolbar(v);
            // Set Display
            setPrinterDisplay(v);
        }
        return v;
    }

    private void setPrinterDisplay(View v) {
        TextView printerNameText = v.findViewById(R.id.textView_printer_name);
        TextView printerTypeText = v.findViewById(R.id.textView_printer_type);
        TextView printerStatusText = v.findViewById(R.id.textView_printer_status);
        Button chooseDocumentButton = v.findViewById(R.id.button_choose_document);
        documentNameText = v.findViewById(R.id.textView_document_name);
        printButton = v.findViewById(R.id.button_print);

        printerNameText.setText(printer.getName());
        printerTypeText.setText(printer.getPrinterType());
        printerStatusText.setText(printer.getStatus());
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!documentNameText.getText().equals(getString(R.string.text_no_document))) {
                    mListener.onPrinterDisplayInteraction(NetworkFragmentBuilder.build(getActivity().getSupportFragmentManager(),
                            NetworkFragment.URL_UPLOAD, contentUri));
                    AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                    alert.setMessage("You printed: "+documentNameText.getText()+ " to "+printer.getName())
                            .setTitle("Print Success!");
                    alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch(which) {
                                case (DialogInterface.BUTTON_POSITIVE): {
                                    break;
                                }
                            }
                        }
                    });
                    alert.create().show();
                    documentNameText.setText(getString(R.string.text_no_document));
                    enablePrintButton(false);
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
        chooseDocumentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performFileSearch();
            }
        });
    }

    private void setToolbar(View v) {
        Toolbar fragmentToolbar = (Toolbar) v.findViewById(R.id.toolbar);
        Toolbar activityToolbar = (Toolbar) getActivity().findViewById(R.id.my_toolbar);
        activityToolbar.setVisibility(View.GONE);
        thisToolbar = fragmentToolbar;
        ((AppCompatActivity)getActivity()).setSupportActionBar(fragmentToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fragmentToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.toolbar_menu_printer_display, menu);
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
        intent.setType("*/*");
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
                dumpFileMetaData(contentUri);
                enablePrintButton(true);
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
                documentNameText.setText(displayName);
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

    private void enablePrintButton(boolean b) {
        if (b) {
            printButton.setAlpha(1.0f);
            printButton.setClickable(true);
        } else {
            printButton.setAlpha(.5f);
            printButton.setClickable(false);
        }
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
    public interface OnPrinterDisplayInteractionListener {
        // TODO: Update argument type and name
        void onPrinterDisplayInteraction(NetworkFragment networkFragment);
    }
}
