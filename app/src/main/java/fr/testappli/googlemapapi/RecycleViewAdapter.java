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

import fr.testappli.googlemapapi.models.Garage;

class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public void setNavigationClickListener(NavigationClickListener navigationClickListener) {
        this.mNavigationClickListener = navigationClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    public interface NavigationClickListener {
        void onItemClick(View view, int position);
    }

    private LayoutInflater mInflater;
    private ArrayList<Garage> garageDataList;
    private Context contextAdapter;

    private ItemClickListener mClickListener;
    private NavigationClickListener mNavigationClickListener;

    MyRecyclerViewAdapter(Context context, ArrayList<Garage> garageDataList) {
        this.mInflater = LayoutInflater.from(context);
        this.garageDataList = garageDataList;
        this.contextAdapter = context;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.reservation_list_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setContext(contextAdapter);
        Garage data = garageDataList.get(position);
        holder.address.setText(data.getAddress().split(",")[0]);
        holder.city.setText(data.getAddress().split(",")[1]);
        holder.price.setText(String.format("%s€", String.valueOf(data.getPrice())));
        holder.itemView.setOnClickListener(v -> mClickListener.onItemClick(holder.itemView, position));
        holder.navigate.setOnClickListener(v -> mNavigationClickListener.onItemClick(holder.itemView, position));
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView address, city, price;
        FloatingActionButton navigate;
        Context contextHolder;

        ViewHolder(View itemView) {
            super(itemView);
            address = itemView.findViewById(R.id.tv_address);
            city = itemView.findViewById(R.id.tv_city);
            price = itemView.findViewById(R.id.tv_price);
            navigate = itemView.findViewById(R.id.iv_navigate);
        }

        void setContext(Context context){ this.contextHolder = context;}
    }

    // convenience method for getting data at click position
    public Garage getItem(int id) {
        return garageDataList.get(id);
    }

    @Override
    public int getItemCount() {
        return garageDataList.size();
    }

    int getGaragePositionByAddress(String address){
        for(Garage garage : garageDataList){
            if(garage.getAddress().equals(address))
                return garageDataList.indexOf(garage);
        }
        return 0;
    }
}