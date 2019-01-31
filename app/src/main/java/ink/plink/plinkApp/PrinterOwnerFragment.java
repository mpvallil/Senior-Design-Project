package ink.plink.plinkApp;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fragment representing a recyclerView of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnPrinterOwnerFragmentInteractionListener}
 * interface.
 */
public class PrinterOwnerFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnPrinterOwnerFragmentInteractionListener mListener;
    List<Printer> ownerPrinterList;
    RecyclerView recyclerView;
    ProgressBar progressBar;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PrinterOwnerFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static PrinterOwnerFragment newInstance(int columnCount) {
        PrinterOwnerFragment fragment = new PrinterOwnerFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_printerowner_list, container, false);
        setToolbar(view);
        recyclerView = view.findViewById(R.id.list);
        // Set the adapter
        if (recyclerView != null) {
            Context context = view.getContext();
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            progressBar = view.findViewById(R.id.progressBar);
            getPrintersByOwnerList();
        }
        return view;
    }

    private void getPrintersByOwnerList() {
        NetworkFragment.getGetPrintersByOwnerInstance(getChildFragmentManager()).startDownload();
    }

    private void setToolbar(View v) {
        setHasOptionsMenu(true);
        Toolbar fragmentToolbar = (Toolbar) v.findViewById(R.id.toolbar);
        Toolbar activityToolbar = (Toolbar) getActivity().findViewById(R.id.my_toolbar);
        activityToolbar.setVisibility(View.GONE);
        ((AppCompatActivity)getActivity()).setSupportActionBar(fragmentToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fragmentToolbar.setTitle(R.string.nav_drawer_Owner_manage);
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
        inflater.inflate(R.menu.toolbar_menu_printer_owner, menu);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnPrinterOwnerFragmentInteractionListener) {
            mListener = (OnPrinterOwnerFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.action_refresh: {
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setAdapter(null);
                getPrintersByOwnerList();
            }
            case R.id.action_add_printer: {
                
            }
        }
        return false;
    }

    public void setPrinterList(String printerJSON) {
        if (printerJSON.startsWith("[")) {
            ownerPrinterList = new ArrayList<>(Arrays.asList(Printer.getPrinterList(printerJSON)));
        } else {
            ownerPrinterList = new ArrayList<>();
        }
        ownerPrinterList.add(new Printer().setName("Test Printer List 1").setStatus(true));
        ownerPrinterList.add(new Printer().setName("Test Printer List 2").setStatus(false));
        progressBar.setVisibility(View.GONE);
        recyclerView.setAdapter(new MyPrinterOwnerRecyclerViewAdapter(ownerPrinterList, mListener));
    }

    public void showPrinterSelected(Printer printer) {
        FragmentManager fm = getFragmentManager();
        PrinterSettingsFragment frag = new PrinterSettingsFragment();
        frag.setPrinter(printer);
        fm.beginTransaction()
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .replace(R.id.frameLayout_printer_settings, frag, MainActivity.TAG_PRINTER_SETTINGS_FRAGMENT)
                .addToBackStack(null)
                .commit();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnPrinterOwnerFragmentInteractionListener {
        // TODO: Update argument type and name
        void onPrinterOwnerFragmentInteraction(Printer printer);
        void onPrinterOwnerFragmentGetPrinters();
    }
}
