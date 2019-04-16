package fr.testappli.googlemapapi.form;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;
import com.braintreepayments.api.interfaces.HttpResponseCallback;
import com.braintreepayments.api.internal.HttpClient;
import com.braintreepayments.api.models.PaymentMethodNonce;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.models.Garage;

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
        Button b_register = findViewById(R.id.b_register);
        FloatingActionButton navigate = findViewById(R.id.iv_navigate);

        tv_address.setText(garageClicked.getAddress());
        tv_price.setText(String.format("%s€", String.valueOf(garageClicked.getPrice())));
        tv_description.setText(garageClicked.getDescription());
        b_register.setOnClickListener(v -> new getToken().execute());

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
                    Log.e("TESTTEST444", exception.toString());
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
