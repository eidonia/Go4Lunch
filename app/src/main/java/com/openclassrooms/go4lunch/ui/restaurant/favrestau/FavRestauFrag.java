package com.openclassrooms.go4lunch.ui.restaurant.favrestau;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.openclassrooms.go4lunch.databinding.FragmentFavRestauBinding;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.ui.restaurant.list.ListRestAdapter;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FavRestauFrag extends Fragment {

    @Inject
    @Named("users")
    public DatabaseReference refUsers;
    private FirebaseUser firebaseUser;
    private FragmentFavRestauBinding binding;
    private ListRestAdapter restAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentFavRestauBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        restAdapter = new ListRestAdapter(getContext());
        binding.listRestau.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.listRestau.setAdapter(restAdapter);

        Query query = refUsers.child(firebaseUser.getUid());

        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                restAdapter.updateRestauList(user.getFavRestau());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return view;
    }
}