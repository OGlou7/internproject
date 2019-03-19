package fr.testappli.googlemapapi;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.content.Intent;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String URL_FOR_LOGIN = "https://XXX.XXX.X.XX/android_login_example/login.php";
    ProgressDialog progressDialog;
    private EditText loginInputEmail, loginInputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginInputEmail = findViewById(R.id.login_input_email);
        loginInputPassword = findViewById(R.id.login_input_password);
        Button btnlogin = findViewById(R.id.btn_login);
        Button btnLinkSignup = findViewById(R.id.btn_link_signup);
        // Progress dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);

        btnlogin.setOnClickListener(view -> loginUser(loginInputEmail.getText().toString(),
                loginInputPassword.getText().toString()));

        btnLinkSignup.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(i);
            finish();
        });
    }

    private void loginUser( final String email, final String password) {
        // Tag used to cancel the request
        String cancel_req_tag = "login";
        progressDialog.setMessage("Logging you in...");
        showDialog();
        finish();
        /*StringRequest strReq = new StringRequest(Request.Method.POST,
                URL_FOR_LOGIN, response -> {
                    Log.d(TAG, "Register Response: " + response);
                    hideDialog();
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");

                        if (!error) {
                            String user = jObj.getJSONObject("user").getString("name");
                            // Launch User activity
                            Intent intent = new Intent(
                                    LoginActivity.this,
                                    MainActivity.class);
                            intent.putExtra("username", user);
                            startActivity(intent);
                            finish();
                        } else {

                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(getApplicationContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, error -> {
                    Log.e(TAG, "Login Error: " + error.getMessage());
                    Toast.makeText(getApplicationContext(),
                            error.getMessage(), Toast.LENGTH_LONG).show();
                    hideDialog();
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Posting params to login url
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                params.put("password", password);
                return params;
            }
        };*/
        // Adding request to request queue
        //AppSingleton.getInstance(getApplicationContext()).addToRequestQueue(strReq,cancel_req_tag);
    }

    private void showDialog() {
        if (!progressDialog.isShowing())
            progressDialog.show();
    }
    private void hideDialog() {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}