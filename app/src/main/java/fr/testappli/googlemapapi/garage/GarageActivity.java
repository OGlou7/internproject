package fr.testappli.googlemapapi.garage;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import fr.testappli.googlemapapi.CalendarActivity;
import fr.testappli.googlemapapi.MainActivity;
import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.api.GarageHelper;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.models.Garage;
import fr.testappli.googlemapapi.models.NonAvailableTime;
import fr.testappli.googlemapapi.week.WeekViewEvent;

import static fr.testappli.googlemapapi.week.WeekActivity.dateToCalendar;

public class GarageActivity extends BaseActivity {
    private static final String TAG = "GarageActivityLog";
    private PopupWindow mPopupWindow;
    private RelativeLayout mRelativeLayout;
    public final static int CALENDARACTIVITY_REQUEST = 1;


    // FOR DESIGN
    RecyclerView recyclerView;

    // FOR DATA
    private GarageListAdapter garageListAdapter;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart(){
        super.onStart();
        garageListAdapter.startListening();
    }
    @Override
    protected void onStop(){
        super.onStop();
        garageListAdapter.stopListening();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_garage_list);
        mRelativeLayout = findViewById(R.id.garageMainLayout);
        recyclerView = findViewById(R.id.listViewGarage);

        // setup the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setOnClickListener(v -> this.addGarage());

        // discard changes
        toolbar.setNavigationOnClickListener(v -> finish());

        this.configureRecyclerView(getCurrentUser().getUid());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public int getFragmentLayout() { return R.layout.activity_profile; }

    public void addGarage(){
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.new_garage_form,null);

        mPopupWindow = new PopupWindow(
                customView,
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT
        );

        mPopupWindow.setFocusable(true);
        mPopupWindow.update();

        EditText et_description = customView.findViewById(R.id.et_description);
        EditText et_address = customView.findViewById(R.id.et_address);
        EditText et_country = customView.findViewById(R.id.et_country);
        EditText et_city = customView.findViewById(R.id.et_city);
        EditText et_price = customView.findViewById(R.id.et_price);

        Button b_register = customView.findViewById(R.id.b_register);
        b_register.setOnClickListener(view -> {
            if(et_address.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Address is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if(et_country.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Country is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if(et_city.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "City is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            if(et_price.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "City is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            createGarageInFirestore(et_address.getText().toString(), et_description.getText().toString(), Double.valueOf(et_price.getText().toString()));
            mPopupWindow.dismiss();
        });

        ImageButton ib_cross = customView.findViewById(R.id.ib_cross);
        ib_cross.setOnClickListener(view -> mPopupWindow.dismiss());

        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.showAtLocation(mRelativeLayout, Gravity.CENTER,0,0);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }

    // REST REQUEST
    private void createGarageInFirestore(String address, @Nullable String description, double price){
        String uuid = UUID.randomUUID().toString();
        GarageHelper.createGarageForUser(getCurrentUser().getUid(), uuid, address, description, price).addOnFailureListener(this.onFailureListener());
    }

    // UI
    private void configureRecyclerView(String userID){
        this.garageListAdapter = new GarageListAdapter(generateOptionsForAdapter(GarageHelper.getAllGarageForUser(userID)), item -> {
//            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
//            Date startdate = null;
//            Date enddate = null;
//            try {
//                startdate = format.parse("2019-07-28T12:30Z");
//                enddate = format.parse("2019-07-29T12:30Z");
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            WeekViewEvent event0 = new WeekViewEvent(startdate.getTime(), "Tag de la location : ", "Adresse du Garage, Ville", dateToCalendar(startdate), dateToCalendar(enddate));
//            GarageHelper.updateListDateNonDispo(getCurrentUser().getUid(), item.getUid(), new NonAvailableTime(event0));

            Intent calendarActivity = new Intent(GarageActivity.this, CalendarActivity.class);
            Bundle bundleGarage = new Bundle();
            bundleGarage.putSerializable("garageClicked", item);
            calendarActivity.putExtra("garageClicked", bundleGarage);
            startActivityForResult(calendarActivity, CALENDARACTIVITY_REQUEST);
        });
        garageListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.smoothScrollToPosition(garageListAdapter.getItemCount()); // Scroll to bottom
            }
        });
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(this.garageListAdapter);
    }

    // Create options for RecyclerView from a Query
    private FirestoreRecyclerOptions<Garage> generateOptionsForAdapter(Query query){
        return new FirestoreRecyclerOptions.Builder<Garage>()
                .setQuery(query, Garage.class)
                .build();
    }
}
