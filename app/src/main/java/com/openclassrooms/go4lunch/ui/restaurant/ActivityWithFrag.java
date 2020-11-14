package com.openclassrooms.go4lunch.ui.restaurant;

import android.os.Bundle;
import android.view.Gravity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.ActivityWithFragBinding;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ActivityWithFrag extends AppCompatActivity {

    private ActivityWithFragBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWithFragBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(binding.navView, navController);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navDrawer = navHostFragment.getNavController();
        NavigationView navView = findViewById(R.id.navBar);
        NavigationUI.setupWithNavController(navView, navDrawer);

    }

    public void openDrawer() {
        binding.drawerLayout.openDrawer(Gravity.LEFT);
    }

}