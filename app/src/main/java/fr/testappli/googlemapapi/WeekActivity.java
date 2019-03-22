package fr.testappli.googlemapapi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

//import com.alamkanak.weekview.MonthLoader;
//import com.alamkanak.weekview.WeekView;
//import com.alamkanak.weekview.WeekViewEvent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class WeekActivity extends AppCompatActivity {

    private WeekView mWeekView = null;
    private ArrayList<WeekViewEvent> mNewEvents;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.week_activity);

        Intent intent = getIntent();
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

            Bundle testt = new Bundle();
            testt.putSerializable("events", nonAvailableDaysList);
            intent.putExtra("events", testt);
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

        // Empty view click listener
        mWeekView.setEmptyViewClickListener(time -> {
            time.set(Calendar.MINUTE, time.get(Calendar.MINUTE) < 30 ? 0 : 30 );
            Calendar endTime = (Calendar) time.clone();
            endTime.add(Calendar.MINUTE, 30);

            // Create a new event.
            long id = mNewEvents.size() == 0 ? 1 : mNewEvents.get(mNewEvents.size() - 1).getId() + 1;
            WeekViewEvent event = new WeekViewEvent(id, "New event", time, endTime);
            mNewEvents.add(event);

            // Refresh the week view. onMonthChange will be called again.
            mWeekView.notifyDatasetChanged();
        });

        // Event click listener
        mWeekView.setOnEventClickListener((event, eventRect) -> {
            if(event.getColor() != getColor(R.color.myRed)){
                for (WeekViewEvent weekViewEvent : mNewEvents) {
                    if (weekViewEvent.getId() == event.getId()) {
                        mNewEvents.remove(weekViewEvent);
                        break;
                    }
                }
                mWeekView.notifyDatasetChanged();
            }
        });

        // Month change listener
        mWeekView.setMonthChangeListener((newYear, newMonth) -> {
            // Populate the week view with the events that was added by tapping on empty view.
            List<WeekViewEvent> events = getEvents(newYear, newMonth);
            ArrayList<WeekViewEvent> newEvents = getNewEvents(newYear, newMonth);
            events.addAll(newEvents);
            return events;
        });

        // new date time interpreter
        DateTimeInterpreter dateTimeInterpreter = new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE M/dd", Locale.getDefault());
                    return sdf.format(date.getTime()).toUpperCase();
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }

            @Override
            public String interpretTime(int hour) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour/2);
                calendar.set(Calendar.MINUTE, hour%2 == 0 ? 0 : 30);

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    return sdf.format(calendar.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }
        };
        //mWeekView.setDateTimeInterpreter(dateTimeInterpreter);

        mNewEvents = new ArrayList<>();
    }

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
        WeekViewEvent event0 = new WeekViewEvent(0, "Tag de la location : ", "Adresse du Garage, Ville", dateToCalendar(startdate), dateToCalendar(enddate));
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
//                Bundle bundle = data.getExtras();
//                events = (ArrayList<EventDay>) bundle.getSerializable("events");
//                intent.putExtra("events", )
                //finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static Calendar dateToCalendar(Date date){
        Calendar cal = Calendar.getInstance();       // get calendar instance
        cal.setTime(date);
        return cal;                                   // return the date part
    }
}
