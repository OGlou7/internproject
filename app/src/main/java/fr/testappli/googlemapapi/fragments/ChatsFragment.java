package fr.testappli.googlemapapi.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.UserAdapter;
import fr.testappli.googlemapapi.api.ChatHelper;
import fr.testappli.googlemapapi.api.UserHelper;
import fr.testappli.googlemapapi.models.User;
import fr.testappli.googlemapapi.notifications.Token;


public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;

    FirebaseUser fuser;

    private List<User> usersList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        usersList = new ArrayList<>();

        ChatHelper.getChatCollection().addSnapshotListener((queryDocumentSnapshots, e) -> {
            usersList.clear();
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                if(document.getId().contains(fuser.getUid())){
                    UserHelper.getUser(document.getId().replace(fuser.getUid(), "")).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            DocumentSnapshot documentUser = task1.getResult();
                            User user = documentUser.toObject(User.class);
                            usersList.add(user);
                        }
                        if(task1.isComplete()) updateUI();
                    });
                }
            }
        });
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String token = task.getResult().getToken();
                        updateToken(token);
                    }
                });
        return view;
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(fuser.getUid()).setValue(token1);
    }

    private void updateUI(){
        userAdapter = new UserAdapter(getContext(), usersList);
        recyclerView.setAdapter(userAdapter);
    }
}