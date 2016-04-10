package se.bolzyk.gethealthy;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;

import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MapsActivity  extends AppCompatActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

/**
 * Request code for location permission request.
 *
 * @see #onRequestPermissionsResult(int, String[], int[])
 */
private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

/**
 * Flag indicating whether a requested permission has been denied after returning in
 * {@link #onRequestPermissionsResult(int, String[], int[])}.
 */
private boolean mPermissionDenied = false;

private GoogleMap mMap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment =
        (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        FloatingActionButton fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        FloatingActionButton fab3 = (FloatingActionButton) findViewById(R.id.fab3);

        //Create and set float buttons for fast navigation
        createAndSetFloatingButton(fab1, MainActivity.class);
        createAndSetFloatingButton(fab2, AddActivity.class);
        createAndSetFloatingButton(fab3, MapsActivity.class);

    }


    /**
     * Method that sets fast navigation float buttons by creating a button listener
     * @param fab the layout id of FloatingActionButton
     * @param myActivity the class that should be called when the button is pressed
     */
    private void createAndSetFloatingButton(FloatingActionButton fab, final Class myActivity) {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getBaseContext(), myActivity);
                startActivity(intent);
            }
        });
    }


    @Override
    @SuppressWarnings("deprecation")
    public void onMapReady(GoogleMap map) {
        mMap = map;

        mMap.setOnMyLocationButtonClickListener(this);

        enableMyLocation();

        //use my location to zoom in of the area
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 12));
            }
        });

        setMapValues(mMap);
    }


    private void setMapValues(GoogleMap googleMap) {
        //Iksu
        googleMap.addMarker(new MarkerOptions().position(new LatLng(63.818676, 20.318376)).title("Iksu Sport"));
        googleMap.addMarker(new MarkerOptions().position(new LatLng(63.821250, 20.275330)).title("Iksu Plus"));
        googleMap.addMarker(new MarkerOptions().position(new LatLng(63.836387, 20.166371)).title("Iksu Spa"));

        //Tenton
        googleMap.addMarker(new MarkerOptions().position(new LatLng(63.845019, 20.328696)).title("Tenton"));
        googleMap.addMarker(new MarkerOptions().position(new LatLng(63.827485, 20.253854)).title("Tenton"));
        googleMap.addMarker(new MarkerOptions().position(new LatLng(63.857612, 20.313235)).title("Tenton"));


        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }


    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
            Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }


    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults, Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }


    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }


    /**
    * Displays a dialog with error message explaining that the location permission is missing.
    */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog.newInstance(true).show(getSupportFragmentManager(), "dialog");
    }


}
