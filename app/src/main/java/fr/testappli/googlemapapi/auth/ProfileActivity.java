package fr.testappli.googlemapapi.auth;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import java.util.UUID;

import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.api.UserHelper;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.models.User;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class ProfileActivity extends BaseActivity {

    private static final int PROFILEACTIVITY_REQUEST = 2;
    private TextInputEditText textInputEditTextUsername;
    private TextView TextViewEmail;
    private EditText EditTextViewImmmatriculation;
    private ImageView imageViewProfile;
    private ProgressBar progressBar;
    private CheckBox checkBoxIsVendor;
    private LinearLayout layoutButton;

    private static final int SIGN_OUT_TASK = 10;
    private static final int DELETE_USER_TASK = 20;
    private static final int UPDATE_USERNAME = 30;

    private User modelCurrentUser;

    // FOR PICTURES
    private static final String PERMS = Manifest.permission.READ_EXTERNAL_STORAGE;
    private static final int RC_IMAGE_PERMS = 100;
    private Uri uriImageSelected;
    private static final int RC_CHOOSE_PHOTO = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FOR DESIGN
        imageViewProfile = findViewById(R.id.profile_activity_imageview_profile);
        textInputEditTextUsername = findViewById(R.id.profile_activity_edit_text_username);
        TextViewEmail = findViewById(R.id.profile_activity_text_view_email);
        EditTextViewImmmatriculation = findViewById(R.id.profile_activity_text_view_immatriculation);
        progressBar = findViewById(R.id.profile_activity_progress_bar);
        checkBoxIsVendor = findViewById(R.id.profile_activity_check_box_is_vendor);
        Button update = findViewById(R.id.profile_activity_button_update);
        Button signOut = findViewById(R.id.profile_activity_button_sign_out);
        Button delete = findViewById(R.id.profile_activity_button_delete);
        layoutButton = findViewById(R.id.layoutButton);

        imageViewProfile.setOnClickListener(v -> onClickAddFile());
        checkBoxIsVendor.setOnClickListener(v -> this.updateUserIsVendor());
        update.setOnClickListener(v -> {
            this.updateUsernameInFirebase();
            this.updateUserImmatriculationInFirebase();
            Snackbar.make(layoutButton, "Profile mis à jour", Snackbar.LENGTH_LONG).show();
        });
        signOut.setOnClickListener(v -> this.signOutUserFromFirebase());
        delete.setOnClickListener(v -> new AlertDialog.Builder(this)
                .setMessage(R.string.popup_message_confirmation_delete_account)
                .setPositiveButton(R.string.popup_message_choice_yes, (dialogInterface, i) -> deleteUserFromFirebase())
                .setNegativeButton(R.string.popup_message_choice_no, null)
                .show());

        Intent intent = getIntent();
        Bundle bundleModelUser = Objects.requireNonNull(intent.getExtras()).getBundle("modelCurrentUser");
        modelCurrentUser = (User) Objects.requireNonNull(Objects.requireNonNull(bundleModelUser).getSerializable("modelCurrentUser"));

        //TOOLBAR
        // setup the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(v -> finishActivity());

        this.updateUIWhenCreating();
    }

    private void finishActivity(){
        Intent data = new Intent();
        Bundle bundleModelCurrentUser = new Bundle();
        bundleModelCurrentUser.putSerializable("modelCurrentUser", modelCurrentUser);
        data.putExtra("modelCurrentUser", bundleModelCurrentUser);
        setResult(PROFILEACTIVITY_REQUEST,data);
        finish();
    }

    private void updateUsernameInFirebase(){
        this.progressBar.setVisibility(View.VISIBLE);
        String username = Objects.requireNonNull(this.textInputEditTextUsername.getText()).toString();

        if (this.getCurrentUser() != null){
            if (!username.isEmpty() &&  !username.equals(getString(R.string.info_no_username_found))){
                modelCurrentUser.setUsername(username);
                UserHelper.updateUsername(username, this.getCurrentUser().getUid()).addOnFailureListener(this.onFailureListener()).addOnSuccessListener(this.updateUIAfterRESTRequestsCompleted(UPDATE_USERNAME));
            }
        }
    }

    private void updateUserIsVendor(){
        if (this.getCurrentUser() != null) {
            modelCurrentUser.setIsVendor(this.checkBoxIsVendor.isChecked());
            UserHelper.updateIsVendor(this.getCurrentUser().getUid(), this.checkBoxIsVendor.isChecked()).addOnFailureListener(this.onFailureListener());
        }
    }

    private void updateUserImmatriculationInFirebase(){
        if (this.getCurrentUser() != null) {
            modelCurrentUser.setImmatriculation(this.EditTextViewImmmatriculation.getText().toString());
            UserHelper.updateImmatriculation(this.getCurrentUser().getUid(), this.EditTextViewImmmatriculation.getText().toString()).addOnFailureListener(this.onFailureListener());
        }
    }


    private void signOutUserFromFirebase(){
        AuthUI.getInstance()
                .signOut(this)
                .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
        this.logOut();
    }

    // DELETE
    private void deleteUserFromFirebase(){
        if (this.getCurrentUser() != null) {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnSuccessListener(this, this.updateUIAfterRESTRequestsCompleted(SIGN_OUT_TASK));
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
        finishActivity();
    }

    private OnSuccessListener<Void> updateUIAfterRESTRequestsCompleted(final int origin){
        return aVoid -> {
            switch (origin){
                case SIGN_OUT_TASK:
                    finishActivity();
                    break;
                case DELETE_USER_TASK:
                    finishActivity();
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
            if (modelCurrentUser.getUrlPicture() != null && !modelCurrentUser.getUrlPicture().isEmpty()) {
                Glide.with(this)
                        .load(Uri.parse(modelCurrentUser.getUrlPicture()))
                        .apply(RequestOptions.circleCropTransform())
                        .into(imageViewProfile);
            }

            //Get email & username from Firebase
            String email = TextUtils.isEmpty(this.getCurrentUser().getEmail()) ? getString(R.string.info_no_email_found) : this.getCurrentUser().getEmail();
            this.TextViewEmail.setText(email);

            // Get additional data from Firestore (isVendor & Username)
            UserHelper.getUser(this.getCurrentUser().getUid()).addOnSuccessListener(documentSnapshot -> {
                User currentUser = documentSnapshot.toObject(User.class);
                String username1 = TextUtils.isEmpty(Objects.requireNonNull(currentUser).getUsername()) ? getString(R.string.info_no_username_found) : currentUser.getUsername();
                String immatriculation = currentUser.getImmatriculation();
                checkBoxIsVendor.setChecked(currentUser.getIsVendor());
                textInputEditTextUsername.setText(username1);
                EditTextViewImmmatriculation.setText(immatriculation);
            });
        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Calling the appropriate method after activity result
        this.handleResponse(requestCode, resultCode, data);
    }


    @AfterPermissionGranted(RC_IMAGE_PERMS)
    public void onClickAddFile() { this.chooseImageFromPhone(); }

    @Override
    public void onBackPressed() { finishActivity(); }

    // --------------------
    // REST REQUESTS
    // --------------------
    private void chooseImageFromPhone(){
        if (!EasyPermissions.hasPermissions(this, PERMS)) {
            EasyPermissions.requestPermissions(this, getString(R.string.popup_title_permission_files_access), RC_IMAGE_PERMS, PERMS);
            return;
        }
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RC_CHOOSE_PHOTO);
    }

    private void handleResponse(int requestCode, int resultCode, Intent data){
        if (requestCode == RC_CHOOSE_PHOTO) {
            if (resultCode == RESULT_OK) {
                this.uriImageSelected = data.getData();
                Glide.with(this)
                        .load(this.uriImageSelected)
                        .apply(RequestOptions.circleCropTransform())
                        .into(this.imageViewProfile);


                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                        .setPhotoUri(this.uriImageSelected)
                        .build();

                Objects.requireNonNull(getCurrentUser()).updateProfile(profileUpdates)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Log.e("handleResponse", "User profile updated.");
                            }
                        });
                uploadPhotoInFirebase();
            }
        } else {
            Toast.makeText(this, getString(R.string.toast_title_no_image_chosen), Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadPhotoInFirebase() {
        String uuid = UUID.randomUUID().toString();

        StorageReference mImageRef = FirebaseStorage.getInstance().getReference(uuid);
        mImageRef.putFile(this.uriImageSelected)
                .addOnSuccessListener(this, taskSnapshot -> {
                    Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                    firebaseUri.addOnSuccessListener(uri -> {
                        UserHelper.updatePhotoURI(Objects.requireNonNull(getCurrentUser()).getUid(), uri.toString());
                        modelCurrentUser.setUrlPicture(uri.toString());
                    });

                })
                .addOnFailureListener(this.onFailureListener());
    }


    @Override
    public int getFragmentLayout() { return R.layout.activity_profile; }
}
