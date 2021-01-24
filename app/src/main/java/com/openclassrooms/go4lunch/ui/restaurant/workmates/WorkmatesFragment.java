package com.openclassrooms.go4lunch.ui.restaurant.workmates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.FragmentWorkatesBinding;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.ui.restaurant.ActivityWithFrag;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WorkmatesFragment extends Fragment {

    @Inject
    public DatabaseReference users;
    private FragmentWorkatesBinding binding;
    private UserFirebase userFirebase;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkatesBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        binding.toolbar.setNavigationOnClickListener(v -> {
            ((ActivityWithFrag)getActivity()).openDrawer();
        });

        binding.listWorkmates.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        FirebaseRecyclerOptions<User> options = new FirebaseRecyclerOptions.Builder<User>()
                .setQuery(users, User.class)
                .build();

        userFirebase = new UserFirebase(options, getContext());
        binding.listWorkmates.setAdapter(userFirebase);
        userFirebase.startListening();



        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}