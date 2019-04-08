package fr.testappli.googlemapapi.base;

import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;

import java.util.Arrays;
import java.util.Objects;

import fr.testappli.googlemapapi.MainActivity;
import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.api.UserHelper;
import fr.testappli.googlemapapi.models.User;


public class LoginActivity extends BaseActivity {

    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
            if (this.isCurrentUserLogged()){
                this.startMapActivity();
            } else {
                this.startSignInActivity();
            }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        this.handleResponseAfterSignIn(requestCode, resultCode, data);
    }

    private void handleResponseAfterSignIn(int requestCode, int resultCode, Intent data){

        IdpResponse response = IdpResponse.fromResultIntent(data);

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                this.createUserInFirestore();
                Toast.makeText(getApplicationContext(), getString(R.string.connection_succeed), Toast.LENGTH_LONG).show();
                startMapActivity();
            } else {
                if (response == null) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_authentication_canceled), Toast.LENGTH_LONG).show();
                } else if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_no_internet), Toast.LENGTH_LONG).show();
                } else if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_unknown_error), Toast.LENGTH_LONG).show();
                }
                finish();
            }
        }
    }

    private void startMapActivity(){
        Intent mapActivity = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mapActivity);
        finish();
    }

    private void startSignInActivity(){
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(
                                Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(),
                                        new AuthUI.IdpConfig.GoogleBuilder().build()))
                        .setIsSmartLockEnabled(false, true)
                        .setLogo(R.drawable.ic_action_refresh)
                        .build(),
                RC_SIGN_IN);

    }

    // REST REQUEST
    private void createUserInFirestore(){
        if (this.getCurrentUser() != null){

            String urlPicture = (this.getCurrentUser().getPhotoUrl() != null) ? this.getCurrentUser().getPhotoUrl().toString() : null;
            String username = this.getCurrentUser().getDisplayName();
            String uid = this.getCurrentUser().getUid();

            UserHelper.getUser(uid)
                    .addOnSuccessListener(documentSnapshot -> {
                        User modelCurrentUser = documentSnapshot.toObject(User.class);
                        if(modelCurrentUser == null){
                            UserHelper.createUser(uid, username, urlPicture).addOnFailureListener(this.onFailureListener());
                        }
                    });
        }
    }

    @Override
    public int getFragmentLayout() { return R.layout.activity_profile; }
}