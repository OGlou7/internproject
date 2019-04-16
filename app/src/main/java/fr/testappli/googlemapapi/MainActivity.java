package fr.testappli.googlemapapi;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import fr.testappli.googlemapapi.api.GarageHelper;
import fr.testappli.googlemapapi.api.UserHelper;
import fr.testappli.googlemapapi.auth.ProfileActivity;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.chat.ChatActivity;
import fr.testappli.googlemapapi.form.ReservationForm;
import fr.testappli.googlemapapi.garage.GarageActivity;
import fr.testappli.googlemapapi.models.Garage;
import fr.testappli.googlemapapi.models.User;
import fr.testappli.googlemapapi.vendor_chat.VendorChatActivity;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends BaseActivity implements OnMapReadyCallback {

    // PERMISSIONS
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5445;
    private static final String PERMS = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int RC_IMAGE_PERMS = 100;

    // MAP
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentLocationMarker;
    private Location currentLocation;
    private boolean firstTimeFlag = true;
    private ArrayList<Marker> markerList = new ArrayList<>();

    private  BroadcastReceiver broadcastReceiver;

    // UI
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView recyclerView;

    // INTENT REQUEST
    public final static int PROFILEACTIVITY_REQUEST = 2;
    public final static int GARAGEACTIVITY_REQUEST = 3;
    public final static int RESERVATIONFORM_REQUEST = 4;


    private boolean firstCreateFlag = true;
    private int nbOfUser;
    private User modelCurrentUser;
    private ArrayList<Garage> garagesDisplayedInRecyclerView = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        if (!EasyPermissions.hasPermissions(this, PERMS)) {
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_permission_files_access), RC_IMAGE_PERMS, PERMS);
            return;
        }


        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        configureToolbar();
        configureDrawerLayout();
        configureNavigationView();
        configureMap();
        configureMapPointers();
        getCurrentUserFromFirestore();

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
        recyclerView = findViewById(R.id.listView2);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!firstCreateFlag) {
            configureMapPointers();
        }
        if (isGooglePlayServicesAvailable()) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            startCurrentLocationUpdates();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROFILEACTIVITY_REQUEST) {
            if(!firstCreateFlag){
                configureMapPointers();
            }
            Bundle bundleProfile = Objects.requireNonNull(data.getExtras()).getBundle("modelCurrentUser");
            modelCurrentUser = (User) Objects.requireNonNull(Objects.requireNonNull(bundleProfile).getSerializable("modelCurrentUser"));
        }
    }

    @Override
    public void onBackPressed() {
        if (this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
        
    // CONFIGURATION

    void configureMapPointers(){
        // Get All Users
        garagesDisplayedInRecyclerView.clear();
        UserHelper.getUsersCollection().get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        nbOfUser = Objects.requireNonNull(task.getResult()).size();
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            User user = document.toObject(User.class);
                            displayAllAvailableGaragesForUser(user.getUid());
                        }
                    } else {
                        Log.e("configureMapPointers", "Error getting documents: ", task.getException());
                    }
                });
    }

    void displayAllAvailableGaragesForUser(String user_uid){
        // Get All Garages
        GarageHelper.getAllGarageForUser(user_uid).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            Garage garage = document.toObject(Garage.class);
                            if(garage.getisAvailable()) {
                                garagesDisplayedInRecyclerView.add(garage);
                            }
                        }
                    } else {
                        Log.d("displayAllAvailable", "Error getting documents: ", task.getException());
                    }
                    if(task.isComplete()) {
                        if(--nbOfUser == 0){
                            createGarageRecyclerView(garagesDisplayedInRecyclerView);
                            firstCreateFlag=false;
                        }
                    }
                });
    }

    void createGarageRecyclerView(ArrayList<Garage> garageArrayList){
        ArrayList<String> addressToMark = new ArrayList<>();
        for(Garage garage : garageArrayList) addressToMark.add(garage.getAddress());
        setAddressMarkers(addressToMark);

        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);

        recyclerView.setLayoutManager(horizontalLayoutManager);
        MyRecyclerViewAdapter adapter2 = new MyRecyclerViewAdapter(this, garageArrayList);
        // Garage adapter click listener
        adapter2.setClickListener((view, position) -> {
            recyclerView.scrollToPosition(position);
            Garage garageClicked = adapter2.getItem(position);
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(getLatLongFromAddressString(getApplicationContext(),garageClicked.getAddress())));
            Toast.makeText(this, garageClicked.getAddress(), Toast.LENGTH_SHORT).show();
        });

        // Navigation button adaptater click listener
        adapter2.setNavigationClickListener((view, position) -> {
            Garage garageClicked = adapter2.getItem(position);
            /*String url = "http://maps.google.com/maps?saddr=" +
                    currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&daddr=" +
                    garageClicked.getAddress().replace(" ", "+");
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);*/
            Intent calendarActivity = new Intent(MainActivity.this, ReservationForm.class);
            Bundle bundleGarage = new Bundle();
            bundleGarage.putSerializable("garageClicked", garageClicked);
            calendarActivity.putExtra("garageClicked", bundleGarage);
            startActivityForResult(calendarActivity, RESERVATIONFORM_REQUEST);
        });
        recyclerView.setAdapter(adapter2);
    }


    void configureToolbar(){
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
    }


    void configureMap(){
        // Map Fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        assert supportMapFragment != null;
        supportMapFragment.getMapAsync(this);
        // Center the map with current location
        findViewById(R.id.currentLocationImageButton).setOnClickListener(v -> {
            if (v.getId() == R.id.currentLocationImageButton && googleMap != null && currentLocation != null)
                MainActivity.this.animateCamera(currentLocation);
        });

        // Initialize the AutocompleteSupportFragment.
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        Objects.requireNonNull(autocompleteFragment).setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                search(place);
            }

            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.e("ERROR", "An error occurred: " + status);
            }
        });
    }

    private void configureDrawerLayout(){
        this.drawerLayout = findViewById(R.id.activity_main_drawer_layout);
        drawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                    }
                    @Override
                    public void onDrawerOpened(@NonNull View drawerView) {
                        // Respond when the drawer is opened
                    }
                    @Override
                    public void onDrawerClosed(@NonNull View drawerView) {
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

        String uriUserPicture = modelCurrentUser.getUrlPicture();
        Drawable test = getDrawable(R.drawable.ic_profile);

        if(uriUserPicture != null)
            Glide.with(this)
                    .load(Uri.parse(modelCurrentUser.getUrlPicture()))
                    .apply(RequestOptions.circleCropTransform())
                    .into(iv_profile_image);
        else
            Glide.with(this)
                    .load(test)
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
                case R.id.activity_main_drawer_profile:
                    Intent profile = new Intent(MainActivity.this, ProfileActivity.class);
                    Bundle bundleProfile = new Bundle();
                    bundleProfile.putSerializable("modelCurrentUser", modelCurrentUser);
                    profile.putExtra("modelCurrentUser", bundleProfile);
                    startActivityForResult(profile, PROFILEACTIVITY_REQUEST);
                    break;
                case R.id.activity_main_drawer_settings:
                    break;
                case R.id.activity_main_drawer_garages:
                    Intent garageIntent = new Intent(MainActivity.this, GarageActivity.class);
                    startActivityForResult(garageIntent, GARAGEACTIVITY_REQUEST);
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
        UserHelper.getUser(Objects.requireNonNull(getCurrentUser()).getUid()).addOnSuccessListener(documentSnapshot -> modelCurrentUser = documentSnapshot.toObject(User.class));
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
        for(Marker marker : markerList) marker.remove();

        for (String registeredAddresse : registeredAddresses){
            String address = registeredAddresse.split(";")[0];
            LatLng userLatLng = getLatLongFromAddressString(getApplicationContext(), address);
            if(userLatLng != null){
                markerList.add(googleMap.addMarker(new MarkerOptions().position(userLatLng)
                        .title(address)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
            }
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
                if (this.getCurrentUser() != null) {
                    AuthUI.getInstance()
                            .signOut(this)
                            .addOnSuccessListener(command -> finish());
                    UserHelper.deleteUser(this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener());
                    AuthUI.getInstance()
                            .delete(this)
                            .addOnSuccessListener(command -> finish());
                }
                return true;
            case R.id.action_quit:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startVendorChatActivity(){
        //Intent intent = new Intent(this, VendorChatActivity.class);
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        this.googleMap.setOnMarkerClickListener(marker -> {
            if(marker.getTitle() != null && !marker.getTitle().equals("Your Position")) {
                int indexOfGarage = 0;
                for(Garage garage : garagesDisplayedInRecyclerView){
                    if(marker.getTitle().equals(garage.getAddress()))  break;
                    indexOfGarage++;
                }
                recyclerView.scrollToPosition(indexOfGarage);
            }
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
            Address currentAddress = getAddressFromLatLong(getApplicationContext(), currentLocation.getLatitude(), currentLocation.getLongitude());
            //Toast.makeText(getApplicationContext(), Objects.requireNonNull(currentAddress).getAddressLine(0), Toast.LENGTH_SHORT).show();
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
    protected void search(Place place) {
        LatLng latLng = place.getLatLng();

        String addressText = place.getAddress();

        MarkerOptions markerOptions = new MarkerOptions();

        markerOptions.position(Objects.requireNonNull(latLng));
        markerOptions.title(addressText);

        googleMap.addMarker(markerOptions).setTitle(addressText);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

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