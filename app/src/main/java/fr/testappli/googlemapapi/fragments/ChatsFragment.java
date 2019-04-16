package fr.testappli.googlemapapi.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.UserAdapter;
import fr.testappli.googlemapapi.api.ChatHelper;
import fr.testappli.googlemapapi.api.UserHelper;
import fr.testappli.googlemapapi.models.User;


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
            Log.e("testtest88888", queryDocumentSnapshots.getDocuments().get(0).getId());
        });

        ChatHelper.getChatCollection().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                usersList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    if(document.getId().contains(fuser.getUid())){
                        UserHelper.getUser(document.getId().replace(fuser.getUid(), "")).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                DocumentSnapshot documentUser = task1.getResult();
                                usersList.add(documentUser.toObject(User.class));
                            }
                            if(task1.isComplete()) updateUI();
                        });
                    }
                }
            } else {
                Log.e("ERROR", "Error getting documents: ", task.getException());
            }

            userAdapter = new UserAdapter(getContext(), usersList);
            recyclerView.setAdapter(userAdapter);
        });
        return view;
    }

    private void updateUI(){
        userAdapter = new UserAdapter(getContext(), usersList);
        recyclerView.setAdapter(userAdapter);
    }
}