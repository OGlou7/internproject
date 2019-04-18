package fr.testappli.googlemapapi.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.List;

import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.api.MessageHelper;
import fr.testappli.googlemapapi.chat.MessageActivity;
import fr.testappli.googlemapapi.models.Message2;
import fr.testappli.googlemapapi.models.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;

    String theLastMessage;

    public UserAdapter(Context mContext, List<User> mUsers){
        this.mUsers = mUsers;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final User user = mUsers.get(position);
        holder.username.setText(user.getUsername());
        if (user.getUrlPicture() == null){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getUrlPicture()).into(holder.profile_image);
        }


        lastMessage(user, holder.last_msg);

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, MessageActivity.class);
            intent.putExtra("userid", user.getUid());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;
        private TextView last_msg;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            last_msg = itemView.findViewById(R.id.last_msg);
        }
    }

    //check for last message
    private void lastMessage(final User user, final TextView last_msg){
        String userid = user.getUid();
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String chatID = generateChatID(userid, firebaseUser.getUid());
        MessageHelper.getMessageCollectionForChat(chatID).addSnapshotListener((queryDocumentSnapshots, e) -> {
            MessageHelper.getLastMessageForChat(chatID).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                for (DocumentSnapshot document : queryDocumentSnapshots1.getDocuments()) {
                    Message2 message = document.toObject(Message2.class);
                    if (message != null) {
                        if (message.getReceiver().equals(firebaseUser.getUid()) && message.getSender().equals(userid) ||
                                message.getReceiver().equals(userid) && message.getSender().equals(firebaseUser.getUid())) {
                            theLastMessage = message.getMessage();
                        }
                    }
                }
                switch (theLastMessage){
                    case  "default":
                        last_msg.setText("No Message");
                        break;

                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }

                theLastMessage = "default";
            });
        });
    }

    private String generateChatID(String sender, String receiver){
        return sender.compareTo(receiver) < 0 ? sender + receiver : receiver + sender;
    }
}