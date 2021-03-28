package com.openclassrooms.go4lunch.ui.restaurant;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.DisconnectionDialogBinding;
import com.openclassrooms.go4lunch.ui.MainActivity;

import static com.openclassrooms.go4lunch.utils.Constante.LOG_OUT_EJABBERD;

public class LogOutDialFrag extends DialogFragment {

    DisconnectionDialogBinding discoBinding;
    private AlertDialog dialog = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.Theme_MaterialComponents_DayNight_Dialog_Alert);
        builder.setTitle(R.string.disconnect);
        discoBinding = DisconnectionDialogBinding.inflate(getLayoutInflater());
        builder.setView(discoBinding.getRoot());
        builder.setPositiveButton(R.string.yesDisco, (dialogInterface, i) -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent();
            intent.setAction(LOG_OUT_EJABBERD);
            getContext().sendBroadcast(intent);
            startActivity(new Intent(getContext(), MainActivity.class));
        });

        builder.setNegativeButton(R.string.noDisco, null);

        dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAccent));
            dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAccent));
        });

        return dialog;
    }
}
