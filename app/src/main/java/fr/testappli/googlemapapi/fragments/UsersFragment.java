package fr.testappli.googlemapapi.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.UserAdapter;
import fr.testappli.googlemapapi.api.UserHelper;
import fr.testappli.googlemapapi.models.User;


public class UsersFragment extends Fragment {

    private RecyclerView recyclerView;

    private UserAdapter userAdapter;
    private List<User> mUsers;

    EditText search_users;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_users, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUsers = new ArrayList<>();

        readUsers();

        search_users = view.findViewById(R.id.search_users);
        search_users.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUsers(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    private void searchUsers(String s) {
        List<User> usersSearch = new ArrayList<>();

        for(User user : mUsers){
            if(Pattern.compile(Pattern.quote(s), Pattern.CASE_INSENSITIVE).matcher(user.getUsername()).find()){
                usersSearch.add(user);
            }
        }
        userAdapter = new UserAdapter(getContext(), usersSearch);
        recyclerView.setAdapter(userAdapter);
    }

    private void readUsers() {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        UserHelper.getUsersCollection().get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                    mUsers.clear();
                    for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                        User user = document.toObject(User.class);
                        if (!user.getUid().equals(Objects.requireNonNull(firebaseUser).getUid())) {
                            mUsers.add(user);
                        }
                    }
                    userAdapter = new UserAdapter(getContext(), mUsers);
                    recyclerView.setAdapter(userAdapter);
            } else {
                Log.e("configureMapPointers", "Error getting documents: ", task.getException());
            }
        });
    }
}