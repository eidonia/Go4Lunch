package com.openclassrooms.go4lunch.ui.restaurant.workmates;

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
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.models.User;

import java.util.ArrayList;
import java.util.List;

import static com.openclassrooms.go4lunch.utils.Constante.LFRAG_ADA;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

    private final Context context;
    private final List<User> userList = new ArrayList<>();
    private final String typeFrag;

    public UserListAdapter(Context context, String typeFrag) {
        this.context = context;
        this.typeFrag = typeFrag;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.workmates_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = userList.get(position);
        Log.d("listUserTest", "iemCount " + user.getName());
        holder.nameUser.setText(user.getName());
        Glide.with(context).load(user.getPhotoUrl())
                .apply(new RequestOptions()
                        .circleCrop()
                        .format(DecodeFormat.PREFER_RGB_565)
                        .override(Target.SIZE_ORIGINAL))
                .into(holder.imgUser);
        if (typeFrag.equals(LFRAG_ADA)) {
            if (user.isRestauChoosen()) {
                holder.restauChosen.setText(user.getThisDayRestau().getName());
            } else {
                holder.restauChosen.setText("" + user.getName() + " n'a pas choisi de restaurant");
            }
        } else {
            holder.restauChosen.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }


    public void setUserList(List<User> listUser) {
        this.userList.clear();
        this.userList.addAll(listUser);
        notifyDataSetChanged();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView nameUser;
        ImageView imgUser;
        TextView restauChosen;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nameUser = itemView.findViewById(R.id.nameUser);
            imgUser = itemView.findViewById(R.id.userImg);
            restauChosen = itemView.findViewById(R.id.restauChoosen);
        }
    }
}
