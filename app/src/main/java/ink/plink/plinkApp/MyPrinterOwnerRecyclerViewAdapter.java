package ink.plink.plinkApp;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ink.plink.plinkApp.PrinterOwnerFragment.OnPrinterOwnerFragmentInteractionListener;

import java.util.List;

/**
 * specified {@link OnPrinterOwnerFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyPrinterOwnerRecyclerViewAdapter extends RecyclerView.Adapter<MyPrinterOwnerRecyclerViewAdapter.ViewHolder> {

    private final List<Printer> mValues;
    private final OnPrinterOwnerFragmentInteractionListener mListener;

    public MyPrinterOwnerRecyclerViewAdapter(List<Printer> printers, OnPrinterOwnerFragmentInteractionListener listener) {
        mValues = printers;
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_printerowner, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.mPrinter = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getName());
        holder.mContentView.setText(mValues.get(position).getStatusAsString());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onPrinterOwnerFragmentInteraction(holder.mPrinter);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Printer mPrinter;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.item_number);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
