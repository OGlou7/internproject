package fr.testappli.googlemapapi.garage;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import fr.testappli.googlemapapi.R;
import fr.testappli.googlemapapi.models.Garage;

public class GarageListAdapter extends FirestoreRecyclerAdapter<Garage, GarageListAdapter.GarageViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Garage item);
    }

    private final OnItemClickListener listener;

    public GarageListAdapter(@NonNull FirestoreRecyclerOptions<Garage> options, OnItemClickListener listener) {
        super(options);
        this.listener = listener;
    }

    @Override
    protected void onBindViewHolder(@NonNull GarageViewHolder holder, int position, @NonNull Garage model) {
        holder.tv_row_description.setText(model.getDescription());
        holder.tv_row_garage_address.setText(model.getAddress());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(model));

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
        TextView tv_row_description;

        public GarageViewHolder(@NonNull View itemView) {
            super(itemView);
            iv_row_image_garage = itemView.findViewById(R.id.iv_row_image_garage);
            tv_row_garage_address = itemView.findViewById(R.id.tv_row_garage_address);
            tv_row_description = itemView.findViewById(R.id.tv_row_description);
        }
    }
}
