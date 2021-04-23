package com.openclassrooms.go4lunch.ui.restaurant;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.ChangeNameBinding;
import com.openclassrooms.go4lunch.databinding.ChangePictureBinding;
import com.openclassrooms.go4lunch.databinding.FragmentSettingsBinding;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.viewmodel.RestaurantViewModel;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import dagger.hilt.android.AndroidEntryPoint;

import static android.app.Activity.RESULT_OK;

@AndroidEntryPoint
public class SettingsFragment extends Fragment {

    private static final int GET_FROM_GALLERY = 1;
    private static final int GET_FROM_PICTURE = 2;
    private RestaurantViewModel restaurantViewModel;
    private FragmentSettingsBinding binding;
    private FirebaseUser firebaseUser;
    private AlertDialog dialog = null;
    private ChangeNameBinding changeNameBinding;
    private ChangePictureBinding changePictureBinding;
    private User user;
    private Uri photoURI;
    private Boolean isActivNotif;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        restaurantViewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        restaurantViewModel.getUser().observe(getViewLifecycleOwner(), user1 -> {
                    binding.editName.setText(user1.getName());
                    this.user = user1;
                }
        );

        isActivNotif = PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean("isNotifActiv", true);

        binding.activNotif.setChecked(isActivNotif);

        binding.activNotif.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
                prefs.edit()
                        .putBoolean("isNotifActiv", b)
                        .apply();
            }
        });

        binding.constraintName.setOnClickListener(view1 -> openDialog("Name"));

        binding.constraintPicture.setOnClickListener(view1 -> openDialog("Picture"));

        return view;
    }

    private void openDialog(String name) {

        if (name.equals("Name")) {
            final AlertDialog dialog = openNameDialog();
            dialog.show();
            dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAccent));
            dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAccent));
        } else {
            final AlertDialog dialog = openPicDialog();
            dialog.show();
            dialog.getButton(dialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorAccent));
            dialog.getButton(dialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorAccent));
        }
    }

    private AlertDialog openPicDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_MaterialComponents_DayNight_Dialog_Alert);
        builder.setTitle(R.string.pictureText);
        changePictureBinding = ChangePictureBinding.inflate(getLayoutInflater());
        builder.setView(changePictureBinding.getRoot());

        changePictureBinding.buttonGallery.setOnClickListener(view -> startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI), GET_FROM_GALLERY));
        changePictureBinding.buttonPic.setOnClickListener(view -> {
            try {
                openPicApp();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        dialog = builder.create();
        return dialog;
    }

    private void openPicApp() throws IOException {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {

            File photoFile = null;
            photoFile = createImageFile();

            if (photoFile != null) {

                photoURI = FileProvider.getUriForFile(getContext(),
                        "com.openclassrooms.go4lunch",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(intent, GET_FROM_PICTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + firebaseUser.getUid() + "_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_FROM_PICTURE && resultCode == RESULT_OK) {
            restaurantViewModel.changePic(photoURI);
            binding.updatePic.setText(R.string.picUpdate);

        } else if (requestCode == GET_FROM_GALLERY && resultCode == RESULT_OK && data != null) {
            String filename = "";
            Uri imageUri = data.getData();

            if (imageUri.getScheme().equals("file")) {
                filename = imageUri.getLastPathSegment();
            } else {
                Cursor cursor = null;
                try {
                    cursor = getActivity().getContentResolver().query(imageUri, new String[]{
                            MediaStore.Images.ImageColumns.DISPLAY_NAME
                    }, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        filename = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME));
                    }
                } finally {
                    {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }

                    restaurantViewModel.changePic(imageUri);
                    binding.updatePic.setText(R.string.picUpdate);
                }
            }
        }
        dialog.dismiss();
    }

    private AlertDialog openNameDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.Theme_MaterialComponents_DayNight_Dialog_Alert);
        builder.setTitle(R.string.nameText);
        changeNameBinding = ChangeNameBinding.inflate(getLayoutInflater());
        builder.setView(changeNameBinding.getRoot());
        changeNameBinding.editNameDial.setHint(user.getName());
        builder.setPositiveButton(R.string.btnValidate, null);
        builder.setNegativeButton(R.string.btnCancel, null);

        dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button posButt = this.dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            posButt.setOnClickListener(view -> {
                if (changeNameBinding.editNameDial.getText().toString().isEmpty()) {
                    changeNameBinding.textError.setText(R.string.error);
                } else {
                    restaurantViewModel.changeName(changeNameBinding.editNameDial.getText().toString());
                    dialogInterface.dismiss();
                }
            });

            Button negButt = this.dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negButt.setOnClickListener(view -> dialogInterface.dismiss());
        });

        return dialog;
    }
}

