package ncsu.project15.ece484_project15_client;

import android.net.Network;
import android.support.v4.app.FragmentManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.google.android.gms.maps.model.LatLng;

import ncsu.project15.ece484_project15_client.NetworkFragment;
import ncsu.project15.ece484_project15_client.Printer;

public class NetworkFragmentBuilder {

    static final String URL_KEY = "Url Key";
    static final String DOCUMENT_KEY = "Document Key";
    //private static final String CLIENT_TOKEN_KEY = "Client Token Key";
    static final String PRINTER_NAME_KEY = "Printer Name Key";
    static final String LOCATION_KEY = "Location Key";

    private static final String TAG = "NetworkFragment";

    public static NetworkFragment build(FragmentManager fm, String url) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        networkFragment.setArguments(args);
        fm.beginTransaction().add(networkFragment,TAG).commit();
        fm.executePendingTransactions();
        return networkFragment;
    }

    public static NetworkFragment build(FragmentManager fm, String url, Uri documentUri) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        args.putParcelable(DOCUMENT_KEY, documentUri);
        networkFragment.setArguments(args);
        fm.beginTransaction().add(networkFragment,TAG).commit();
        fm.executePendingTransactions();
        return networkFragment;
    }

    public static NetworkFragment build(FragmentManager fm, String url, Uri documentUri, LatLng location) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        args.putParcelable(DOCUMENT_KEY, documentUri);
        args.putParcelable(LOCATION_KEY, location);
        networkFragment.setArguments(args);
        fm.beginTransaction().add(networkFragment,TAG).commit();
        fm.executePendingTransactions();
        return networkFragment;
    }

    public static NetworkFragment build(FragmentManager fm, String url, String printerName) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        args.putString(PRINTER_NAME_KEY, printerName);
        networkFragment.setArguments(args);
        fm.beginTransaction().add(networkFragment,TAG).commit();
        fm.executePendingTransactions();
        return networkFragment;
    }

    public static NetworkFragment build(FragmentManager fm, String url, Uri documentUri, String printerName) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        args.putParcelable(DOCUMENT_KEY, documentUri);
        args.putString(PRINTER_NAME_KEY, printerName);
        networkFragment.setArguments(args);
        fm.beginTransaction().add(networkFragment,TAG).commit();
        fm.executePendingTransactions();
        return networkFragment;
    }

    public static NetworkFragment build(FragmentManager fm, String url, LatLng location) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        args.putParcelable(LOCATION_KEY, location);
        networkFragment.setArguments(args);
        fm.beginTransaction().add(networkFragment,TAG).commit();
        fm.executePendingTransactions();
        return networkFragment;
    }
}
