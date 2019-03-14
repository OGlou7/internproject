package fr.testappli.googlemapapi;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.applandeo.materialcalendarview.DatePicker;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {
    private ImageButton ib_startDate = null;
    private ImageButton ib_endDate = null;

    private com.applandeo.materialcalendarview.CalendarView calendarView = null;
    private List<EventDay> events = new ArrayList<>();
    private Calendar currentCalendar = null;
    private Date currentTime;


    private PopupWindow mPopupWindow;
    private LinearLayout mRelativeLayout;
    private EditText et_startdate = null;
    private EditText et_enddate = null;

    private int dayPicked;
    private int monthPicked;
    private int yearPicked;

    @SuppressLint("SimpleDateFormat")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calendar_activity);
        calendarView = findViewById(R.id.calendarView);

        currentCalendar = Calendar.getInstance();
        currentTime = currentCalendar.getTime();

        Button todayButton = findViewById(R.id.today);
        todayButton.setOnClickListener(v -> {
            try {
                calendarView.setDate(currentTime);
            } catch (OutOfDateRangeException e) {
                e.printStackTrace();
            }
        });

        ImageView imageReturn = findViewById(R.id.back);
        imageReturn.setOnClickListener(v -> {
            /*Intent result = new Intent();
            result.putExtra(MainActivity2.BUTTONS, "3");
            setResult(RESULT_OK, result);*/
            finish();
        });



        mRelativeLayout = findViewById(R.id.mainLayout);
        Button newReservationButton = findViewById(R.id.newReservation);
        newReservationButton.setOnClickListener(v -> {
            LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

            @SuppressLint("InflateParams") View customView = inflater.inflate(R.layout.reservation_form,null);

            mPopupWindow = new PopupWindow(
                    customView,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT
            );

            Button closeButton = customView.findViewById(R.id.register);
            closeButton.setOnClickListener(view -> mPopupWindow.dismiss());

            ImageButton ib_cross = customView.findViewById(R.id.ib_cross);
            ib_cross.setOnClickListener(view -> mPopupWindow.dismiss());

            et_startdate = customView.findViewById(R.id.et_startdate);
            et_startdate.setText(new SimpleDateFormat("dd/MM/yyyy").format(currentCalendar.getTime()));

            currentCalendar.add(Calendar.DAY_OF_YEAR, 1);
            et_enddate = customView.findViewById(R.id.et_enddate);
            et_enddate.setText(new SimpleDateFormat("dd/MM/yyyy").format(currentCalendar.getTime()));
            currentCalendar.setTime(currentTime);


            ib_startDate = customView.findViewById(R.id.ib_startdate);
            ib_startDate.setOnClickListener(b -> {
                DatePickerBuilder builder = new DatePickerBuilder(this, calendars -> {
                    yearPicked = calendars.get(0).get(Calendar.YEAR);
                    monthPicked = calendars.get(0).get(Calendar.MONTH) + 1;
                    dayPicked = calendars.get(0).get(Calendar.DAY_OF_MONTH);

                    et_startdate.setText(String.format("%s/%s/%s", String.valueOf(dayPicked), String.valueOf(monthPicked), String.valueOf(yearPicked)));
                }).pickerType(com.applandeo.materialcalendarview.CalendarView.ONE_DAY_PICKER);

                DatePicker datePicker = builder.build();
                datePicker.show();
                Log.d("DebugPop", "datePicker show");
            });

            ib_endDate = customView.findViewById(R.id.ib_enddate);
            ib_endDate.setOnClickListener(b -> {
                DatePickerBuilder builder = new DatePickerBuilder(this, calendars -> {
                    yearPicked = calendars.get(0).get(Calendar.YEAR);
                    monthPicked = calendars.get(0).get(Calendar.MONTH) + 1;
                    dayPicked = calendars.get(0).get(Calendar.DAY_OF_MONTH);

                    et_enddate.setText(String.format("%s/%s/%s", String.valueOf(dayPicked), String.valueOf(monthPicked), String.valueOf(yearPicked)));
                }).pickerType(com.applandeo.materialcalendarview.CalendarView.ONE_DAY_PICKER);
                DatePicker datePicker = builder.build();
                datePicker.show();
            });

            mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            mPopupWindow.showAtLocation(mRelativeLayout, Gravity.CENTER,0,0);

            /*DatePickerBuilder builder = new DatePickerBuilder(this, listener).pickerType(com.applandeo.materialcalendarview.CalendarView.ONE_DAY_PICKER);
            DatePicker datePicker = builder.build();
            datePicker.show();*/
        });


        events.add(new EventDay(currentCalendar, R.drawable.ic_action_refresh));
        List<Calendar> selectedDates = calendarView.getSelectedDates();
        Calendar selectedDate = calendarView.getFirstSelectedDate();


        calendarView.setEvents(events);
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDayCalendar = eventDay.getCalendar();
            events.add(new EventDay(clickedDayCalendar, R.drawable.ic_action_refresh));
            calendarView.setEvents(events);
        });
    }
}