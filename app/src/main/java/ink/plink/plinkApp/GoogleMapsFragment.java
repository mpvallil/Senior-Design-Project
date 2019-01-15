package ink.plink.plinkApp;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

public class GoogleMapsFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    private static final String TAG = "GoogleMapsFragment";
    // Maps variables
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private LocationRequest mLocationRequest;
    Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationClient;
    LocationCallback mLocationCallback;
    CameraPosition position;
    boolean followLocation = true;
    private GoogleMap.OnInfoWindowClickListener mInfoWindowClickListener;
    private GoogleMap.OnMarkerClickListener mMarkerClickListener;

    // Buttons for location
    View myLocationButton;
    // Values for Translation of myLocationButton
    private final float fromY = 0;
    private final float toY = -150;
    private final long DURATION = 200;
    View defaultMyLocationButton;

    // Variables for getting local printers
    Location getLocalPrintersLastLocation;
    ArrayList<Marker> localPrinterMarkers = new ArrayList<Marker>();

    // Listener for sending events back to MainActivity
    private OnMapsInteractionListener mMapsListener;

    // Find Activity's FragmentManager
    FragmentManager fragmentManager;


    public static GoogleMapsFragment newInstance() {
        GoogleMapsFragment fragment = new GoogleMapsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the view
        View mView = inflater.inflate(R.layout.fragment_google_maps, null);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Get the default location button
        defaultMyLocationButton = ((View) mView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));

        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Find the Activity's fragment manager
        fragmentManager = getActivity().getSupportFragmentManager();
        // Get the MapFragment and get the callback for the map
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.google_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set custom MyLocationBUtton
        myLocationButton = view.findViewById(R.id.myLocationButton);
        myLocationButton.setOnClickListener(this);

        // Location Callback. Set activity for when the location updates
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    Log.i("MapsActivity", "Location: " + location.getLatitude() + " " + location.getLongitude());
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (followLocation) {
                        onClick(myLocationButton);
                    }
                    mLastLocation = location;
                    if (getLocalPrintersLastLocation == null || getLocalPrintersLastLocation.distanceTo(location) > 402.336) { // Resends if distance to the previous request location is more than .25 miles
                        getLocalPrintersLastLocation = location;
                        getLocalPrintersRequest(getLocalPrintersLastLocation);
                    }
                }
            }
        };
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Get the map once its ready
        mMap = googleMap;
        // disable default location button, change padding
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.setPadding(0, getResources().getDimensionPixelSize(R.dimen.action_bar_maps_size), 0, 0);
        //Set Map Style
        try {
            // Customise map styling via JSON file
            boolean success = googleMap.setMapStyle( MapStyleOptions.loadRawResourceStyle( getContext(), R.raw.maps_style_json));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        // Set the Location request intervals
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000); // one second interval
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Create an OnCameraIdle for when the camera stops moving
        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                position = mMap.getCameraPosition();
            }
        });

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                followLocation = false;
            }
        });

        mInfoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if (marker.getTag() != null) {
                    Printer printer = (Printer) marker.getTag();
                    mMapsListener.onMapsInteractionSelectPrinter(printer);
                }
            }
        };

        mMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                ObjectAnimator objAnim = ObjectAnimator.ofFloat(myLocationButton, "translationY", 0 , -150);
                objAnim.setDuration(200);
                objAnim.start();
                return false;
            }
        };
        mMap.setOnInfoWindowClickListener(mInfoWindowClickListener);
        mMap.setOnMarkerClickListener(mMarkerClickListener);
        mMap.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(Marker marker) {
                // Do whatever you want to do here...
                ObjectAnimator objAnim = ObjectAnimator.ofFloat(myLocationButton, "translationY", toY , fromY);
                objAnim.setDuration(DURATION);
                objAnim.start();
            }
        });

        // Check for location permissions
        checkLocationPermissionMethod();


    }

    public void getLocalPrintersRequest(Location location) {
        mMapsListener.onMapsInteractionGetLocalPrinters(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    public void getLocalPrinters(String printers) {
        Printer[] localPrinters = Printer.getPrinterList(printers);
        for (Printer printer : localPrinters) {
            createPrinterMarker(printer);
        }
    }

    private void createPrinterMarker(Printer printer) {
        Marker m = mMap.addMarker(new MarkerOptions()
                .position(printer.getLocation())
                .title(printer.getName())
                .snippet("Click for more info!")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET)));
        m.setTag(printer);
        localPrinterMarkers.add(m);
    }

    public void checkLocationPermissionMethod() {

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission has already been granted
            mFusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                    }
                }
            });
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
            mMap.setMyLocationEnabled(true);
        } else {
            //Request Location Permission
            checkLocationPermission();
        }
    }

    public static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_ACCESS_FINE_LOCATION );
                            }
                        })
                        .create()
                        .show();
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_ACCESS_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getActivity(), "permission denied", Toast.LENGTH_LONG).show();
                }
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMapsInteractionListener) {
            mMapsListener = (OnMapsInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMapsInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mMapsListener = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        // Remove Location updates when onPause is called
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }


    @Override
    public void setUserVisibleHint(boolean visibleHint) {
        // Resume Location updates when onResume is called
        if (visibleHint) {
            if (mMap != null) {
                checkLocationPermission();
                Log.i("setUserVisible hint", "true");
            }
        } else {
            // Remove Location updates when onPause is called
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
            Log.i("setUserVisible hint", "false");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            // Set click behavior for location button
            case R.id.myLocationButton: {
                if(mMap != null) {
                    if(defaultMyLocationButton != null) {
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        defaultMyLocationButton.callOnClick();
                        mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        followLocation = true;
                    }
                }
            }
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
    public interface OnMapsInteractionListener {
        // TODO: Update argument type and name
        void onMapsInteractionSelectPrinter(Printer printer);
        void onMapsInteractionGetLocalPrinters(LatLng location);
    }
}
