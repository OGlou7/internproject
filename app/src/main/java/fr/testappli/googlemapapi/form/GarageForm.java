package fr.testappli.googlemapapi.form;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.api.GarageHelper;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.models.Garage;

public class GarageForm extends BaseActivity {
    private String selectedAddress = "";

    @Override
    public int getFragmentLayout() { return R.layout.activity_profile; }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_garage_form);
        
        Intent intent = getIntent();
        
        String action = intent.getStringExtra("Action");
        switch (action) {
            case "add":
                addGarage();
                break;
            case "update":
                modifyGarage();
                break;
        }
    }

    public void modifyGarage(){

        Bundle bundleGarage = Objects.requireNonNull(getIntent().getExtras()).getBundle("garageClicked");
        Garage garage = (Garage) Objects.requireNonNull(Objects.requireNonNull(bundleGarage).getSerializable("garageClicked"));

        EditText et_description = findViewById(R.id.et_description);
        EditText et_address = findViewById(R.id.et_address);
        EditText et_price = findViewById(R.id.et_price);

        Button b_register = findViewById(R.id.b_register);

        ImageButton ib_cross = findViewById(R.id.ib_cross);
        ib_cross.setOnClickListener(view -> finish());


        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_garage);
        Objects.requireNonNull(Objects.requireNonNull(autocompleteFragment).getView()).setVisibility(View.GONE);

        et_address.setEnabled(false);

        et_description.setText(garage.getDescription());
        et_address.setText(garage.getAddress());
        et_price.setText(String.valueOf(garage.getPrice()));

        b_register.setOnClickListener(view -> {
            if(et_price.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Price is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            updateGarageInFirestore(garage.getUid(), garage.getAddress(), et_description.getText().toString(), Double.valueOf(et_price.getText().toString()));
            finish();
        });
    }


    void addGarage(){

        EditText et_description = findViewById(R.id.et_description);
        EditText et_address = findViewById(R.id.et_address);
        EditText et_price = findViewById(R.id.et_price);

        et_address.setVisibility(View.GONE);

        ImageButton ib_cross = findViewById(R.id.ib_cross);
        ib_cross.setOnClickListener(view -> finish());

        // Initialize the AutocompleteSupportFragment.
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        // Initialize the AutocompleteSupportFragment.
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_garage);

        Objects.requireNonNull(autocompleteFragment).setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

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
                        selectedAddress = addresses.get(0).getAddressLine(0);
                    }
                } catch (Exception e) {
                    Log.e("Error", "Address not found : ");
                }
            }

            @Override
            public void onError(@NonNull Status status) {
                // TODO: Handle the error.
                Log.e("ERROR", "An error occurred: " + status);
            }
        });

        Button b_register = findViewById(R.id.b_register);
        b_register.setOnClickListener(view -> {
            if(selectedAddress.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Address is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if(et_price.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Price is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            createGarageInFirestore(selectedAddress, et_description.getText().toString(), Double.valueOf(et_price.getText().toString()));
            finish();
        });
    }

    // REST REQUEST
    private void createGarageInFirestore(String address, @Nullable String description, double price){
        String uuid = UUID.randomUUID().toString();
        GarageHelper.createGarageForUser(Objects.requireNonNull(getCurrentUser()).getUid(), uuid, address, description, price).addOnFailureListener(this.onFailureListener());
    }

    private void updateGarageInFirestore(String garageID, String address,String description,double price){
        GarageHelper.updateAddress(Objects.requireNonNull(getCurrentUser()).getUid(), garageID, address);
        GarageHelper.updateDescription(getCurrentUser().getUid(), garageID, description);
        GarageHelper.updatePrice(getCurrentUser().getUid(), garageID, price);
    }
}
