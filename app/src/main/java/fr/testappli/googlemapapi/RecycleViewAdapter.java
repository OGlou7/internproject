package fr.testappli.googlemapapi;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private ArrayList<Reservation> reservationDataList;
    private Context context;

    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private View lastView = null;

    // data is passed into the constructor
    MyRecyclerViewAdapter(Context context, ArrayList<Reservation> reservationDataList) {
        this.mInflater = LayoutInflater.from(context);
        this.reservationDataList = reservationDataList;
        this.context = context;
    }

    // inflates the row layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.reservation_list_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the view and textview in each row
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setContext(context);
        Reservation data = reservationDataList.get(position);
        holder.address.setText(data.getAddress());
        holder.city.setText(data.getCity());
        holder.price.setText(String.format("%s€", String.valueOf(data.getPrice())));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return reservationDataList.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView address, city, price;
        FloatingActionButton navigate;
        Context context;

        ViewHolder(View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.tv_address);
            city = itemView.findViewById(R.id.tv_city);
            price = itemView.findViewById(R.id.tv_price);
            navigate = itemView.findViewById(R.id.iv_navigate);
            navigate.setOnClickListener(v -> this.onClick(itemView));
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
//            if(lastView != null)
//                lastView.findViewById(R.id.parent).setBackgroundColor(ContextCompat.getColor(this.context, R.color.myGreen));
//            lastView = view;
        }

        void setContext(Context context){ this.context = context;}
    }

    // convenience method for getting data at click position
    public Reservation getItem(int id) {
        return reservationDataList.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}