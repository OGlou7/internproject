package fr.testappli.googlemapapi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;

import java.util.ArrayList;
import java.util.List;

public class WeekActivity extends AppCompatActivity {

    private WeekView mWeekView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.week_activity);

        Intent intent = getIntent();
        int nbOfVisibleDays = intent.getIntExtra("nbOfVisibleDays", 7);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ImageView imageReturn = findViewById(R.id.back);
        imageReturn.setOnClickListener(v -> finish());

        // Get a reference for the week view in the layout.
        mWeekView = findViewById(R.id.weekView);
        mWeekView.setNumberOfVisibleDays(nbOfVisibleDays);

// Set an action when any event is clicked.
        //mWeekView.setOnEventClickListener(mEventClickListener);

// The week view has infinite scrolling horizontally. We have to provide the events of a
// month every time the month changes on the week view.
         mWeekView.setMonthChangeListener(mMonthChangeListener);

// Set long press listener for events.
        //mWeekView.setEventLongPressListener(mEventLongPressListener);
    }

    MonthLoader.MonthChangeListener mMonthChangeListener = (newYear, newMonth) -> {
        // Populate the week view with some events.
        List<WeekViewEvent> events = getEvents(newYear, newMonth);
        return events;
    };

    private List<WeekViewEvent> getEvents(int newYear, int newMonth) {
        ArrayList<WeekViewEvent> test = new ArrayList<>();
        return test;
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
            case R.id.action_settings2:
                mWeekView.setNumberOfVisibleDays(3);
                return true;
            case R.id.action_settings3:
                Intent calendar = new Intent(getApplicationContext(), CalendarActivity.class);
                startActivityForResult(calendar, 0);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
