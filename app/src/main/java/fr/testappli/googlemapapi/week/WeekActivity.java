package fr.testappli.googlemapapi.week;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.DatePicker;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.api.NonAvailableTimeHelper;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.models.Garage;

public class WeekActivity extends BaseActivity {

    private static final int WEEKEVENTFORM_UPDATE_REQUEST = 0;
    private static final int WEEKEVENTFORM_ADD_REQUEST = 1;
    private static final int WEEKEVENTFORM_DELETE_REQUEST = 2;

    private WeekView mWeekView = null;
    private ArrayList<WeekViewEvent> mNewEvents;
    private ArrayList<WeekViewEvent> mOldEvents;
    private Garage garageClicked;
    private ArrayList<WeekViewEvent> eventToSave = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.week_activity);
        mNewEvents = new ArrayList<>();

        Intent intent = getIntent();
        Bundle bundle = Objects.requireNonNull(intent.getExtras()).getBundle("events");
        Bundle bundleGarage = intent.getExtras().getBundle("garageClicked");
        garageClicked = (Garage) Objects.requireNonNull(Objects.requireNonNull(bundleGarage).getSerializable("garageClicked"));

        mOldEvents = (ArrayList<WeekViewEvent>) Objects.requireNonNull(Objects.requireNonNull(bundle).getSerializable("events"));
        int nbOfVisibleDays = intent.getIntExtra("nbOfVisibleDays", 1);
        long dayTimeClicked = intent.getLongExtra("dayClicked", Calendar.getInstance().getTimeInMillis());

        // setup the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // discard changes
        toolbar.setNavigationOnClickListener(v -> {
            ArrayList<WeekViewEvent> eventsToRemove = new ArrayList<>();
            for(WeekViewEvent weekViewEvent : mNewEvents)
                if(weekViewEvent.getColor() != getResources().getColor(R.color.myRed))
                    eventsToRemove.add(weekViewEvent);

            for(WeekViewEvent weekViewEvent : eventsToRemove)
                mNewEvents.remove(weekViewEvent);
            mWeekView.notifyDatasetChanged();

            ArrayList<WeekViewEvent> nonAvailableDaysList = new ArrayList<>(mNewEvents);

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
        mWeekView.setEventTextColor(getResources().getColor(R.color.myGreen));
        mWeekView.setEventTextSize(30);
        mWeekView.goToHour(8);

        // Empty view event click listener
        mWeekView.setEmptyViewClickListener(time -> {
            time.set(Calendar.MINUTE, time.get(Calendar.MINUTE) < 30 ? 0 : 30 );
            addEvent(time);
        });

        // Event click listener
        mWeekView.setOnEventClickListener((event, eventRect) -> {
            if (event.getColor() != getResources().getColor(R.color.myRed)) {
                for (WeekViewEvent weekViewEvent : mNewEvents) {
                    if (Objects.equals(weekViewEvent.getId(), event.getId())) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == WEEKEVENTFORM_DELETE_REQUEST) {
            Bundle BundleWeekViewEventToDelete = Objects.requireNonNull(data.getExtras()).getBundle("weekViewEventToDelete");
            WeekViewEvent test = (WeekViewEvent) Objects.requireNonNull(Objects.requireNonNull(BundleWeekViewEventToDelete).getSerializable("weekViewEventToDelete"));
            deleteNonAvailableTimeInFirestore(test);
        }
        mWeekView.notifyDatasetChanged();*/
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
        WeekViewEvent event0 = new WeekViewEvent(uuid, "Adresse du Garage, Ville", dateToCalendar(startdate), dateToCalendar(enddate));
        event0.setColor(getResources().getColor(R.color.myRed));
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
            case R.id.action_save:
                for(WeekViewEvent weekViewEvent : mNewEvents){
                    weekViewEvent.setColor(getResources().getColor(R.color.myRed));
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

    private void testtest(){
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.date_range_picker,null);

        PopupWindow mPopupWindow = new PopupWindow(
                customView,
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT
        );

        mPopupWindow.setFocusable(true);
        mPopupWindow.update();


        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.showAtLocation(findViewById(R.id.relativeLayout_weekActivity), Gravity.CENTER,0,0);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }


    public static Calendar dateToCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }


    public void addEvent(Calendar timeClicked){
        ArrayList<Calendar> selectedCalendarsOnPicker = new ArrayList<>();
        Calendar endCalendar = (Calendar) timeClicked.clone();
        endCalendar.add(Calendar.MINUTE, 30);

        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.new_weekevent_form,null);

        PopupWindow mPopupWindow = new PopupWindow(
                customView,
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT
        );

        mPopupWindow.setFocusable(true);
        mPopupWindow.update();

        // UI
        NumberPicker np_starttimepicker = customView.findViewById(R.id.np_starttimepicker);
        NumberPicker np_endtimepicker = customView.findViewById(R.id.np_endtimepicker);
        Button b_weekevent_delete = customView.findViewById(R.id.b_weekevent_delete);
        TextView tv_week_startdate = customView.findViewById(R.id.tv_week_startdate);
        TextView tv_week_enddate = customView.findViewById(R.id.tv_week_enddate);

        // Handle Time Picker
        List<String> displayedValues = new ArrayList<>();
        for(int i=0;i<24;i++){
            displayedValues.add(String.format("%02d:00", i));
            displayedValues.add(String.format("%02d:30", i));
        }
        np_starttimepicker.setMinValue(0);
        np_starttimepicker.setMaxValue(displayedValues.size() - 1);
        np_starttimepicker.setDisplayedValues(displayedValues.toArray(new String[0]));
        np_starttimepicker.setValue((timeClicked.get(Calendar.HOUR_OF_DAY) * 2) + (timeClicked.get(Calendar.MINUTE) == 30 ? 1 : 0));

        np_endtimepicker.setMinValue(0);
        np_endtimepicker.setMaxValue(displayedValues.size() - 1);
        np_endtimepicker.setDisplayedValues(displayedValues.toArray(new String[0]));
        np_endtimepicker.setValue((endCalendar.get(Calendar.HOUR_OF_DAY) * 2) + (endCalendar.get(Calendar.MINUTE) == 30 ? 1 : 0));

        // Handle UI

        b_weekevent_delete.setText("Cancel");
        b_weekevent_delete.setOnClickListener(view -> mPopupWindow.dismiss());

        // Handle Time Picker
        DatePickerBuilder builder = new DatePickerBuilder(this, calendars -> {
            selectedCalendarsOnPicker.addAll(calendars);
            tv_week_startdate.setText(String.format("%02d/%02d/%02d", calendars.get(0).get(Calendar.DAY_OF_MONTH),
                    calendars.get(0).get(Calendar.MONTH) + 1,
                    calendars.get(0).get(Calendar.YEAR)));
            tv_week_enddate.setText(String.format("%02d/%02d/%02d", calendars.get(calendars.size() - 1).get(Calendar.DAY_OF_MONTH),
                    calendars.get(calendars.size() - 1).get(Calendar.MONTH) + 1,
                    calendars.get(calendars.size() - 1).get(Calendar.YEAR)));
        }).pickerType(CalendarView.RANGE_PICKER);

        tv_week_startdate.setOnClickListener(v -> {
            DatePicker datePicker = builder.build();
            datePicker.show();
        });

        tv_week_enddate.setOnClickListener(v -> {
            DatePicker datePicker = builder.build();
            datePicker.show();
        });

        // Save new event
        Button b_weekevent_save = customView.findViewById(R.id.b_weekevent_save);
        b_weekevent_save.setOnClickListener(view -> {
            Calendar startTime = timeClicked;
            startTime.set(Calendar.HOUR_OF_DAY, np_starttimepicker.getValue()/2);
            startTime.set(Calendar.MINUTE, np_starttimepicker.getValue()%2==0?0:30);

            Calendar endTime = endCalendar;
            endTime.set(Calendar.HOUR_OF_DAY, np_endtimepicker.getValue()/2);
            endTime.set(Calendar.MINUTE, np_endtimepicker.getValue()%2==0?0:30);

            if(!selectedCalendarsOnPicker.isEmpty()) {
                startTime = selectedCalendarsOnPicker.get(0);
                startTime.set(Calendar.HOUR_OF_DAY, np_starttimepicker.getValue()/2);
                startTime.set(Calendar.MINUTE, np_starttimepicker.getValue()%2==0?0:30);

                endTime = selectedCalendarsOnPicker.get(selectedCalendarsOnPicker.size() - 1);
                endTime.set(Calendar.HOUR_OF_DAY, np_endtimepicker.getValue()/2);
                endTime.set(Calendar.MINUTE, np_endtimepicker.getValue()%2==0?0:30);
            }

            String uuid = UUID.randomUUID().toString();
            WeekViewEvent event = new WeekViewEvent(uuid,  garageClicked.getAddress(), startTime, endTime);
            if(!endTime.after(startTime)) {
                Log.d("WeekActivity", "Start is before end !");
                Toast.makeText(getApplicationContext(), "Start is before end !", Toast.LENGTH_SHORT).show();
                return;
            }

            mNewEvents.add(event);
            mWeekView.notifyDatasetChanged();

            mPopupWindow.dismiss();
        });

        ImageButton ib_cross = customView.findViewById(R.id.ib_cross);
        ib_cross.setOnClickListener(view -> mPopupWindow.dismiss());

        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.showAtLocation(findViewById(R.id.relativeLayout_weekActivity), Gravity.CENTER,0,0);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }


    public void modifyEvent(WeekViewEvent weekViewEvent){
        ArrayList<Calendar> selectedCalendarsOnPicker = new ArrayList<>();
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.new_weekevent_form,null);

        PopupWindow mPopupWindow = new PopupWindow(
                customView,
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT
        );

        mPopupWindow.setFocusable(true);
        mPopupWindow.update();

        // UI
        NumberPicker np_starttimepicker = customView.findViewById(R.id.np_starttimepicker);
        NumberPicker np_endtimepicker = customView.findViewById(R.id.np_endtimepicker);
        Button b_weekevent_save = customView.findViewById(R.id.b_weekevent_save);
        Button b_weekevent_delete = customView.findViewById(R.id.b_weekevent_delete);
        TextView tv_week_startdate = customView.findViewById(R.id.tv_week_startdate);
        TextView tv_week_enddate = customView.findViewById(R.id.tv_week_enddate);
        ImageButton ib_cross = customView.findViewById(R.id.ib_cross);

        // Handle UI
        tv_week_startdate.setText(String.format("%02d/%02d/%02d", weekViewEvent.getStartTime().get(Calendar.DAY_OF_MONTH),
                weekViewEvent.getStartTime().get(Calendar.MONTH) + 1,
                weekViewEvent.getStartTime().get(Calendar.YEAR)));

        tv_week_enddate.setText(String.format("%02d/%02d/%02d", weekViewEvent.getEndTime().get(Calendar.DAY_OF_MONTH),
                weekViewEvent.getEndTime().get(Calendar.MONTH) + 1,
                weekViewEvent.getEndTime().get(Calendar.YEAR)));

        ib_cross.setOnClickListener(view -> mPopupWindow.dismiss());

        // Handle Number Picker
        List<String> displayedValues = new ArrayList<>();
        for(int i=0;i<24;i++){
            displayedValues.add(String.format("%02d:00", i));
            displayedValues.add(String.format("%02d:30", i));
        }
        np_starttimepicker.setMinValue(0);
        np_starttimepicker.setMaxValue(displayedValues.size() - 1);
        np_starttimepicker.setDisplayedValues(displayedValues.toArray(new String[0]));
        np_starttimepicker.setValue((weekViewEvent.getStartTime().get(Calendar.HOUR_OF_DAY) * 2) + (weekViewEvent.getStartTime().get(Calendar.MINUTE) == 30 ? 1 : 0));

        np_endtimepicker.setMinValue(0);
        np_endtimepicker.setMaxValue(displayedValues.size() - 1);
        np_endtimepicker.setDisplayedValues(displayedValues.toArray(new String[0]));
        np_endtimepicker.setValue((weekViewEvent.getEndTime().get(Calendar.HOUR_OF_DAY) * 2) + (weekViewEvent.getEndTime().get(Calendar.MINUTE) == 30 ? 1 : 0));

        // Handle Time Picker
        DatePickerBuilder builder = new DatePickerBuilder(this, calendars -> {
            selectedCalendarsOnPicker.addAll(calendars);
            tv_week_startdate.setText(String.format("%02d/%02d/%02d", calendars.get(0).get(Calendar.DAY_OF_MONTH),
                    calendars.get(0).get(Calendar.MONTH) + 1,
                    calendars.get(0).get(Calendar.YEAR)));
            tv_week_enddate.setText(String.format("%02d/%02d/%02d", calendars.get(calendars.size() - 1).get(Calendar.DAY_OF_MONTH),
                    calendars.get(calendars.size() - 1).get(Calendar.MONTH) + 1,
                    calendars.get(calendars.size() - 1).get(Calendar.YEAR)));
        }).pickerType(CalendarView.RANGE_PICKER);

        tv_week_startdate.setOnClickListener(v -> {
            DatePicker datePicker = builder.build();
            datePicker.show();
        });

        tv_week_enddate.setOnClickListener(v -> {
            DatePicker datePicker = builder.build();
            datePicker.show();
        });

        // Handle Save & Delete Button
        b_weekevent_delete.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                .setMessage(R.string.popup_message_confirmation_delete_garage)
                .setPositiveButton(R.string.popup_message_choice_yes, (dialogInterface, i) -> deleteNonAvailableTimeInFirestore(weekViewEvent))
                .setNegativeButton(R.string.popup_message_choice_no, null)
                .show();

            mPopupWindow.dismiss();
        });

        b_weekevent_save.setOnClickListener(view -> {
            Calendar startTime = weekViewEvent.getStartTime();
            startTime.set(Calendar.HOUR_OF_DAY, np_starttimepicker.getValue()/2);
            startTime.set(Calendar.MINUTE, np_starttimepicker.getValue()%2==0?0:30);

            Calendar endTime = weekViewEvent.getEndTime();
            endTime.set(Calendar.HOUR_OF_DAY, np_endtimepicker.getValue()/2);
            endTime.set(Calendar.MINUTE, np_endtimepicker.getValue()%2==0?0:30);

            if(!selectedCalendarsOnPicker.isEmpty()) {
                startTime = selectedCalendarsOnPicker.get(0);
                startTime.set(Calendar.HOUR_OF_DAY, np_starttimepicker.getValue()/2);
                startTime.set(Calendar.MINUTE, np_starttimepicker.getValue()%2==0?0:30);

                endTime = selectedCalendarsOnPicker.get(selectedCalendarsOnPicker.size() - 1);
                endTime.set(Calendar.HOUR_OF_DAY, np_endtimepicker.getValue()/2);
                endTime.set(Calendar.MINUTE, np_endtimepicker.getValue()%2==0?0:30);
            }

            if(endTime.after(startTime)) {
                weekViewEvent.setStartTime(startTime);
                weekViewEvent.setEndTime(endTime);
            }
            else {
                Log.d("WeekActivity", "Start is before end !");
                Toast.makeText(getApplicationContext(), "Start is before end !", Toast.LENGTH_SHORT).show();
            }

            updateNonAvailableTimeInFirestore(weekViewEvent);
            mWeekView.notifyDatasetChanged();

            mPopupWindow.dismiss();
        });

        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.showAtLocation(findViewById(R.id.relativeLayout_weekActivity), Gravity.CENTER,0,0);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    // REST REQUEST
    private void createNonAvailableTimeInFirestore(WeekViewEvent weekViewEvent){
        NonAvailableTimeHelper.createNonAvailableTimeForGarage(Objects.requireNonNull(getCurrentUser()).getUid(), garageClicked.getUid(), weekViewEvent.getId(), weekViewEvent).addOnFailureListener(this.onFailureListener());
    }

    private void updateNonAvailableTimeInFirestore(WeekViewEvent weekViewEvent){
        NonAvailableTimeHelper.updateNonAvailableTime(Objects.requireNonNull(getCurrentUser()).getUid(), garageClicked.getUid(), weekViewEvent.getId(), weekViewEvent).addOnFailureListener(this.onFailureListener());
    }

    private void deleteNonAvailableTimeInFirestore(WeekViewEvent weekViewEvent){
        NonAvailableTimeHelper.deleteDateNonDispo(Objects.requireNonNull(getCurrentUser()).getUid(), garageClicked.getUid(), weekViewEvent.getId());
        if (mNewEvents.contains(weekViewEvent)) {
            mNewEvents.remove(weekViewEvent);
        } else if(mOldEvents.contains(weekViewEvent)){
            mOldEvents.remove(weekViewEvent);
        } else {
            return;
        }
        mWeekView.notifyDatasetChanged();
    }
}
