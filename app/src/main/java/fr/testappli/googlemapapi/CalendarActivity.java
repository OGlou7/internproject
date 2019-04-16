package fr.testappli.googlemapapi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import fr.testappli.googlemapapi.api.NonAvailableTimeHelper;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.models.Garage;
import fr.testappli.googlemapapi.models.NonAvailableTime;
import fr.testappli.googlemapapi.week.WeekActivity;
import fr.testappli.googlemapapi.week.WeekViewEvent;

public class CalendarActivity extends BaseActivity {

    private com.applandeo.materialcalendarview.CalendarView calendarView = null;
    private ArrayList<EventDay> events = new ArrayList<>();
    private Calendar currentCalendar = null;
    private ArrayList<WeekViewEvent> nonAvailableDaysList = new ArrayList<>();
    private ArrayList<Calendar> nonAvailableCalendarList = new ArrayList<>();

    private Garage garageClicked;

    public final static int WEEKACTIVITY_REQUEST = 0;
    public final static int TIME_PICKER_INTERVAL = 30;

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_activity);
        calendarView = findViewById(R.id.calendarView);

        Intent intent = getIntent();
        Bundle bundleGarage = Objects.requireNonNull(intent.getExtras()).getBundle("garageClicked");
        garageClicked = (Garage) Objects.requireNonNull(Objects.requireNonNull(bundleGarage).getSerializable("garageClicked"));
        getAllNonAvailableTimeForGarage();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        toolbar.setNavigationOnClickListener(v -> finish());

        currentCalendar = Calendar.getInstance();
        calendarView.setHeaderColor(R.color.calendar_header_color);
        calendarView.setHeaderLabelColor(R.color.colorBlack);
        calendarView.setOnDayClickListener(eventDay -> {
            Intent dayActivity = new Intent(getApplicationContext(), WeekActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("events", nonAvailableDaysList);
            Bundle bundleGarage2 = new Bundle();
            bundleGarage2.putSerializable("garageClicked", garageClicked);
            dayActivity.putExtra("garageClicked", bundleGarage2);
            dayActivity.putExtra("events", bundle);
            dayActivity.putExtra("nbOfVisibleDays", 1);
            dayActivity.putExtra("dayClicked", eventDay.getCalendar().getTimeInMillis());
            startActivityForResult(dayActivity, WEEKACTIVITY_REQUEST);
        });
    }


    @Override
    public int getFragmentLayout() { return R.layout.activity_profile; }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        events.clear();
        nonAvailableCalendarList.clear();
        nonAvailableDaysList.clear();
        getAllNonAvailableTimeForGarage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calendar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_today:
                Toast.makeText(getApplicationContext(), "Option", Toast.LENGTH_SHORT).show();
                try {
                    calendarView.setDate(currentCalendar.getTime());
                } catch (OutOfDateRangeException e) {
                    e.printStackTrace();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<WeekViewEvent> listNonAvailableToListWeekViewEvent(ArrayList<NonAvailableTime> nonAvailableTimeArrayList){
        ArrayList<WeekViewEvent> weekViewEventArrayList = new ArrayList<>();
        for(NonAvailableTime nonAvailableTime : nonAvailableTimeArrayList){
            WeekViewEvent newWeekViewEvent = new WeekViewEvent();
            newWeekViewEvent.setColor(nonAvailableTime.getColor());
            newWeekViewEvent.setStartTime(getDatePart(nonAvailableTime.getStartTime()));
            newWeekViewEvent.setEndTime(getDatePart(nonAvailableTime.getEndTime()));
            newWeekViewEvent.setLocation(nonAvailableTime.getLocation());
            newWeekViewEvent.setId(nonAvailableTime.getId());
            weekViewEventArrayList.add(newWeekViewEvent);
        }
        return weekViewEventArrayList;
    }

    public ArrayList<Date> getDaysBetween(Date start, Date end){
        ArrayList<Date> daysBetween = new ArrayList<>();
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);
        daysBetween.add(start);

        while (startCalendar.before(getDatePart(end))){
            startCalendar.add(Calendar.DAY_OF_YEAR,1);
            Date newDate = startCalendar.getTime();
            daysBetween.add(newDate);
        }
        return daysBetween;
    }

    public static Calendar getDatePart(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static Calendar resetCalendar(Calendar calendar){
        Calendar cal = Calendar.getInstance();
        cal.setTime(calendar.getTime());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal;
    }

    // REST REQUEST

    public void getAllNonAvailableTimeForGarage(){
        ArrayList<NonAvailableTime> nonAvailableTimesList = new ArrayList<>();
        NonAvailableTimeHelper.getNonAvailableTimeCollection(Objects.requireNonNull(getCurrentUser()).getUid(), garageClicked.getUid()).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                            nonAvailableTimesList.add(document.toObject(NonAvailableTime.class));
                        }
                        ArrayList<WeekViewEvent> newWeekNewEvent = listNonAvailableToListWeekViewEvent(nonAvailableTimesList);
                        nonAvailableDaysList.addAll(newWeekNewEvent);
                        for(WeekViewEvent weekViewEvent : newWeekNewEvent) {
                            Calendar startCal = resetCalendar((Calendar)weekViewEvent.getStartTime().clone());
                            Calendar endCal = resetCalendar((Calendar)weekViewEvent.getEndTime().clone());
                            endCal.add(Calendar.DAY_OF_YEAR,1);

                            while(startCal.before(endCal)){
                                Calendar catToAdd = (Calendar)startCal.clone();
                                if(!nonAvailableCalendarList.contains(catToAdd)){
                                    nonAvailableCalendarList.add(catToAdd);
                                }
                                startCal.add(Calendar.DAY_OF_YEAR,1);
                            }
                        }
                        for(Calendar calendar : nonAvailableCalendarList) {
                            events.add(new EventDay(calendar, R.drawable.reservation));
                        }
                        calendarView.setEvents(events);
                    } else {
                        Log.e("ERROR : getAllNonAvailableTimeForGarage", "Error getting documents: ", task.getException());
                    }
                });
    }
}