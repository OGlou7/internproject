package fr.testappli.googlemapapi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
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

public class WeekActivity extends AppCompatActivity {

    private WeekView mWeekView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.week_activity);

        Intent intent = getIntent();
        int nbOfVisibleDays = intent.getIntExtra("nbOfVisibleDays", 1);
        long dayTimeClicked = intent.getLongExtra("dayClicked", Calendar.getInstance().getTimeInMillis());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ImageView imageReturn = findViewById(R.id.back);
        imageReturn.setOnClickListener(v -> finish());

        // Get a reference for the week view in the layout.
        mWeekView = findViewById(R.id.weekView);
        mWeekView.setNumberOfVisibleDays(nbOfVisibleDays);
        Calendar dayClicked = Calendar.getInstance();
        dayClicked.setTimeInMillis(dayTimeClicked);
        mWeekView.goToDate(dayClicked);
        mWeekView.setEventTextColor(getColor(R.color.myGreen));
        mWeekView.setEventTextSize(60);
        mWeekView.setEmptyViewClickListener(time -> Log.e("setEmptyViewClickListener", "setEmptyViewClickListener == " + time.toString()));
        mWeekView.setOnEventClickListener((event, eventRect) -> Log.e("setOnEventClickListener", event.getLocation() + "setEmptyViewClickListener == " + event.getId()));

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
                if(hour%2 != 0)
                    calendar.set(Calendar.MINUTE, 30);
                else
                    calendar.set(Calendar.MINUTE, 0);

                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    return sdf.format(calendar.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }
        };
        mWeekView.setDateTimeInterpreter(dateTimeInterpreter);


        mWeekView.setMonthChangeListener((newYear, newMonth) -> {
            // Populate the week view with some events.
            List<WeekViewEvent> events = getEvents(newYear, newMonth);
            return events;
        });

// Set an action when any event is clicked.
        //mWeekView.setOnEventClickListener(mEventClickListener);

// Set long press listener for events.
        //mWeekView.setEventLongPressListener(mEventLongPressListener);
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
        WeekViewEvent event0 = new WeekViewEvent(0, "Tag de la location : ", "Adresse du Garage, Ville", getDatePart(startdate), getDatePart(enddate));
        event0.setColor(getColor(R.color.myRed));
        weekViewEventList.add(event0);
        return weekViewEventList;
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
                mWeekView.goToToday();
                return true;
            case R.id.action_add:
                return true;
            case R.id.action_settings1:
                mWeekView.setNumberOfVisibleDays(1);
                return true;
            case R.id.action_settings3:
                Intent calendar = new Intent(getApplicationContext(), CalendarActivity.class);
                //TODO renvoyer les nouveaux event pour colorier le calendrier
                calendar.putExtra("nbOfVisibleDays", 1);
                startActivityForResult(calendar, 0);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static Calendar getDatePart(Date date){
        Calendar cal = Calendar.getInstance();       // get calendar instance
        cal.setTime(date);
        return cal;                                   // return the date part
    }
}
