package fr.testappli.googlemapapi.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.testappli.googlemapapi.MessageAdapter;
import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.api.ChatHelper;
import fr.testappli.googlemapapi.api.MessageHelper;
import fr.testappli.googlemapapi.api.UserHelper;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.fragments.APIService;
import fr.testappli.googlemapapi.models.Message2;
import fr.testappli.googlemapapi.models.User;
import fr.testappli.googlemapapi.notifications.Client;
import fr.testappli.googlemapapi.notifications.Data;
import fr.testappli.googlemapapi.notifications.MyResponse;
import fr.testappli.googlemapapi.notifications.Sender;
import fr.testappli.googlemapapi.notifications.Token;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends BaseActivity {
    CircleImageView profile_image;
    TextView username;

    FirebaseUser fuser;

    ImageButton btn_send;
    EditText text_send;

    MessageAdapter messageAdapter;
    List<Message2> mMessage = new ArrayList<>();


    RecyclerView recyclerView;

    Intent intent;

    EventListener<QuerySnapshot> seenListener;

    String userid;

    ListenerRegistration listenerRegistration;

    APIService apiService;
    boolean notify = false;

    @Override
    public int getFragmentLayout() { return R.layout.activity_profile; }

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        configureToolbar();
        configureUI();
        configureMessages();

        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
    }

    private void configureToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void configureUI(){
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.tv_username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);

        btn_send.setOnClickListener(v -> {
            notify = true;
            String message = text_send.getText().toString();
            if(!message.isEmpty()){
                sendMessage(fuser.getUid(), userid, message);
            } else {
                Toast.makeText(this, "You can't send empty message", Toast.LENGTH_SHORT).show();
            }
            text_send.setText("");
        });
    }

    private void configureMessages(){
        intent = getIntent();
        userid = intent.getStringExtra("userid");
        fuser = getCurrentUser();

        String chatID = generateChatID(userid, fuser.getUid());
        ChatHelper.createChat(chatID).addOnFailureListener(this.onFailureListener());

        UserHelper.getUser(userid).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                User user = document.toObject(User.class);
                username.setText(user.getUsername());
                if(user.getUrlPicture() == null){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(getApplicationContext()).load(user.getUrlPicture()).into(profile_image);
                }
            } else {
                Log.e("ERROR", "Error getting documents: ", task.getException());
            }
            readMesagges(fuser.getUid(), userid, getCurrentUser().getPhotoUrl() == null ? null : getCurrentUser().getPhotoUrl().toString());
        });
        seenMessage(userid);
    }

    private void seenMessage(final String userid){
        seenListener = (documentSnapshot, e) -> MessageHelper.getAllMessageForChat(generateChatID(fuser.getUid(), userid)).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mMessage.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Message2 message = document.toObject(Message2.class);

                    if (message.getReceiver().equals(fuser.getUid()) && message.getSender().equals(userid)){
                        MessageHelper.updateIsSeen(generateChatID(fuser.getUid(), userid), document.getId(), true);
                    }
                }
            } else {
                Log.e("ERROR", "Error getting documents: ", task.getException());
            }
        });

        listenerRegistration = MessageHelper.getMessageCollectionForChat(generateChatID(fuser.getUid(), userid)).addSnapshotListener(seenListener);
    }

    private void sendMessage(String sender, String receiver, String message){
        String chatID = generateChatID(sender, receiver);
        MessageHelper.createMessageForChat(message,chatID , sender, receiver);

        final String msg = message;

        UserHelper.getUser(fuser.getUid()).addOnCompleteListener(task -> {
            if(task.isComplete()){
                User user = task.getResult().toObject(User.class);
                if (notify) {
                    sendNotifiaction(receiver, user.getUsername(), msg);
                }
                notify = false;
            }
        });

        /*DatabaseReference reference = FirebaseDatabase.getInstance().getReference("users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (notify) {
                    sendNotifiaction(receiver, user.getUsername(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
    }

    private void sendNotifiaction(String receiver, final String username, final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.mipmap.ic_launcher, username+": "+message, "New Message",
                            userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {
                                    Log.e("ERROR", t.getMessage());
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void readMesagges(final String myid, final String userid, final String imageurl){
        MessageHelper.getMessageCollectionForChat(generateChatID(fuser.getUid(), userid)).addSnapshotListener((queryDocumentSnapshots, e) -> {
            MessageHelper.getAllMessageForChat(generateChatID(fuser.getUid(), userid)).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    mMessage.clear();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Message2 message = document.toObject(Message2.class);
                        if (message.getReceiver().equals(myid) && message.getSender().equals(userid) ||
                                message.getReceiver().equals(userid) && message.getSender().equals(myid)){
                            mMessage.add(message);
                        }

                        messageAdapter = new MessageAdapter(MessageActivity.this, mMessage, imageurl);
                        recyclerView.setAdapter(messageAdapter);
                    }
                } else {
                    Log.e("ERROR", "Error getting documents: ", task.getException());
                }
            });
        });
    }


    private String generateChatID(String sender, String receiver){
        return sender.compareTo(receiver) < 0 ? sender + receiver : receiver + sender;
    }

    @Override
    protected void onStop() {
        super.onStop();
        listenerRegistration.remove();
    }
}
