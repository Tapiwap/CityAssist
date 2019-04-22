package android.cloudpoint.com.mapstests;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int PLACE_PICKER_REQUEST = 1;
    private static final int AUTO_COMPLETE_REQUEST = 2;
    public static final int REQUEST_CODE = 1234;
    public static final float DEFAULT_ZOOM = 15f;
    private LatLng latLng;
    private GoogleMap mMap;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private Boolean mLocationPermissionGranted;

    //Widgets
    // private EditText searchTxt;
    private ImageView gps, mPlacePicker;
    private AutocompleteSupportFragment autocompleteSupportFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        gps = (ImageView) findViewById(R.id.ic_gps);
        mPlacePicker = (ImageView) findViewById(R.id.place_picker);
        final String KEY = getApplicationContext().getResources().getString(R.string.google_maps_key);

        //Get location permissions
        getLocationPermission();

        // Use fields to define the data types to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.NAME);

        // Initialize Places.
        Places.initialize(getApplicationContext(), KEY);

        // Create a new Places client instance.
        final PlacesClient placesClient = Places.createClient(this);

        // Use the builder to create a FindCurrentPlaceRequest.
        FindCurrentPlaceRequest request =
                FindCurrentPlaceRequest.builder(placeFields).build();

        // Call findCurrentPlace and handle the response (first check that the user has granted permission).
        findCurrentPlace(request, placesClient);

        setUpAutoCompleteFragment();

        activateSelfLocationListener();
        mPlacePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        initMap();
    }

    private void activateSelfLocationListener() {
        gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });
    }

    /**
     * Finds the latest location of the device hosting the map
     *
     * @param request
     * @param placesClient
     */
    private void findCurrentPlace(FindCurrentPlaceRequest request, PlacesClient placesClient) {
        Log.d(TAG, "findCurrentPlace: Starting current location determination");
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Task<FindCurrentPlaceResponse> placeResponse = placesClient.findCurrentPlace(request);
            placeResponse.addOnCompleteListener(new OnCompleteListener<FindCurrentPlaceResponse>() {
                @Override
                public void onComplete(@NonNull Task<FindCurrentPlaceResponse> task) {
                    if (task.isSuccessful()) {
                        FindCurrentPlaceResponse response = task.getResult();
                        Log.d(TAG, "onComplete: Place Found :");
                        for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                            Log.i(TAG, String.format("Place '%s' has likelihood: %f",
                                    placeLikelihood.getPlace().getName(),
                                    placeLikelihood.getLikelihood()));
                        }
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                        }
                    }
                }
            });
        } else {
            // A local method to request required permissions;
            // See https://developer.android.com/training/permissions/requesting
            getLocationPermission();
        }
    }

    /**
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: Overriding the super onActivityResult method");
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Creates the Auto Complete Fragment as per the Google Places API
     */
    private void setUpAutoCompleteFragment() {
        Log.d(TAG, "setUpAutoCompleteFragment: Setting up the AutocompleteFragment");
        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays
                .asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));

        autocompleteFragment.setCountry("BW");

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "onPlaceSelected: Place : " + place.toString());
                getLocation(place.getLatLng(), place.getName());
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
    }

    /**
     * Assigns / Denies the relevant permissions for the app as per the user
     */
    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: Getting location permissions");
        String[] permissions = {FINE_LOCATION, COURSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                Log.d(TAG, "getLocationPermission: Permissions Granted");
            } else {
                Log.d(TAG, "getLocationPermission: COURSE_LOCATION DENIED");
                ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
            }
        } else {
            Log.d(TAG, "getLocationPermission: FINE_LOCATION DENIED");
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE);
        }
    }

    /**
     * Confirms if the relevant permissions have been assigned for an action
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    /**
     * Initialise the Google Map Fragment
     */
    private void initMap() {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Retrieves the location of the place searched
     *
     * @param latLng       accepts the latitudes and longitude object
     * @param locationName accepts the name of the place searched
     */
    private void getLocation(@NonNull LatLng latLng, String locationName) {
        Log.d(TAG, "getLocation: Locating...");

        if (this.latLng != null) this.latLng = null;

        this.latLng = latLng;
        moveCameramove(this.latLng, DEFAULT_ZOOM, locationName);
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
        Log.d(TAG, "onMapReady: Map is ready");
        mMap = googleMap;
        getLocationPermission();

        if (mLocationPermissionGranted) {
            getDeviceLocation();
            //search();
        }
    }

    /**
     * Retrieves the device's location then shifts the camera
     */
    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: Getting Device Location");
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                Task location = fusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Location Found");
                            Location loc = (Location) task.getResult();
                            LatLng latLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                            moveCameramove(latLng, DEFAULT_ZOOM, "My Location");
                        } else {
                            Log.d(TAG, "onComplete: curent location is null");
                            Toast.makeText(MapsActivity.this, "Unable to get your location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException se) {
            Log.d(TAG, "getDeviceLocation: Security Exception " + se.getMessage());

        }
    }

    /**
     * Moves the camera on the Google Map
     *
     * @param latLong accepts the latitude and longitude object
     * @param zoom    accepts the zooming arguments; how much zoom
     * @param title   accepts a string to set over the markers
     */
    private void moveCameramove(LatLng latLong, float zoom, String title) {
        Log.d(TAG, "moveCameramove: Moving Camera to Location");
        MarkerOptions options = new MarkerOptions().position(latLong).title(title);
        mMap.addMarker(options);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, zoom));

        hideSoftKeyboard();
    }

    /**
     * Hides the keyboard on the screen
     */
    private void hideSoftKeyboard() {
        Log.d(TAG, "hideSoftKeyboard: Hidding the keyboad");
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
