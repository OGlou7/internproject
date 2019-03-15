package fr.testappli.googlemapapi;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

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

        mWeekView = findViewById(R.id.weekView);

        ImageView imageReturn = findViewById(R.id.back);
        imageReturn.setOnClickListener(v -> finish());

        Button todayButton = findViewById(R.id.today);
        todayButton.setOnClickListener(v -> mWeekView.goToToday());

        // Get a reference for the week view in the layout.
        mWeekView = findViewById(R.id.weekView);

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
}
