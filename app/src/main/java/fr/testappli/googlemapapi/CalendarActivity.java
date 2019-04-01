package fr.testappli.googlemapapi;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.DatePicker;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;
import com.google.firebase.firestore.QueryDocumentSnapshot;


import android.support.v7.widget.Toolbar;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import fr.testappli.googlemapapi.api.NonAvailableTimeHelper;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.models.Garage;
import fr.testappli.googlemapapi.models.NonAvailableTime;
import fr.testappli.googlemapapi.week.WeekActivity;
import fr.testappli.googlemapapi.week.WeekViewEvent;

public class CalendarActivity extends BaseActivity {

    private TimePicker starttimepicker;
    private TimePicker endtimepicker;

    private com.applandeo.materialcalendarview.CalendarView calendarView = null;
    private ArrayList<EventDay> events = new ArrayList<>();
    private Calendar currentCalendar = null;
    private ArrayList<WeekViewEvent> nonAvailableDaysList = new ArrayList<>();
    private ArrayList<Calendar> nonAvailableCalendarList = new ArrayList<>();

    private PopupWindow mPopupWindow;
    private LinearLayout mRelativeLayout;

    private int dayPicked;
    private int monthPicked;
    private int yearPicked;
    private Date startDate;
    private Date endDate;

    private Garage garageClicked;

    public final static int WEEKACTIVITY_REQUEST = 0;
    public final static int TIME_PICKER_INTERVAL = 30;

    private ArrayList<Reservation> reservationArrayList = new ArrayList<>();

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_activity);
        calendarView = findViewById(R.id.calendarView);
        mRelativeLayout = findViewById(R.id.mainLayout);

        Intent intent = getIntent();
        Bundle bundleGarage = Objects.requireNonNull(intent.getExtras()).getBundle("garageClicked");
        garageClicked = (Garage) Objects.requireNonNull(Objects.requireNonNull(bundleGarage).getSerializable("garageClicked"));
        getAllNonAvailableTimeForGarage();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);

        ImageView imageReturn = findViewById(R.id.back);
        imageReturn.setOnClickListener(v -> finish());

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
            case R.id.action_add:
                newReservation();
                return true;
            case R.id.action_settings1:
                Intent dayActivity = new Intent(getApplicationContext(), WeekActivity.class);
                dayActivity.putExtra("nbOfVisibleDays", 1);
                startActivityForResult(dayActivity, WEEKACTIVITY_REQUEST);
                finish();
                return true;
            case R.id.action_settings3:
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
            newWeekViewEvent.setName(nonAvailableTime.getName());
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

    public Date stringToDate(String stringDatum) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

        try {
            return format.parse(stringDatum);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
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


    private void newReservation(){
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.reservation_form,null);

        mPopupWindow = new PopupWindow(
                customView,
                ActionBar.LayoutParams.MATCH_PARENT,
                ActionBar.LayoutParams.MATCH_PARENT
        );

        mPopupWindow.setFocusable(true);
        mPopupWindow.update();

        TextView et_startdate = customView.findViewById(R.id.et_startdate);
        et_startdate.setText("Start Date");
        startDate = currentCalendar.getTime();

        TextView et_enddate = customView.findViewById(R.id.et_enddate);
        et_enddate.setText("End Date  ");
        endDate = currentCalendar.getTime();

        ImageButton ib_startDate = customView.findViewById(R.id.ib_startdate);
        ib_startDate.setOnClickListener(b -> {
            DatePickerBuilder builder = new DatePickerBuilder(this, calendars -> {
                yearPicked = calendars.get(0).get(Calendar.YEAR);
                monthPicked = calendars.get(0).get(Calendar.MONTH) + 1;
                dayPicked = calendars.get(0).get(Calendar.DAY_OF_MONTH);

                et_startdate.setText(String.format("%s-%s-%s", String.valueOf(dayPicked), String.valueOf(monthPicked), String.valueOf(yearPicked)));
                startDate = stringToDate(et_startdate.getText().toString());
            }).pickerType(CalendarView.ONE_DAY_PICKER);

            DatePicker datePicker = builder.build();
            datePicker.show();
        });

        ImageButton ib_endDate = customView.findViewById(R.id.ib_enddate);
        ib_endDate.setOnClickListener(b -> {
            DatePickerBuilder builder = new DatePickerBuilder(this, calendars -> {
                yearPicked = calendars.get(0).get(Calendar.YEAR);
                monthPicked = calendars.get(0).get(Calendar.MONTH) + 1;
                dayPicked = calendars.get(0).get(Calendar.DAY_OF_MONTH);

                et_enddate.setText(String.format("%s-%s-%s", String.valueOf(dayPicked), String.valueOf(monthPicked), String.valueOf(yearPicked)));
                endDate = stringToDate(et_enddate.getText().toString());
            }).pickerType(CalendarView.ONE_DAY_PICKER)
                    .minimumDate(getDatePart(startDate));
            DatePicker datePicker = builder.build();
            datePicker.show();
        });


        EditText et_description = customView.findViewById(R.id.et_description);
        EditText et_address = customView.findViewById(R.id.et_address);
        EditText et_country = customView.findViewById(R.id.et_country);
        EditText et_city = customView.findViewById(R.id.et_city);

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

            if(et_startdate.getText().toString().equals("Start Date")) {
                Toast.makeText(getApplicationContext(), "Invalid start date", Toast.LENGTH_SHORT).show();
                return;
            }

            if(et_enddate.getText().toString().equals("End Date  ")) {
                Toast.makeText(getApplicationContext(), "Invalid end date", Toast.LENGTH_SHORT).show();
                return;
            }

            if(et_city.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "City is empty", Toast.LENGTH_SHORT).show();
                return;
            }

            long newStartDate = startDate.getTime() + (starttimepicker.getHour()*60 + starttimepicker.getMinute()*30)*60*1000;
            long newEndDate = endDate.getTime() + (endtimepicker.getHour()*60 + endtimepicker.getMinute()*30)*60*1000;
            if(newStartDate >= newEndDate){
                Toast.makeText(getApplicationContext(), "Start date after end date", Toast.LENGTH_SHORT).show();
                return;
            }

            startDate.setTime(startDate.getTime() + (starttimepicker.getHour()*60 + starttimepicker.getMinute()*30)*60*1000);
            endDate.setTime(endDate.getTime() + (endtimepicker.getHour()*60 + endtimepicker.getMinute()*30)*60*1000);

            reservationArrayList.add(new Reservation(et_address.getText().toString() + ", " + et_city.getText().toString() + ", " + et_country.getText().toString(),
                    startDate, endDate, et_description.getText().toString(), 2.4));

            ArrayList<Date> daysBetween = getDaysBetween(startDate, endDate);
            for(Date day : daysBetween){
                events.add(new EventDay(getDatePart(day), R.drawable.three_icons));
            }

            calendarView.setEvents(events);
            mPopupWindow.dismiss();
        });

        starttimepicker = customView.findViewById(R.id.starttimepicker);
        setTimePickerInterval(starttimepicker);
        starttimepicker.setIs24HourView(true);

        endtimepicker = customView.findViewById(R.id.endtimepicker);
        setTimePickerInterval(endtimepicker);
        endtimepicker.setIs24HourView(true);

        ImageButton ib_cross = customView.findViewById(R.id.ib_cross);
        ib_cross.setOnClickListener(view -> mPopupWindow.dismiss());

        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mPopupWindow.showAtLocation(mRelativeLayout, Gravity.CENTER,0,0);
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
                            if(!nonAvailableCalendarList.contains(resetCalendar(weekViewEvent.getStartTime()))){
                                nonAvailableCalendarList.add(resetCalendar(weekViewEvent.getStartTime()));
                            }
                            if(!nonAvailableCalendarList.contains(resetCalendar(weekViewEvent.getEndTime()))) {
                                nonAvailableCalendarList.add(resetCalendar(weekViewEvent.getEndTime()));
                            }
                        }
                        for(Calendar calendar : nonAvailableCalendarList)
                            events.add(new EventDay(calendar, R.drawable.reservation));
                        calendarView.setEvents(events);
                    } else {
                        Log.e("ERROR : getAllNonAvailableTimeForGarage", "Error getting documents: ", task.getException());
                    }
                });
    }
}



