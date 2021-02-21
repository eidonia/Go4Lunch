package com.openclassrooms.go4lunch.ui.restaurant;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.DisconnectionDialogBinding;
import com.openclassrooms.go4lunch.databinding.FragmentDisconnectBinding;
import com.openclassrooms.go4lunch.ui.MainActivity;

public class DisconnectFragment extends Fragment {

    private FragmentDisconnectBinding binding;
    private DisconnectionDialogBinding discoBinding;
    private AlertDialog dialog = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDisconnectBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        openDisconnectDial();

        return view;
    }

    private void openDisconnectDial() {
        final AlertDialog dialog = DisconnectDialog();
        dialog.show();
        dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAccent));
        dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAccent));
    }

    private AlertDialog DisconnectDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_MaterialComponents_DayNight_Dialog_Alert);
        builder.setTitle(R.string.disconnect);
        discoBinding = DisconnectionDialogBinding.inflate(getLayoutInflater());
        builder.setView(discoBinding.getRoot());
        builder.setPositiveButton(R.string.yesDisco, (dialogInterface, i) -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getContext(), MainActivity.class));
        });

        builder.setNegativeButton(R.string.noDisco, null);

        dialog = builder.create();
        return dialog;
    }
}