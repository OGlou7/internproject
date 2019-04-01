package fr.testappli.googlemapapi.week;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

//import com.alamkanak.weekview.MonthLoader;
//import com.alamkanak.weekview.WeekView;
//import com.alamkanak.weekview.WeekViewEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import fr.testappli.googlemapapi.DateTimeInterpreter;
import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.api.GarageHelper;
import fr.testappli.googlemapapi.api.NonAvailableTimeHelper;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.models.Garage;
import fr.testappli.googlemapapi.models.NonAvailableTime;
import fr.testappli.googlemapapi.week.WeekView;
import fr.testappli.googlemapapi.week.WeekViewEvent;

public class WeekActivity extends BaseActivity {

    private WeekView mWeekView = null;
    private ArrayList<WeekViewEvent> mNewEvents;
    private ArrayList<WeekViewEvent> mOldEvents;
    private Garage garageClicked;
    private ArrayList<WeekViewEvent> eventToSave = new ArrayList<>();
    public final static int TIME_PICKER_INTERVAL = 30;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.week_activity);
        mNewEvents = new ArrayList<>();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras().getBundle("events");
        Bundle bundleGarage = intent.getExtras().getBundle("garageClicked");
        garageClicked = (Garage) Objects.requireNonNull(bundleGarage.getSerializable("garageClicked"));
        mOldEvents = (ArrayList<WeekViewEvent>) Objects.requireNonNull(bundle.getSerializable("events"));
        int nbOfVisibleDays = intent.getIntExtra("nbOfVisibleDays", 1);
        long dayTimeClicked = intent.getLongExtra("dayClicked", Calendar.getInstance().getTimeInMillis());

        // setup the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // discard changes
        toolbar.setNavigationOnClickListener(v -> {
            ArrayList<WeekViewEvent> eventsToRemove = new ArrayList<>();
            for(WeekViewEvent weekViewEvent : mNewEvents)
                if(weekViewEvent.getColor() != getColor(R.color.myRed))
                    eventsToRemove.add(weekViewEvent);

            for(WeekViewEvent weekViewEvent : eventsToRemove)
                mNewEvents.remove(weekViewEvent);
            mWeekView.notifyDatasetChanged();

            ArrayList<WeekViewEvent> nonAvailableDaysList = new ArrayList<>();
            nonAvailableDaysList.addAll(mNewEvents);

            Intent data = new Intent();
            Bundle nonAvailableDaysListBundle = new Bundle();
            nonAvailableDaysListBundle.putSerializable("events", nonAvailableDaysList);
            data.putExtra("events", nonAvailableDaysListBundle);
            setResult(0,data);
            finish();
        });

        // Get a reference for the week view in the layout.
        mWeekView = findViewById(R.id.weekView);

        // setup the Week view
        mWeekView.setNumberOfVisibleDays(nbOfVisibleDays);
        Calendar dayClicked = Calendar.getInstance();
        dayClicked.setTimeInMillis(dayTimeClicked);
        mWeekView.goToDate(dayClicked);
        mWeekView.setEventTextColor(getColor(R.color.myGreen));
        mWeekView.setEventTextSize(30);

        // Empty view event click listener
        mWeekView.setEmptyViewClickListener(time -> {
            time.set(Calendar.MINUTE, time.get(Calendar.MINUTE) < 30 ? 0 : 30 );
            Calendar endTime = (Calendar) time.clone();
            endTime.add(Calendar.MINUTE, 30);

            // Create a new event.
            String uuid = UUID.randomUUID().toString();
            WeekViewEvent event = new WeekViewEvent(uuid, "New event", garageClicked.getAddress(), time, endTime);
            mNewEvents.add(event);

            mWeekView.notifyDatasetChanged();
        });

        // Event click listener
        mWeekView.setOnEventClickListener((event, eventRect) -> {
            if (event.getColor() != getColor(R.color.myRed)) {
                for (WeekViewEvent weekViewEvent : mNewEvents) {
                    if (weekViewEvent.getId() == event.getId()) {
                        mNewEvents.remove(weekViewEvent);
                        break;
                    }
                }
                mWeekView.notifyDatasetChanged();
            } else {
                modifyEvent(event);
            }
        });

        // Month change listener
        mWeekView.setMonthChangeListener((newYear, newMonth) -> {
            // Populate the week view with the events that was added by tapping on empty view.
            List<WeekViewEvent> events = getEvents(newYear, newMonth);
            ArrayList<WeekViewEvent> newEvents = getNewEvents(newYear, newMonth);
            ArrayList<WeekViewEvent> oldEvents = getOldEvents(newYear, newMonth);
            events.addAll(newEvents);
            events.addAll(oldEvents);
            return events;
        });
    }

    @Override
    public int getFragmentLayout() { return R.layout.activity_profile; }

    private List<WeekViewEvent> getEvents(int newYear, int newMonth) {
        ArrayList<WeekViewEvent> weekViewEventList = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        Date startdate = null;
        Date enddate = null;
        try {
            startdate = format.parse("2019-"+ String.valueOf(newMonth)+"-28T12:30Z");
            enddate = format.parse("2019-"+ String.valueOf(newMonth)+"-29T12:30Z");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String uuid = UUID.randomUUID().toString();
        WeekViewEvent event0 = new WeekViewEvent(uuid, "Tag de la location : ", "Adresse du Garage, Ville", dateToCalendar(startdate), dateToCalendar(enddate));
        event0.setColor(getColor(R.color.myRed));
        weekViewEventList.add(event0);
        return weekViewEventList;
    }

    private ArrayList<WeekViewEvent> getNewEvents(int year, int month) {

        // Get the starting point and ending point of the given month. We need this to find the
        // events of the given month.
        Calendar startOfMonth = Calendar.getInstance();
        startOfMonth.set(Calendar.YEAR, year);
        startOfMonth.set(Calendar.MONTH, month - 1);
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        startOfMonth.set(Calendar.MINUTE, 0);
        startOfMonth.set(Calendar.SECOND, 0);
        startOfMonth.set(Calendar.MILLISECOND, 0);
        Calendar endOfMonth = (Calendar) startOfMonth.clone();
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getMaximum(Calendar.DAY_OF_MONTH));
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23);
        endOfMonth.set(Calendar.MINUTE, 59);
        endOfMonth.set(Calendar.SECOND, 59);

        // Find the events that were added by tapping on empty view and that occurs in the given
        // time frame.
        ArrayList<WeekViewEvent> events = new ArrayList<>();
        for (WeekViewEvent event : mNewEvents) {
            if (event.getEndTime().getTimeInMillis() > startOfMonth.getTimeInMillis() &&
                    event.getStartTime().getTimeInMillis() < endOfMonth.getTimeInMillis()) {
                events.add(event);
            }
        }
        return events;
    }

    private ArrayList<WeekViewEvent> getOldEvents(int year, int month) {

        // Get the starting point and ending point of the given month. We need this to find the
        // events of the given month.
        Calendar startOfMonth = Calendar.getInstance();
        startOfMonth.set(Calendar.YEAR, year);
        startOfMonth.set(Calendar.MONTH, month - 1);
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1);
        startOfMonth.set(Calendar.HOUR_OF_DAY, 0);
        startOfMonth.set(Calendar.MINUTE, 0);
        startOfMonth.set(Calendar.SECOND, 0);
        startOfMonth.set(Calendar.MILLISECOND, 0);
        Calendar endOfMonth = (Calendar) startOfMonth.clone();
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getMaximum(Calendar.DAY_OF_MONTH));
        endOfMonth.set(Calendar.HOUR_OF_DAY, 23);
        endOfMonth.set(Calendar.MINUTE, 59);
        endOfMonth.set(Calendar.SECOND, 59);

        // Find the events that were added by tapping on empty view and that occurs in the given
        // time frame.
        ArrayList<WeekViewEvent> events = new ArrayList<>();
        for (WeekViewEvent event : mOldEvents) {
            if (event.getEndTime().getTimeInMillis() > startOfMonth.getTimeInMillis() &&
                    event.getStartTime().getTimeInMillis() < endOfMonth.getTimeInMillis()) {
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_weekview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_today:
                mWeekView.goToToday();
                return true;
            case R.id.action_add:
                return true;
            case R.id.action_save:
                for(WeekViewEvent weekViewEvent : mNewEvents){
                    weekViewEvent.setColor(getColor(R.color.myRed));
                }
                mWeekView.notifyDatasetChanged();

                for(WeekViewEvent weekViewEvent : mNewEvents) {
                    if(!eventToSave.contains(weekViewEvent)){
                        eventToSave.add(weekViewEvent);
                        createNonAvailableTimeInFirestore(weekViewEvent);
                    }
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public static Calendar dateToCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public void modifyEvent(WeekViewEvent weekViewEvent){
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.new_weekevent_form,null);

        PopupWindow mPopupWindow = new PopupWindow(
                customView,
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT
        );

        mPopupWindow.setFocusable(true);
        mPopupWindow.update();

//        TimePicker starttimepicker = customView.findViewById(R.id.starttimepicker);
//        setTimePickerInterval(starttimepicker);
//        starttimepicker.setIs24HourView(true);
//        starttimepicker.setHour(weekViewEvent.getStartTime().get(Calendar.HOUR_OF_DAY));
//        starttimepicker.setMinute(weekViewEvent.getStartTime().get(Calendar.MINUTE));
        NumberPicker starttimepicker = customView.findViewById(R.id.starttimepicker);

        List<String> displayedValues = new ArrayList<>();
        for(int i=0;i<24;i++){
            displayedValues.add(String.format("%02d:00", i));
            displayedValues.add(String.format("%02d:30", i));
        }
        starttimepicker.setMinValue(0);
        starttimepicker.setMaxValue(displayedValues.size() - 1);
        starttimepicker.setDisplayedValues(displayedValues.toArray(new String[0]));

        TextView et_startdate = customView.findViewById(R.id.et_startdate);
        et_startdate.setText(String.valueOf(weekViewEvent.getStartTime().get(Calendar.DAY_OF_MONTH))+"/"+
                String.valueOf(weekViewEvent.getStartTime().get(Calendar.MONTH) + 1)+"/"+
                String.valueOf(weekViewEvent.getStartTime().get(Calendar.YEAR)));

        EditText et_weekevent_name = customView.findViewById(R.id.et_weekevent_name);
        et_weekevent_name.setText(weekViewEvent.getName());

        Button b_register = customView.findViewById(R.id.b_register);
        b_register.setOnClickListener(view -> {
            if(et_weekevent_name.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "Address is empty", Toast.LENGTH_SHORT).show();
                return;
            }
            mPopupWindow.dismiss();
        });

        Button b_delete = customView.findViewById(R.id.b_delete);
        b_delete.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                .setMessage(R.string.popup_message_confirmation_delete_garage)
                .setPositiveButton(R.string.popup_message_choice_yes, (dialogInterface, i) -> deleteNonAvailableTimeInFirestore(weekViewEvent))
                .setNegativeButton(R.string.popup_message_choice_no, null)
                .show();

            mPopupWindow.dismiss();
        });


        ImageButton ib_cross = customView.findViewById(R.id.ib_cross);
        ib_cross.setOnClickListener(view -> mPopupWindow.dismiss());

        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.showAtLocation(findViewById(R.id.relativeLayout_weekActivity), Gravity.CENTER,0,0);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

    }


    private void setTimePickerInterval(TimePicker timePicker) {
        try {
            NumberPicker minutePicker = timePicker.findViewById(Resources.getSystem().getIdentifier(
                    "minute", "id", "android"));
            minutePicker.setMinValue(0);
            minutePicker.setMaxValue((60 / TIME_PICKER_INTERVAL) - 1);
            List<String> displayedValues = new ArrayList<>();
            for (int i = 0; i < 60; i += TIME_PICKER_INTERVAL) {
                displayedValues.add(String.format("%02d", i));
            }
            minutePicker.setDisplayedValues(displayedValues.toArray(new String[0]));
        } catch (Exception e) {
            Log.e("Error", "Exception: " + e);
        }
    }

    // REST REQUEST
    private void createNonAvailableTimeInFirestore(WeekViewEvent weekViewEvent){
        NonAvailableTimeHelper.createNonAvailableTimeForGarage(getCurrentUser().getUid(), garageClicked.getUid(), weekViewEvent.getId(), weekViewEvent).addOnFailureListener(this.onFailureListener());
    }

    private void deleteNonAvailableTimeInFirestore(WeekViewEvent weekViewEvent){
        NonAvailableTimeHelper.deleteDateNonDispo(getCurrentUser().getUid(), garageClicked.getUid(), weekViewEvent.getId());
        if (mNewEvents.contains(weekViewEvent)) {
            mNewEvents.remove(weekViewEvent);
        } else if(mOldEvents.contains(weekViewEvent)){
            mOldEvents.remove(weekViewEvent);
        }
        else
            return;
        mWeekView.notifyDatasetChanged();
    }
}
