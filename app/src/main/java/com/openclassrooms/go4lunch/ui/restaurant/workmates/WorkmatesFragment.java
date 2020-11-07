package com.openclassrooms.go4lunch.ui.restaurant.workmates;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.FragmentWorkatesBinding;

public class WorkmatesFragment extends Fragment {

    private FragmentWorkatesBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentWorkatesBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        return view;
    }
}