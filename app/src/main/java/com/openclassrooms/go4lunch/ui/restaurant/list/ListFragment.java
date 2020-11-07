package com.openclassrooms.go4lunch.ui.restaurant.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.FragmentListBinding;

public class ListFragment extends Fragment {

    private FragmentListBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        return view;
    }
}