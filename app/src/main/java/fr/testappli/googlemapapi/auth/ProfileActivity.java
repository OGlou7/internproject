package fr.testappli.googlemapapi.auth;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Objects;

import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.api.UserHelper;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.models.User;

public class ProfileActivity extends BaseActivity {

    private TextInputEditText textInputEditTextUsername;
    private TextView textViewEmail;
    private ImageView imageViewProfile;
    private ProgressBar progressBar;
    private CheckBox checkBoxIsVendor;

    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    private static final int UPDATE_USERNAME = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FOR DESIGN
        imageViewProfile = findViewById(R.id.profile_activity_imageview_profile);
        textInputEditTextUsername = findViewById(R.id.profile_activity_edit_text_username);
        textViewEmail = findViewById(R.id.profile_activity_text_view_email);
        progressBar = findViewById(R.id.profile_activity_progress_bar);
        checkBoxIsVendor = findViewById(R.id.profile_activity_check_box_is_vendor);
        Button update = findViewById(R.id.profile_activity_button_update);
        Button signOut = findViewById(R.id.profile_activity_button_sign_out);
        Button delete = findViewById(R.id.profile_activity_button_delete);

        checkBoxIsVendor.setOnClickListener(v -> this.updateUserIsVendor());
        update.setOnClickListener(v -> this.updateUsernameInFirebase());
        signOut.setOnClickListener(v -> this.signOutUserFromFirebase());
        delete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setMessage(R.string.popup_message_confirmation_delete_account)
                .setPositiveButton(R.string.popup_message_choice_yes, (dialogInterface, i) -> deleteUserFromFirebase())
                .setNegativeButton(R.string.popup_message_choice_no, null)
                .show());

        //TOOLBAR
        // setup the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finish());

        this.updateUIWhenCreating();
    }

    // Update User Username
    private void updateUsernameInFirebase(){
        this.progressBar.setVisibility(View.VISIBLE);
        String username = Objects.requireNonNull(this.textInputEditTextUsername.getText()).toString();

        if (this.getCurrentUser() != null){
            if (!username.isEmpty() &&  !username.equals(getString(R.string.info_no_username_found))){
                UserHelper.updateUsername(username, this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener()).addOnSuccessListener(this.updateUIAfterRESTRequestsCompleted(UPDATE_USERNAME));
            }
        }
    }

    private void updateUserIsVendor(){
        if (this.getCurrentUser() != null) {
            UserHelper.updateIsVendor(this.getCurrentUser().getUid(), this.checkBoxIsVendor.isChecked()).addOnFailureListener(this.onFailureListener());
        }
    }


    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
        this.logOut();
    }

    private void deleteUserFromFirebase(){
        if (this.getCurrentUser() != null) {
            UserHelper.deleteUser(this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener());
            AuthUI.getInstance()
                    .delete(this)
                    .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(DELETE_USER_TASK));
        }
        this.logOut();
    }

    private void logOut(){
        Intent intent = new Intent("finish");
        sendBroadcast(intent);
        finish();
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return aVoid -> {
            switch (origin){
                case SIGN_OUT_TASK:
                    finish();
                    break;
                case DELETE_USER_TASK:
                    finish();
                    break;
                case UPDATE_USERNAME:
                    progressBar.setVisibility(View.INVISIBLE);
                    break;
                default:
                    break;
            }
        };
    }

    private void updateUIWhenCreating(){
        if (this.getCurrentUser() != null){

            //Get picture URL from Firebase
            if (this.getCurrentUser().getPhotoUrl() != null) {
                Glide.with(this)
                        .load(this.getCurrentUser().getPhotoUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageViewProfile);
            }

            //Get email & username from Firebase
            String email = TextUtils.isEmpty(this.getCurrentUser().getEmail()) ? getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();
            //String username = TextUtils.isEmpty(this.getCurrentUser().getDisplayName()) ? getString(R.string.info_no_username_found) : this.getCurrentUser().getDisplayName();

            //Update views with data
            //this.textInputEditTextUsername.setText(username);
            this.textViewEmail.setText(email);

            // Get additional data from Firestore (isVendor & Username)
            UserHelper.getUser(this.getCurrentUser().getUid()).addOnSuccessListener(documentSnapshot -> {
                User currentUser = documentSnapshot.toObject(User.class);
                String username1 = TextUtils.isEmpty(Objects.requireNonNull(currentUser).getUsername()) ? getString(R.string.info_no_username_found) : currentUser.getUsername();
                checkBoxIsVendor.setChecked(currentUser.getIsVendor());
                textInputEditTextUsername.setText(username1);
            });
        }
    }

    @Override
    public int getFragmentLayout() { return R.layout.activity_profile; }
}
