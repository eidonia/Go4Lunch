package com.openclassrooms.go4lunch.ui.restaurant.list;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.ui.restaurant.ActivityWithFrag;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
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
        holder.adressRestau.setText(restaurant.getVicinity());
        holder.distanceRestau.setText(context.getString(R.string.distanceRestau, restaurant.getDistance()));
        holder.pplEat.setText("(" + restaurant.getListUser().size() + ")");
        if (restaurant.getRating() == 1) {
            holder.firstStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_star_rate_24));
            holder.secondStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_rate_24));
            holder.thirdStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_rate_24));

        } else if (restaurant.getRating() == 2) {
            holder.firstStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_star_rate_24));
            holder.secondStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_star_rate_24));
            holder.thirdStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_rate_24));
        } else if (restaurant.getRating() == 3) {
            holder.firstStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_star_rate_24));
            holder.secondStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_star_rate_24));
            holder.thirdStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_baseline_star_rate_24));
        } else {
            holder.firstStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_rate_24));
            holder.secondStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_rate_24));
            holder.thirdStar.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_outline_star_rate_24));
        }
        Glide.with(context).load(restaurant.getPicUrl()).centerCrop().into(holder.imgRestau);
        holder.itemView.setOnClickListener(v -> {
            try {
                ((ActivityWithFrag) context).openBottomSheetDialog(restaurant, "list", null);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        Log.d("nullRestau", "error : " + day);
        HashMap<String, String> mapHourRestau = new HashMap<>();
        try {
            mapHourRestau = restaurant.setChips(day);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String openCLose = mapHourRestau.get("isOpen");

        if (openCLose.equals("true")) {
            String hour = mapHourRestau.get("hour");
            holder.openRestau.setText(context.getString(R.string.openUntil, hour));
        } else if (openCLose.equals("false")) {
            String hour = mapHourRestau.get("hour");
            holder.openRestau.setText(context.getString(R.string.close, hour));
        } else {
            holder.openRestau.setText(R.string.closeToday);
        }

    }

    @Override
    public int getItemCount() {
        return listRestau.size();
    }

    public void updateRestauList(List<Restaurant> listRestaurant) {
        this.listRestau.clear();
        this.listRestau = listRestaurant;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameRestau;
        TextView adressRestau;
        TextView distanceRestau;
        ImageView firstStar;
        ImageView secondStar;
        ImageView thirdStar;
        TextView openRestau;
        ImageView imgRestau;
        TextView pplEat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameRestau = itemView.findViewById(R.id.nameRes);
            adressRestau = itemView.findViewById(R.id.adresseRes);
            distanceRestau = itemView.findViewById(R.id.distanceRest);
            firstStar = itemView.findViewById(R.id.firstStar);
            secondStar = itemView.findViewById(R.id.secondStar);
            thirdStar = itemView.findViewById(R.id.thirdStar);
            openRestau = itemView.findViewById(R.id.openRes);
            imgRestau = itemView.findViewById(R.id.imgRestau);
            pplEat = itemView.findViewById(R.id.pplEatText);
        }
    }
}
