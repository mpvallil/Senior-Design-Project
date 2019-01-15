package ink.plink.plinkApp;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;

public class PrinterFilterFragment extends DialogFragment {

    // Use this instance of the interface to deliver action events
    PrinterFilterFragmentListener mListener;
    private boolean isClear;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.fragment_printer_filter, null))
                // Add action buttons
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onPrinterFilterPositiveClick(PrinterFilterFragment.this);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PrinterFilterFragment.this.getDialog().hide();
                    }
                })
                .setNeutralButton("Clear", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isClear = true;
                        mListener.onPrinterFilterClearClick(PrinterFilterFragment.this);
                    }
                })
                .setTitle("Filter By");
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (isClear) {
            super.onSaveInstanceState(outState);
            isClear = false;
        } else {
            //TODO: Save all Filter fields
        }
    }

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface PrinterFilterFragmentListener {
        void onPrinterFilterPositiveClick(DialogFragment dialog);
        void onPrinterFilterClearClick(DialogFragment dialog);
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PrinterFilterFragmentListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement PrinterFilterFragmentListener");
        }
    }
}
