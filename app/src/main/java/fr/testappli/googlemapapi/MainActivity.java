package fr.testappli.googlemapapi;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import fr.testappli.googlemapapi.api.GarageHelper;
import fr.testappli.googlemapapi.api.UserHelper;
import fr.testappli.googlemapapi.auth.ProfileActivity;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.garage.GarageActivity;
import fr.testappli.googlemapapi.models.Garage;
import fr.testappli.googlemapapi.models.User;
import fr.testappli.googlemapapi.vendor_chat.VendorChatActivity;

public class MainActivity extends BaseActivity implements OnMapReadyCallback {

    // PERMISSIONS
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5445;

    // MAP
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentLocationMarker;
    private Location currentLocation;
    private Address currentAddress;
    private boolean firstTimeFlag = true;
    private AutocompleteSupportFragment autocompleteFragment;


    private  BroadcastReceiver broadcastReceiver;

    // UI
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    // INTENT REQUEST
    public final static int CALENDARACTIVITY_REQUEST = 1;
    public final static int PROFILEACTIVITY_REQUEST = 2;

    private User modelCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        getCurrentUserFromFirestore();
        configureToolbar();
        configureDrawerLayout();
        configureNavigationView();
        configureMap();

        // Handle receiver to finish activity
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(Objects.requireNonNull(action).equals("finish")){
                    finish();
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("finish"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        configureMapPointers();
        if (isGooglePlayServicesAvailable()) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            startCurrentLocationUpdates();
        }
    }
        
    // CONFIGURATION

    void configureMapPointers(){
        // Get All Users
        UserHelper.getUsersCollection().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            User user = document.toObject(User.class);
                            Log.e("testtestUSER", user.getUsername());
                            displayAllAvailableGaragesForUser(user.getUid());
                        }
                    } else {
                        Log.e("testtest", "Error getting documents: ", task.getException());
                    }
                });
    }

    void displayAllAvailableGaragesForUser(String user_uid){
        // Get All Garages
        ArrayList<Garage> garageArrayList = new ArrayList<>();
        GarageHelper.getAllGarageForUser(user_uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Garage garage = document.toObject(Garage.class);
                            if(garage.getisAvailable()) {
                                Log.e("testtestGARAGE", garage.getAddress());
                                garageArrayList.add(garage);
                            }
                        }
                    } else {
                        Log.d("testtest", "Error getting documents: ", task.getException());
                    }
                    configureListRecyclerView(garageArrayList);
                    ArrayList<String> addressList = new ArrayList<>();
                    for(Garage garage : garageArrayList)
                        addressList.add(garage.getAddress());
                    setAddressMarkers(addressList);
                });
    }

    void configureToolbar(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }

    void configureListRecyclerView(ArrayList<Garage> garageArrayList){
        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.listView2);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);

        // set up horizontal list
        MyRecyclerViewAdapter adapter2 = new MyRecyclerViewAdapter(this, garageArrayList);
        adapter2.setClickListener((view, position) -> {
            Garage garageClicked = adapter2.getItem(position);
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(getLatLongFromAddressString(getApplicationContext(),garageClicked.getAddress())));
            Toast.makeText(this, garageClicked.getAddress(), Toast.LENGTH_SHORT).show();
        });

        adapter2.setNavigationClickListener((view, position) -> {
            Garage garageClicked = adapter2.getItem(position);
            String url = "http://maps.google.com/maps?saddr=" +
                    currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&daddr=" +
                    garageClicked.getAddress().replace(" ", "+");
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter2);
    }

    void configureMap(){
        // Map Fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(this);
        findViewById(R.id.currentLocationImageButton).setOnClickListener(currentLocationClickListener);

        // Initialize the AutocompleteSupportFragment.
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        assert autocompleteFragment != null;
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        //Objects.requireNonNull(autocompleteFragment.getView()).setVisibility(View.GONE);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                String placeToSearch = place.getName();

                Geocoder geocoder = new Geocoder(getBaseContext());
                List<Address> addresses;

                try {
                    // Getting a maximum of 3 Addresses that matches the input text
                    addresses = geocoder.getFromLocationName(placeToSearch, 2);
                    if (addresses != null && !addresses.isEmpty()){
                        search(addresses);
                    }
                    Objects.requireNonNull(autocompleteFragment.getView()).setVisibility(View.GONE);
                } catch (Exception e) {
                    Log.e("Error", "Address not found : ");
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                // Handle the error.
                Log.i("Error", "An error occurred: " + status);
            }
        });
    }

    private void configureDrawerLayout(){
        this.drawerLayout = findViewById(R.id.activity_main_drawer_layout);
        drawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                    }
                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Respond when the drawer is opened
                    }
                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                    }
                    @Override
                    public void onDrawerStateChanged(int newState) {
                        if(newState>0)
                            setOwnerPartVisibleDrawer();
                        // Respond when the drawer motion state changes
                    }
                }
        );

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    void setOwnerPartVisibleDrawer(){
        navigationView.getMenu().findItem(R.id.activity_main_drawer_owner_part).setVisible((Objects.requireNonNull(modelCurrentUser).getIsVendor()));

        ImageView iv_profile_image = findViewById(R.id.iv_profile_image);
        //iv_profile_image.setImageURI(getCurrentUser().getPhotoUrl());

        Glide.with(this)
                .load(getCurrentUser().getPhotoUrl())
                .apply(RequestOptions.circleCropTransform())
                .into(iv_profile_image);

        TextView tv_username = findViewById(R.id.tv_username);
        tv_username.setText(modelCurrentUser.getUsername());
    }

    private void configureNavigationView(){
        this.navigationView = findViewById(R.id.activity_main_nav_view);

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();

            switch (id){
                case R.id.activity_main_drawer_map :
                    break;
                case R.id.activity_main_drawer_profile:
                    Intent profile = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivityForResult(profile, PROFILEACTIVITY_REQUEST);
                    break;
                case R.id.activity_main_drawer_settings:
                    break;
                case R.id.activity_main_drawer_garages:
                    Intent garageIntent = new Intent(MainActivity.this, GarageActivity.class);
                    startActivityForResult(garageIntent, PROFILEACTIVITY_REQUEST);
                    break;
                case R.id.activity_main_drawer_overview:
                    break;
                case R.id.activity_main_drawer_chats:
                    if (this.isCurrentUserLogged()){
                        this.startVendorChatActivity();
                    } else {
                        Toast.makeText(this, getString(R.string.error_not_connected), Toast.LENGTH_SHORT).show();
                    }
                    break;
                default:
                    break;
            }
            this.drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // Get Current User from Firestore
    private void getCurrentUserFromFirestore(){
        UserHelper.getUser(getCurrentUser().getUid()).addOnSuccessListener(documentSnapshot -> modelCurrentUser = documentSnapshot.toObject(User.class));
    }

    @Override
    public int getFragmentLayout() { return R.layout.activity_profile; }

    // UTILS
    
    // Method to get address from latitude and longitude
    public static LatLng getLatLongFromAddressString(Context context, String address) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        List<Address> addresses;

        try {
            // Getting a maximum of 2 Address that matches the input text
            addresses = geocoder.getFromLocationName(address, 2);
            if (addresses != null && !addresses.isEmpty()) {
                // Add a marker
                return new LatLng(addresses.get(0).getLatitude(), addresses.get(0).getLongitude());
            }
        } catch (Exception e) {
            Log.e("EXCEPTION ADDRESS", "Address not found : ",e);
        }
        return null;
    }

    public void setAddressMarkers(ArrayList<String> addressMarkersAddress){
        // Setting the position of the marker
        String[] registeredAddresses = addressMarkersAddress.toArray(new String[0]);

        for (String registeredAddresse : registeredAddresses){
            String address = registeredAddresse.split(";")[0];
            LatLng userLatLng = getLatLongFromAddressString(getApplicationContext(), address);
            if(userLatLng != null){
                googleMap.addMarker(new MarkerOptions().position(userLatLng)
                        .title(address)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            }
        }
    }
    
    
    @Override
    public void onBackPressed() {
        // close navigationDrawer
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Toast.makeText(getApplicationContext(), "Search", Toast.LENGTH_SHORT).show();
                if(autocompleteFragment.isVisible())
                    Objects.requireNonNull(autocompleteFragment.getView()).setVisibility(View.GONE);
                else
                    Objects.requireNonNull(autocompleteFragment.getView()).setVisibility(View.VISIBLE);
                return true;
            case R.id.action_chat:
                if (this.isCurrentUserLogged()){
                    this.startVendorChatActivity();
                } else {
                    Toast.makeText(this, getString(R.string.error_not_connected), Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.action_garage_list:
                Intent garageIntent = new Intent(MainActivity.this, GarageActivity.class);
                startActivityForResult(garageIntent, PROFILEACTIVITY_REQUEST);
                return true;
            case R.id.action_profile:
                Intent profile = new Intent(MainActivity.this, ProfileActivity.class);
                startActivityForResult(profile, PROFILEACTIVITY_REQUEST);
                return true;
            case R.id.action_quit:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startVendorChatActivity(){
        Intent intent = new Intent(this, VendorChatActivity.class);
        startActivity(intent);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        //setAddressMarkers();

        this.googleMap.setOnMarkerClickListener(marker -> {
            if(marker.getTitle() != null && !marker.getTitle().equals("Your Position"))
                //TODO: AFFICHER LES DETAIL DU GARAGE ICI
                Log.e("TESTTEST444", marker.getTitle());
            return false;
        });
    }

    // Update current position every 3 seconds
    private void startCurrentLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(50000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                return;
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
    }

    // Callback when position updated
    private final LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() == null)
                return;
            currentLocation = locationResult.getLastLocation();
            if (firstTimeFlag && googleMap != null) {
                animateCamera(currentLocation);
                firstTimeFlag = false;
            }
            showMarker(currentLocation);
            currentAddress = getAddressFromLatLong(getApplicationContext(),currentLocation.getLatitude(),currentLocation.getLongitude());
            Toast.makeText(getApplicationContext(), currentAddress.getAddressLine(0), Toast.LENGTH_SHORT).show();
        }
    };

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status)
            return true;
        else {
            if (googleApiAvailability.isUserResolvableError(status))
                Toast.makeText(this, "Please Install google play services to use this application", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    // Handle permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(this, "Permission denied by uses", Toast.LENGTH_SHORT).show();
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                startCurrentLocationUpdates();
        }
    }


    // Method to get address from latitude and longitude
    public static Address getAddressFromLatLong(Context context, double lat, double lng) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());

        List<Address> list;
        try {
            list = geocoder.getFromLocation(lat, lng, 10);
            return list.get(0);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }


    // Method to add marker on map on the given address
    protected void search(List<Address> addresses) {

        Address address = addresses.get(0);

        LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

        String addressText = String.format(
                "%s, %s",
                address.getMaxAddressLineIndex() > 0 ? address
                        .getAddressLine(0) : "", address.getCountryName());

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(latLng);
        markerOptions.title(addressText);

        Log.e("TEST8TEST", latLng.toString());
        //googleMap.clear();
        googleMap.addMarker(markerOptions).setTitle(addresses.get(0).getAddressLine(0));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    // Center the map with current location
    private final View.OnClickListener currentLocationClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.currentLocationImageButton && googleMap != null && currentLocation != null)
                MainActivity.this.animateCamera(currentLocation);
        }
    };


    private void showMarker(@NonNull Location currentLocation) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (currentLocationMarker == null)
            currentLocationMarker = googleMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.defaultMarker()).position(latLng));
        else
            MarkerAnimation.animateMarkerToGB(currentLocationMarker, latLng, new LatLngInterpolator.Spherical());
    }

    private void animateCamera(@NonNull Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(latLng)));
    }

    @NonNull
    private CameraPosition getCameraPositionWithBearing(LatLng latLng) {
        return new CameraPosition.Builder().target(latLng).zoom(16).build();
    }

    @Override
    protected void onPause(){
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        fusedLocationProviderClient = null;
        googleMap = null;
    }
}