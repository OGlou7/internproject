package fr.testappli.googlemapapi.garage;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.widget.CheckBox;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

import java.util.Objects;

import fr.testappli.googlemapapi.CalendarActivity;
import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.api.GarageHelper;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.form.GarageForm;
import fr.testappli.googlemapapi.models.Garage;

public class GarageActivity extends BaseActivity {
    public final static int CALENDARACTIVITY_REQUEST = 1;
    public final static int GARAGEFORM_ADD_REQUEST = 2;
    public final static int GARAGEFORM_UPDATE_REQUEST = 3;

    // FOR DESIGN
    private RecyclerView recyclerView;

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
        recyclerView = findViewById(R.id.listViewGarage);

        // setup the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setOnClickListener(v -> this.addGarage());
        toolbar.setNavigationOnClickListener(v -> finish());

        this.configureRecyclerView(Objects.requireNonNull(getCurrentUser()).getUid());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public int getFragmentLayout() { return R.layout.activity_profile; }

    public void addGarage(){
        Intent garageFormAdd = new Intent(GarageActivity.this, GarageForm.class);
        garageFormAdd.putExtra("Action", "add");
        startActivityForResult(garageFormAdd, GARAGEFORM_ADD_REQUEST);
    }

    public void modifyGarage(Garage garage){
        Intent garageFormUpdate = new Intent(GarageActivity.this, GarageForm.class);
        garageFormUpdate.putExtra("Action", "update");
        Bundle bundleGarage = new Bundle();
        bundleGarage.putSerializable("garageClicked", garage);
        garageFormUpdate.putExtra("garageClicked", bundleGarage);
        startActivityForResult(garageFormUpdate, GARAGEFORM_UPDATE_REQUEST);
    }

    // UI
    private void configureRecyclerView(String userID){
        this.garageListAdapter = new GarageListAdapter(generateOptionsForAdapter(GarageHelper.getAllGarageForUser(userID)),
            // OnItemClickListener
            item -> {
                Intent calendarActivity = new Intent(GarageActivity.this, CalendarActivity.class);
                Bundle bundleGarage = new Bundle();
                bundleGarage.putSerializable("garageClicked", item);
                calendarActivity.putExtra("garageClicked", bundleGarage);
                startActivityForResult(calendarActivity, CALENDARACTIVITY_REQUEST);
            },
            // OnItemLongClickListener
            item -> new AlertDialog.Builder(this)
                        .setMessage(R.string.popup_message_confirmation_delete_garage)
                        .setPositiveButton(R.string.popup_message_choice_yes, (dialogInterface, i) -> GarageHelper.deleteGarage(Objects.requireNonNull(getCurrentUser()).getUid(), item.getUid()))
                        .setNegativeButton(R.string.popup_message_choice_no, null)
                        .show(),
            // OnMoreImageClickListener
            (v, g) -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()) {
                        case R.id.action_modify_garage:
                            modifyGarage(g);
                            return true;
                        case R.id.action_delete_garage:
                            new AlertDialog.Builder(this)
                                    .setMessage(R.string.popup_message_confirmation_delete_garage)
                                    .setPositiveButton(R.string.popup_message_choice_yes, (dialogInterface, i) -> GarageHelper.deleteGarage(Objects.requireNonNull(getCurrentUser()).getUid(), g.getUid()))
                                    .setNegativeButton(R.string.popup_message_choice_no, null)
                                    .show();
                            return true;
                        default:
                            return false;
                    }
                });
                popup.inflate(R.menu.menu_garage_clicked);
                popup.setGravity(Gravity.RIGHT);
                popup.show();
            },
            // onCheckClickListener
            (v, g) -> GarageHelper.updateisAvailable(Objects.requireNonNull(getCurrentUser()).getUid(), g.getUid(), ((CheckBox)v.findViewById(R.id.cb_row_garage_is_reserved)).isChecked())
        );

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
