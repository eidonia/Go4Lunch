package com.openclassrooms.go4lunch.ui.restaurant.list;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.ui.restaurant.ActivityWithFrag;

import java.util.ArrayList;
import java.util.List;

public class ListRestAdapter extends RecyclerView.Adapter<ListRestAdapter.ViewHolder> {

    private final Context context;
    private List<Restaurant> listRestau = new ArrayList<>();

    public ListRestAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restaurant_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant restaurant = listRestau.get(position);
        Log.d("picUrl", "" + restaurant.getPlaceId());
        holder.nameRestau.setText(restaurant.getName());
        holder.adresseRestau.setText(restaurant.getVicinity());
        holder.distanceRestau.setText("" + restaurant.getDistance() + " m");
        if (restaurant.getRating() == 1) {
            holder.firstStar.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_star_rate_24));
            holder.secondStar.setImageDrawable(context.getDrawable(R.drawable.ic_outline_star_rate_24));
            holder.thirdStar.setImageDrawable(context.getDrawable(R.drawable.ic_outline_star_rate_24));

        } else if (restaurant.getRating() == 2) {
            holder.firstStar.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_star_rate_24));
            holder.secondStar.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_star_rate_24));
            holder.thirdStar.setImageDrawable(context.getDrawable(R.drawable.ic_outline_star_rate_24));
        } else if (restaurant.getRating() == 3) {
            holder.firstStar.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_star_rate_24));
            holder.secondStar.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_star_rate_24));
            holder.thirdStar.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_star_rate_24));
        } else {
            holder.firstStar.setImageDrawable(context.getDrawable(R.drawable.ic_outline_star_rate_24));
            holder.secondStar.setImageDrawable(context.getDrawable(R.drawable.ic_outline_star_rate_24));
            holder.thirdStar.setImageDrawable(context.getDrawable(R.drawable.ic_outline_star_rate_24));
        }
        Glide.with(context).load(restaurant.getPicUrl()).centerCrop().into(holder.imgRestau);
        holder.itemView.setOnClickListener(v -> ((ActivityWithFrag) context).openBottomSheetDialog(restaurant, "list"));

    }

    @Override
    public int getItemCount() {
        Log.d("getItem", " " + listRestau.size());
        return listRestau.size();
    }

    public void updateRestauList(List<Restaurant> listRestaurant) {
        this.listRestau.clear();
        this.listRestau = listRestaurant;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameRestau;
        TextView adresseRestau;
        TextView distanceRestau;
        ImageView firstStar;
        ImageView secondStar;
        ImageView thirdStar;
        TextView openRestau;
        ImageView imgRestau;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameRestau = itemView.findViewById(R.id.nameRes);
            adresseRestau = itemView.findViewById(R.id.adresseRes);
            distanceRestau = itemView.findViewById(R.id.distanceRest);
            firstStar = itemView.findViewById(R.id.firstStar);
            secondStar = itemView.findViewById(R.id.secondStar);
            thirdStar = itemView.findViewById(R.id.thirdStar);
            openRestau = itemView.findViewById(R.id.openRes);
            imgRestau = itemView.findViewById(R.id.imgRestau);
        }
    }
}
