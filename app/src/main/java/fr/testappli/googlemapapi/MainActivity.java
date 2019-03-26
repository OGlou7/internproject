package fr.testappli.googlemapapi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import fr.testappli.googlemapapi.auth.ProfileActivity;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.vendor_chat.VendorChatActivity;

//TODO: URGENT & IMPORTANT remettre 'public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {'
public class MainActivity extends BaseActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 5445;

    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker currentLocationMarker;
    private Location currentLocation;
    private Address currentAddress;
    private boolean firstTimeFlag = true;
    private AutocompleteSupportFragment autocompleteFragment;

    private ImageView img_maneuver;
    private TextView tv_maneuver;

    private JSONArray legs;
    private JSONArray steps;
    private ArrayList<String> maneuver = new ArrayList<>();
    private ArrayList<String> html_instructions = new ArrayList<>();

    private ArrayList<Reservation> reservationArrayList = new ArrayList<>();

    private  BroadcastReceiver broadcastReceiver;

    // L'identifiant de notre requête
    public final static int CALENDARACTIVITY_REQUEST = 1;
    public final static int PROFILEACTIVITY_REQUEST = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Log.e("TESTTEST","MainActivity create");
        // Map Fragment
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);

        // Maneuvers Display and controls
        tv_maneuver = findViewById(R.id.tv_manouver);
        img_maneuver = findViewById(R.id.im_manouver);
        tv_maneuver.setVisibility(View.GONE);
        img_maneuver.setVisibility(View.GONE);

        ImageView imageReturn = findViewById(R.id.switch_mode);
        imageReturn.setOnClickListener(v -> {
            Intent calendarActivity = new Intent(MainActivity.this, CalendarActivity.class);
            startActivityForResult(calendarActivity, CALENDARACTIVITY_REQUEST);
        });

        // Handle Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        // Handle map configuration
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

        Objects.requireNonNull(autocompleteFragment.getView()).setVisibility(View.GONE);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                String placeToSearch = place.getName();
                Log.e("testtest8", placeToSearch);

                Geocoder geocoder = new Geocoder(getBaseContext());
                List<Address> addresses;

                try {
                    // Getting a maximum of 3 Addresses that matches the input text
                    addresses = geocoder.getFromLocationName(placeToSearch, 2);
                    if (addresses != null && !addresses.isEmpty())
                    {
                        search(addresses);
                        Log.e("testtest8", addresses.toString());
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

        // Handle receiver to finish activity
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action.equals("finish")){
                    finish();
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("finish"));

        // TODO: get garage  from fatebase
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        Date startdate = null;
        Date enddate = null;
        try {
            startdate = format.parse("2019-03-20T12:30Z");
            enddate = format.parse("2019-03-22T12:30Z");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        reservationArrayList.add(new Reservation("119 Chemin de la hunière, Palaiseau, France", startdate, enddate, "bla", 2.4));
        reservationArrayList.add(new Reservation("101 Chemin de la hunière, Palaiseau, France", startdate, enddate, "bla", 2.5));
        reservationArrayList.add(new Reservation("89 rue des maraichers, Villebon-sur-Yvette, France", startdate, enddate, "bla", 2.6));
        reservationArrayList.add(new Reservation("Paris, Paris, France", startdate, enddate, "bla", 2.7));

        // set up the RecyclerView
        RecyclerView recyclerView = findViewById(R.id.listView2);
        LinearLayoutManager horizontalLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(horizontalLayoutManager);

        // set up horizontal list
        MyRecyclerViewAdapter adapter2 = new MyRecyclerViewAdapter(this, reservationArrayList);
        adapter2.setClickListener((view, position) -> {
            Reservation reservationClicked = adapter2.getItem(position);
            //view.findViewById(R.id.parent).setBackgroundColor(getColor(R.color.myOrange));
            view.findViewById(R.id.iv_navigate).setOnClickListener(v -> {
                //recyclerView.setVisibility(View.GONE);
                postDirectionRequestFromLatLong(getLatLongFromAddressString(getApplicationContext(), reservationClicked.getCompleteAddress()));
            });
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(getLatLongFromAddressString(getApplicationContext(),reservationClicked.getCompleteAddress())));
            Toast.makeText(this, reservationClicked.getAddress(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setAdapter(adapter2);
    }

    @Override
    public int getFragmentLayout() { return R.layout.activity_profile; }

    public void postDirectionRequestFromLatLong(LatLng latLng){
        String url = "https://maps.googleapis.com/maps/api/directions/json?&origin=" +
                currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&destination=" +
                latLng.latitude + "," + latLng.longitude + "&travelmode=driving" +
                "&key=" + getString(R.string.google_maps_key);
        Log.d("onMapClick", url);

        // Start downloading json data from Google Directions API
        FetchUrl FetchUrl = new FetchUrl();
        FetchUrl.execute(url);
        //move map camera
        googleMap.clear();
        setAddressMarkers();
        googleMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                .title("Your Position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(getLatLongFromAddressString(getApplicationContext(),currentAddress.getAddressLine(0))));
    }


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


    public void setAddressMarkers(){
        // Setting the position of the marker
        String[] registeredAddresses = getResources().getStringArray(R.array.Address_client);

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
        setAddressMarkers();

        this.googleMap.setOnMarkerClickListener(marker -> {
            if(marker.getTitle() != null && !marker.getTitle().equals("Your Position"))
                postDirectionRequestFromLatLong(getLatLongFromAddressString(getApplicationContext(), marker.getTitle()));
            return false;
        });
    }



    @SuppressLint("StaticFieldLeak")
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }

        private String downloadUrl(String strUrl) throws IOException {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                Log.d("downloadUrl", data);
                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                assert iStream != null;
                iStream.close();
                urlConnection.disconnect();
            }
            return data;
        }
    }



//    @SuppressLint("StaticFieldLeak")
//    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
//        ImageView bmImage;
//
//        // CONSTRUCTOR
//        DownloadImageTask(ImageView bmImage) {
//            this.bmImage = bmImage;
//        }
//
//        // DO IN BACKGROUND
//        protected Bitmap doInBackground(String... urls) {
//            String urldisplay = urls[0];
//            Bitmap mIcon11 = null;
//            try {
//                InputStream in = new java.net.URL(urldisplay).openStream();
//                mIcon11 = BitmapFactory.decodeStream(in);
//            } catch (Exception e) {
//                Log.e("Error", e.getMessage());
//                e.printStackTrace();
//            }
//            return mIcon11;
//        }
//
//        // ON POST-EXECUTE
//        protected void onPostExecute(Bitmap result) {
//            bmImage.setImageBitmap(result);
//        }
//    }





    @SuppressLint("StaticFieldLeak")
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask",jsonData[0]);
                DataParser parser = new DataParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                MapResult mapResult = new MapResult(parser.parse(jObject));
                //routes = parser.parse(jObject);

                routes = mapResult.getRoutes();
                legs = mapResult.getLegs();
                steps = mapResult.getSteps();
                maneuver = mapResult.getManeuver();

                for(int i=0;i<steps.length();i++)
                    html_instructions.add(( (JSONObject)steps.get(i)).getString("html_instructions"));

                Log.e("TESTTEST10", html_instructions.toString());

                Log.d("ParserTask","Executing routes");
                Log.d("ParserTask",routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(Objects.requireNonNull(point.get("lat")));
                    double lng = Double.parseDouble(Objects.requireNonNull(point.get("lng")));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);

                Log.d("onPostExecute","onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                //googleMap.clear();
                googleMap.addPolyline(lineOptions);
            }
            else {
                Log.d("onPostExecute","without Polylines drawn");
            }
        }
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
    protected void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            startCurrentLocationUpdates();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
        fusedLocationProviderClient = null;
        googleMap = null;
    }
}