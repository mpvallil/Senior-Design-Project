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

    private static final String URL_KEY = "Url Key";
    private static final String DOCUMENT_KEY = "Document Key";
    //private static final String CLIENT_TOKEN_KEY = "Client Token Key";
    private static final String PRINTER_KEY = "Printer Key";
    private static final String LOCATION_KEY = "Location Key";

    public static NetworkFragment build(FragmentManager fm, String url) {
        NetworkFragment networkFragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        networkFragment.setArguments(args);
        fm.beginTransaction().add(networkFragment,"NetworkFragment").commit();
        fm.executePendingTransactions();
        return networkFragment;
    }

    public Bundle build(String url, Uri documentUri) {
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        args.putParcelable(DOCUMENT_KEY, documentUri);
        return args;
    }

    public Bundle build(String url, Uri documentUri, LatLng location) {
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        args.putParcelable(DOCUMENT_KEY, documentUri);
        args.putParcelable(LOCATION_KEY, location);
        return args;
    }
}
