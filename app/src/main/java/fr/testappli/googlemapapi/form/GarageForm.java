package fr.testappli.googlemapapi.form;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.libraries.places.api.Places;

import java.util.ArrayList;
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
    private RelativeLayout layoutPeriod;
    private NumberPicker np_starttimepicker;
    private NumberPicker np_endtimepicker;
    private CheckBox cb_rent_all_day;

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
        configureUI();

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

    @SuppressLint("DefaultLocale")
    private void configureUI(){
        layoutPeriod = findViewById(R.id.layoutPeriod);
        layoutPeriod.setVisibility(View.GONE);

        cb_rent_all_day = findViewById(R.id.cb_rent_all_day);
        cb_rent_all_day.setOnClickListener(v -> {
            if(cb_rent_all_day.isChecked()){
                layoutPeriod.setVisibility(View.GONE);
            } else {
                layoutPeriod.setVisibility(View.VISIBLE);
            }
        });

        np_starttimepicker = findViewById(R.id.np_starttimepicker);
        np_endtimepicker = findViewById(R.id.np_endtimepicker);
        // Handle Time Picker
        List<String> displayedValues = new ArrayList<>();
        for(int i=0;i<24;i++){
            displayedValues.add(String.format("%02d:00", i));
            displayedValues.add(String.format("%02d:30", i));
        }
        np_starttimepicker.setMinValue(0);
        np_starttimepicker.setMaxValue(displayedValues.size() - 1);
        np_starttimepicker.setDisplayedValues(displayedValues.toArray(new String[0]));
        np_starttimepicker.setValue(16);

        np_endtimepicker.setMinValue(0);
        np_endtimepicker.setMaxValue(displayedValues.size() - 1);
        np_endtimepicker.setDisplayedValues(displayedValues.toArray(new String[0]));
        np_endtimepicker.setValue(34);

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

        if(!garage.getRentalTime().equals("")) {
            cb_rent_all_day.setChecked(false);
            layoutPeriod.setVisibility(View.VISIBLE);

            int startIndex = (Integer.valueOf(garage.getRentalTime().split("/")[0].split(":")[0]) * 2)
                    + (Integer.valueOf(garage.getRentalTime().split("/")[0].split(":")[1]) == 30 ? 1 : 0);

            int endIndex = (Integer.valueOf(garage.getRentalTime().split("/")[1].split(":")[0]) * 2)
                    + (Integer.valueOf(garage.getRentalTime().split("/")[1].split(":")[1]) == 30 ? 1 : 0);

            np_starttimepicker.setValue(startIndex);
            np_endtimepicker.setValue(endIndex);
        }

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

            String rentalTime = cb_rent_all_day.isChecked() ? "" :
                    np_starttimepicker.getDisplayedValues()[np_starttimepicker.getValue()] + "/" + np_endtimepicker.getDisplayedValues()[np_endtimepicker.getValue()];

            updateGarageInFirestore(garage.getUid(), garage.getAddress(), et_description.getText().toString(), Double.valueOf(et_price.getText().toString()), rentalTime);
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
        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment_garage);

        // Specify the types of place data to return.
        Objects.requireNonNull(autocompleteFragment).setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS));

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) { selectedAddress = place.getAddress(); }

            @Override
            public void onError(@NonNull Status status) { Log.e("ERROR", "An error occurred: " + status); }
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

            Toast.makeText(this, "Garage en location de "
                    + np_starttimepicker.getDisplayedValues()[np_starttimepicker.getValue()] + " à "
                    + np_endtimepicker.getDisplayedValues()[np_endtimepicker.getValue()], Toast.LENGTH_SHORT).show();

            String rentalTime = cb_rent_all_day.isChecked() ? "" :
                    np_starttimepicker.getDisplayedValues()[np_starttimepicker.getValue()] + "/" + np_endtimepicker.getDisplayedValues()[np_endtimepicker.getValue()];

            createGarageInFirestore(selectedAddress, et_description.getText().toString(), Double.valueOf(et_price.getText().toString()), rentalTime);
            finish();
        });
    }

    // REST REQUEST
    private void createGarageInFirestore(String address, @Nullable String description, double price, String rentalTime){
        String uuid = UUID.randomUUID().toString();
        GarageHelper.createGarageForUser(Objects.requireNonNull(getCurrentUser()).getUid(), uuid, address, description, price, rentalTime).addOnFailureListener(this.onFailureListener());
    }

    private void updateGarageInFirestore(String garageID, String address,String description,double price, String rentalTime){
        GarageHelper.updateAddress(Objects.requireNonNull(getCurrentUser()).getUid(), garageID, address);
        GarageHelper.updateDescription(getCurrentUser().getUid(), garageID, description);
        GarageHelper.updatePrice(getCurrentUser().getUid(), garageID, price);
        GarageHelper.updateRentalTime(getCurrentUser().getUid(), garageID, rentalTime);
    }
}

