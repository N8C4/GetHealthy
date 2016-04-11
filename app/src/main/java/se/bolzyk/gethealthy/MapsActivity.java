package se.bolzyk.gethealthy;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import java.util.HashSet;
import java.util.Set;


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


    /**
     * Update the map view
     * Setting the camera view
     * @param map the map
     */
    @Override
    @SuppressWarnings("deprecation")
    public void onMapReady(GoogleMap map) {
        mMap = map;
        double myLat = 63.818676;
        double myLon = 20.318376;

        setStartAndEndButton();

        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();

        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        //
        if(!getStartAndEndData("startPosData").contentEquals("") || !getStartAndEndData("endPosData").contentEquals("")) {

            showDistance(parsStartAndEndData(getStartAndEndData("startPosData")), parsStartAndEndData(getStartAndEndData("endPosData")));
        }

        //
        if(!getStartAndEndData("endPosData").isEmpty()) {

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(parsStartAndEndData(getStartAndEndData("endPosData"))).zoom(11).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        } else {

            CameraPosition cameraPosition = new CameraPosition.Builder().target(
                    new LatLng(myLat, myLon)).zoom(11).build();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        setMapValues(mMap);
    }


    /**
     * Save actual position to db
     */
    @SuppressWarnings("deprecation")
    private void setStartAndEndButton(){

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(final Location location) {

                final Button startButton = (Button) findViewById(R.id.startPosition);
                if (startButton != null) {
                    startButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            SharedPreferences st = getSharedPreferences("LastPosition", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = st.edit();
                            editor.putString("startPosData", location.getLatitude() +":"+ location.getLongitude());
                            Log.d("mmmm",""+location.getLatitude() +":"+ location.getLongitude());
                            editor.apply();

                            int duration = Toast.LENGTH_SHORT;
                            Toast toast = Toast.makeText(getBaseContext(), "Start Position Saved", duration);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }
                    });
                }

                final Button endButton = (Button) findViewById(R.id.stopPosition);
                if (endButton != null) {
                    endButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            SharedPreferences st = getSharedPreferences("LastPosition", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = st.edit();
                            editor.putString("endPosData", location.getLatitude() +":"+ location.getLongitude());
                            Log.d("mmmm",""+location.getLatitude() +":"+ location.getLongitude());
                            editor.apply();

                            int duration = Toast.LENGTH_LONG;
                            String msg = "End Position Saved";

                            if(!getStartAndEndData("startPosData").contentEquals("") ||
                                    !getStartAndEndData("endPosData").contentEquals("")) {
                                msg = "The distance where\n"+
                                        showDistance(parsStartAndEndData(getStartAndEndData("startPosData")),
                                                parsStartAndEndData(getStartAndEndData("endPosData")))
                                        +"\nGood job!";
                            }

                            Toast toast = Toast.makeText(getBaseContext(), msg, duration);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL, 0, 0);
                            toast.show();
                        }
                    });
                }
            }
        });
    }


    private void saveDistantDataBase(String distance) {
        SharedPreferences sp = getSharedPreferences("distantCalc", Context.MODE_PRIVATE);
        Set<String> hs = sp.getStringSet("distantCalc", new HashSet<String>());
        Set<String> in = new HashSet<>(hs);
        in.add(distance + ":" + in.size());
        sp.edit().putStringSet("distantCalc", in).apply();
    }


    private String getStartAndEndData(String dataBase) {
        SharedPreferences st = getSharedPreferences("LastPosition", Context.MODE_PRIVATE);
        return st.getString(dataBase,"");
    }


    private LatLng parsStartAndEndData(String mydata) {
        if(!mydata.isEmpty()) {
            String[] myComponents = mydata.split(":");
            Float myLan = Float.parseFloat(myComponents[0]);
            Float myLng = Float.parseFloat(myComponents[1]);
            return (new LatLng(myLan, myLng));
        }
        return (new LatLng(63.818676, 20.318376));
    }


    private String showDistance(LatLng pos1, LatLng pos2) {
        double distance = SphericalUtil.computeDistanceBetween(pos1, pos2);
        saveDistantDataBase(formatNumber(distance));
        Log.d("The length", "" + formatNumber(distance) + " apart.");
        return formatNumber(distance);
    }


    @SuppressLint("DefaultLocale")
    private String formatNumber(double distance) {
        String unit = "m";
        if (distance < 1) {
            distance *= 1000;
            unit = "mm";
        } else if (distance > 1000) {
            distance /= 1000;
            unit = "km";
        }
        return String.format("%4.3f%s", distance, unit);
    }


    /**
     *
     * @param googleMap the maps
     */
    private void setMapValues(GoogleMap googleMap) {
        //Iksu
        googleMap.addMarker(new MarkerOptions().position(new LatLng(63.818676, 20.318376)).title("Iksu Sport")
                .snippet("Monday-Thursday 06:00 - 23:00\nFriday 06:00 - 21:00\nSaturday 07:45 - 19:00\nSunday 07:45 - 23:00"));
        googleMap.addMarker(new MarkerOptions().position(new LatLng(63.821250, 20.275330)).title("Iksu Plus")
                .snippet("Monday-Thursday 06:30 - 22:00\nFriday 06:30 - 21:00\nSaturday 08:00 - 18:00\nSunday 12:00 - 21:00"));
        googleMap.addMarker(new MarkerOptions().position(new LatLng(63.836387, 20.166371)).title("Iksu Spa")
                .snippet("Monday-Thursday 06:30 - 22:00\nFriday 06:30 - 21:00\nSaturday 08:00 - 19:00\nSunday 08:00 - 21:00"));

        //Tenton
        googleMap.addMarker(new MarkerOptions().position(new LatLng(63.845019, 20.328696)).title("Tenton")
                .snippet("Monday-Sunday 06:00 - 22:00"));
        googleMap.addMarker(new MarkerOptions().position(new LatLng(63.827485, 20.253854)).title("Tenton")
                .snippet("Monday-Sunday 06:00 - 22:00"));
        googleMap.addMarker(new MarkerOptions().position(new LatLng(63.857612, 20.313235)).title("Tenton")
                .snippet("Monday-Sunday 06:00 - 22:00"));

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
