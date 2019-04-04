package fr.testappli.googlemapapi.garage;

import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.lang.reflect.Field;

import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.api.GarageHelper;
import fr.testappli.googlemapapi.models.Garage;

public class GarageListAdapter extends FirestoreRecyclerAdapter<Garage, GarageListAdapter.GarageViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Garage item);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Garage item);
    }

    public interface OnClickListener {
        void onClickListener(View v, Garage g);
    }

    private final OnItemClickListener onItemClickListener;
    private final OnItemLongClickListener onItemLongClickListener;
    private final OnClickListener onMoreClickListener;
    private final OnClickListener onCheckClickListener;

    public GarageListAdapter(@NonNull FirestoreRecyclerOptions<Garage> options, OnItemClickListener onItemClickListener,
                             OnItemLongClickListener onItemLongClickListener, OnClickListener onMoreClickListener, OnClickListener onCheckClickListener) {
        super(options);
        this.onItemClickListener = onItemClickListener;
        this.onItemLongClickListener = onItemLongClickListener;
        this.onMoreClickListener = onMoreClickListener;
        this.onCheckClickListener = onCheckClickListener;
    }

    @Override
    protected void onBindViewHolder(@NonNull GarageViewHolder holder, int position, @NonNull Garage model) {
        String address = model.getAddress().split(",")[0];
        String city = model.getAddress().split(",")[1];
        //holder.tv_row_description.setText(model.getDescription());
        holder.tv_row_garage_address.setText(String.format("%s%s%s", address, System.lineSeparator(), city));
        holder.itemView.setOnClickListener(v -> onItemClickListener.onItemClick(model));
        holder.itemView.setOnLongClickListener(v -> {
            onItemLongClickListener.onItemLongClick(model);
            return true;
        });
        holder.iv_garage_row_more.setOnClickListener(v -> onMoreClickListener.onClickListener(holder.itemView, model));
        holder.cb_row_garage_is_reserved.setChecked(model.getisAvailable());
        holder.cb_row_garage_is_reserved.setOnClickListener(v -> onCheckClickListener.onClickListener(holder.itemView, model));
    }

    @NonNull
    @Override
    public GarageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_garage, viewGroup, false);
        return new GarageViewHolder(v);
    }

    class GarageViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_row_image_garage;
        TextView tv_row_garage_address;
        //TextView tv_row_description;
        ImageView iv_garage_row_more;
        CheckBox cb_row_garage_is_reserved;

        public GarageViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_row_image_garage = itemView.findViewById(R.id.iv_row_image_garage);
            tv_row_garage_address = itemView.findViewById(R.id.tv_row_garage_address);
            //tv_row_description = itemView.findViewById(R.id.tv_row_description);
            iv_garage_row_more = itemView.findViewById(R.id.iv_garage_row_more);
            cb_row_garage_is_reserved = itemView.findViewById(R.id.cb_row_garage_is_reserved);
        }
    }
}
