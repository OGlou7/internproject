package fr.testappli.googlemapapi.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.base.BaseActivity;
import fr.testappli.googlemapapi.fragments.ChatsFragment;
import fr.testappli.googlemapapi.fragments.UsersFragment;

public class ChatActivity extends BaseActivity {
    CircleImageView profile_image;
    TextView username;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        configureToolbar();
        configureUI();
    }

    private void configureToolbar(){
        Toolbar toolbar = findViewById(R.id.toolbar_chat);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void configureUI(){
        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.tv_username);

        username.setText(Objects.requireNonNull(getCurrentUser()).getDisplayName());
        if(getCurrentUser().getPhotoUrl() == null){
            profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(this)
                    .load(getCurrentUser().getPhotoUrl())
                    .apply(RequestOptions.circleCropTransform())
                    .into(profile_image);
        }

        TabLayout tab_layout_chat = findViewById(R.id.tab_layout_chat);
        ViewPager view_pager_chat = findViewById(R.id.view_pager_chat);

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());


        viewPagerAdapter.addFragment(new ChatsFragment(), "chats");
        viewPagerAdapter.addFragment(new UsersFragment(), "users");

        view_pager_chat.setAdapter(viewPagerAdapter);
        tab_layout_chat.setupWithViewPager(view_pager_chat);


        /*DatabaseReference reference = FirebaseDatabase.getInstance().getReference("chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
                int unread = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Message2 message2 = snapshot.getValue(Message2.class);
                    Log.e("TESTTEST222", message2.getMessage());
                    if (message2.getReceiver().equals(getCurrentUser().getUid()) && !message2.isIsseen()){
                        unread++;
                        Log.e("TESTTEST223","" + unread);
                    }
                }

                if (unread == 0){
                    viewPagerAdapter.addFragment(new ChatsFragment(), "Chats");
                } else {
                    viewPagerAdapter.addFragment(new ChatsFragment(), "("+unread+") Chats");
                }

                viewPagerAdapter.addFragment(new UsersFragment(), "Users");

                view_pager_chat.setAdapter(viewPagerAdapter);
                tab_layout_chat.setupWithViewPager(view_pager_chat);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
    }

    @Override
    public int getFragmentLayout() { return R.layout.activity_profile; }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        ViewPagerAdapter(FragmentManager fm){
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        private void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}
