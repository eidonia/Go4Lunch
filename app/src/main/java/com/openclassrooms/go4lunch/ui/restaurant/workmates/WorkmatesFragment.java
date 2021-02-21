package com.openclassrooms.go4lunch.ui.restaurant.workmates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.openclassrooms.go4lunch.databinding.FragmentWorkatesBinding;
import com.openclassrooms.go4lunch.ui.restaurant.ActivityWithFrag;
import com.openclassrooms.go4lunch.viewmodel.RestaurantViewModel;

import dagger.hilt.android.AndroidEntryPoint;

import static com.openclassrooms.go4lunch.utils.Constante.LFRAG_ADA;

@AndroidEntryPoint
public class WorkmatesFragment extends Fragment {

    private FragmentWorkatesBinding binding;
    private RestaurantViewModel restaurantViewModel;
    private UserListAdapter adapter;
    private FirebaseUser firebaseUser;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkatesBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        restaurantViewModel = new ViewModelProvider(this.getActivity()).get(RestaurantViewModel.class);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        binding.toolbar.setNavigationOnClickListener(v -> {
            ((ActivityWithFrag)getActivity()).openDrawer();
        });

        binding.listWorkmates.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        restaurantViewModel.setUid(firebaseUser.getUid());

        restaurantViewModel.getUserList().observe(getViewLifecycleOwner(), users1 -> {
            adapter = new UserListAdapter(getContext(), LFRAG_ADA);
            adapter.setUserList(users1);
            binding.listWorkmates.setAdapter(adapter);
        });


        return view;
    }

}