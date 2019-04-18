package fr.testappli.googlemapapi.form;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.DatePicker;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.internal.HttpClient;
import com.braintreepayments.api.models.PaymentMethodNonce;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.api.NonAvailableTimeHelper;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.models.Garage;
import fr.testappli.googlemapapi.models.NonAvailableTime;

public class ReservationForm  extends BaseActivity {
    private static final String API_GET_TOKEN = "PATH_TO_SERVER";
    private static final String API_CHECK_OUT = "PATH_TO_SERVER";
    private static final int PAYMENT_REQUEST = 1234;

    ReservationForm context;
    String token;
    HashMap<String, String> paramsHash;

    private Garage garageClicked;
    private TextView tv_price;

    @Override
    public int getFragmentLayout() { return R.layout.activity_profile; }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PAYMENT_REQUEST){
            if (RESULT_OK == resultCode){
                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                PaymentMethodNonce paymentNonce = result.getPaymentMethodNonce();
                String strNonce = Objects.requireNonNull(paymentNonce).getNonce();

                paramsHash = new HashMap<>();
                paramsHash.put("amount", tv_price.getText().toString());
                paramsHash.put("nonce", strNonce);

                sendPayments();
            }
        } else if(requestCode == RESULT_CANCELED) {
            Toast.makeText(this, "User Cancel", Toast.LENGTH_SHORT).show();
        } else {
            Exception error = (Exception)data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
            Log.e("ERROR123456", error.toString());
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_reservation_form);

        Intent intent = getIntent();
        Bundle bundleGarage = Objects.requireNonNull(intent.getExtras()).getBundle("garageClicked");
        garageClicked = (Garage) Objects.requireNonNull(Objects.requireNonNull(bundleGarage).getSerializable("garageClicked"));
        context = this;

        configureUI();
        configureBrainTree();
    }


    private void configureBrainTree(){
        tv_price.setOnClickListener(v -> submitPayment());

    }

    private void submitPayment() {
        DropInRequest dropInRequest = new DropInRequest().clientToken(token);
        startActivityForResult(dropInRequest.getIntent(getApplicationContext()), PAYMENT_REQUEST);
    }

    private void configureUI(){
        TextView tv_address = findViewById(R.id.tv_address);
        tv_price = findViewById(R.id.tv_price);
        TextView tv_description = findViewById(R.id.tv_description);
        TextView tv_reservation_startdate = findViewById(R.id.tv_reservation_startdate);
        TextView tv_reservation_enddate = findViewById(R.id.tv_reservation_enddate);
        TextView tv_date_non_dispo = findViewById(R.id.tv_date_non_dispo);
        Button b_register = findViewById(R.id.b_register);
        FloatingActionButton navigate = findViewById(R.id.iv_navigate);

        tv_address.setText(garageClicked.getAddress());
        tv_price.setText(String.format("%s€", String.valueOf(garageClicked.getPrice())));
        tv_description.setText(garageClicked.getDescription());

        tv_date_non_dispo.setVisibility(View.GONE);

        NumberPicker np_starttimepicker = findViewById(R.id.np_starttimepicker);
        NumberPicker np_endtimepicker = findViewById(R.id.np_endtimepicker);
        // Handle Time Picker
        List<String> displayedValues = new ArrayList<>();
        for(int i=0;i<24;i++){
            displayedValues.add(String.format("%02d:00", i));
            displayedValues.add(String.format("%02d:30", i));
        }
        np_starttimepicker.setMinValue(0);
        np_starttimepicker.setMaxValue(displayedValues.size() - 1);
        np_starttimepicker.setDisplayedValues(displayedValues.toArray(new String[0]));
        np_starttimepicker.setValue(16);

        np_endtimepicker.setMinValue(0);
        np_endtimepicker.setMaxValue(displayedValues.size() - 1);
        np_endtimepicker.setDisplayedValues(displayedValues.toArray(new String[0]));
        np_endtimepicker.setValue(34);

        // Handle Time Picker
        DatePickerBuilder builder = new DatePickerBuilder(this, calendars -> {
            tv_reservation_startdate.setText(String.format("%02d/%02d/%02d", calendars.get(0).get(Calendar.DAY_OF_MONTH),
                    calendars.get(0).get(Calendar.MONTH) + 1,
                    calendars.get(0).get(Calendar.YEAR)));
            tv_reservation_enddate.setText(String.format("%02d/%02d/%02d", calendars.get(calendars.size() - 1).get(Calendar.DAY_OF_MONTH),
                    calendars.get(calendars.size() - 1).get(Calendar.MONTH) + 1,
                    calendars.get(calendars.size() - 1).get(Calendar.YEAR)));
        }).pickerType(CalendarView.RANGE_PICKER);

        tv_reservation_startdate.setOnClickListener(v -> {
            DatePicker datePicker = builder.build();
            datePicker.show();
        });

        tv_reservation_enddate.setOnClickListener(v -> {
            DatePicker datePicker = builder.build();
            datePicker.show();
        });

        b_register.setOnClickListener(v -> {
            String startTime = np_starttimepicker.getDisplayedValues()[np_starttimepicker.getValue()];
            String endTime = np_endtimepicker.getDisplayedValues()[np_endtimepicker.getValue()];
            NonAvailableTimeHelper.getNonAvailableTimeCollection(garageClicked.getOwnerID(), garageClicked.getUid()).get().addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    for(DocumentSnapshot document : task.getResult()){
                        NonAvailableTime nonAvailableTime = document.toObject(NonAvailableTime.class);
                        Date startDate = new Date();
                        Date endDate = new Date();

                        startDate.setTime(stringToDate(tv_reservation_startdate.getText().toString()).getTime() + Long.valueOf(startTime.split(":")[0])*3600000 + Long.valueOf(startTime.split(":")[1])*60000);
                        endDate.setTime(stringToDate(tv_reservation_enddate.getText().toString()).getTime() + Long.valueOf(endTime.split(":")[0])*3600000 + Long.valueOf(endTime.split(":")[1])*60000);

                        if(startDate.before(nonAvailableTime.getStartTime()) && endDate.before(nonAvailableTime.getEndTime()) ||
                                startDate.after(nonAvailableTime.getStartTime()) && endDate.after(nonAvailableTime.getEndTime())){
                            //new getToken().execute();
                            tv_date_non_dispo.setVisibility(View.GONE);
                            Toast.makeText(this, "Le garage est disponible aux dates choisies", Toast.LENGTH_SHORT).show();
                        } else {
                            tv_date_non_dispo.setVisibility(View.VISIBLE);
                        }


                    }
                } else {
                    Log.e("ERROR", "Document not found");
                }
            }).addOnFailureListener(this.onFailureListener());
        });

        navigate.setOnClickListener(v -> {
            String url = "http://maps.google.com/maps?&daddr=" +
            garageClicked.getAddress().replace(" ", "+");
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });
    }

    private void sendPayments() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, API_CHECK_OUT, response -> {
            if(response.contains("Successful"))
                Toast.makeText(this, "Transaction Successful", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Transaction Failed", Toast.LENGTH_SHORT).show();
            Log.e("ERROR123456", response);
        }, error ->Log.e("ERROR123456", error.toString())) {
            @Override
            protected Map<String, String> getParams() {
                if(paramsHash == null) return null;
                Map<String, String> params = new HashMap<>();
                for(String key : params.keySet()){
                    params.put(key, Objects.requireNonNull(paramsHash.get(key)));
                }
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(stringRequest);
    }


    public Date stringToDate(String stringDatum) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");

        try {
            return format.parse(stringDatum);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }



    // Braintree Configuration
    private class getToken extends AsyncTask{
        AlertDialog ttest;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlertDialog.Builder test = new AlertDialog.Builder(context)
                    .setMessage("Veuillez Patienter");
            test.show();
            ttest = test.create();
            ttest.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            HttpClient client = new HttpClient();
            client.get(API_GET_TOKEN, new HttpResponseCallback() {
                @Override
                public void success(String responseBody) {
                    runOnUiThread(() -> {
                        //set Token
                        token = responseBody;
                    });
                }

                @Override
                public void failure(Exception exception) {
                    Log.e("ERROR", exception.toString());
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            ttest.dismiss();
        }
    }
}
