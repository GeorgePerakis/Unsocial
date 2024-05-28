package com.example.unsocial;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.unsocial.databinding.ActivityHomeScreenBinding;

public class Home_Screen extends AppCompatActivity {

    ActivityHomeScreenBinding binding;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        replace(new Feed());

        binding = ActivityHomeScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigationView.setOnNavigationItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();

            if (isCurrentFragment(itemId)) {
                return false;
            }

            if (itemId == R.id.Feed) {
                replace(new Feed());
            } else if (itemId == R.id.Profile) {
                replace(new Profile());
            } else if (itemId == R.id.Settings) {
                replace(new Settings());
            }
            return true;
        });
    }

    private void replace(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private boolean isCurrentFragment(int itemId) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout);
        if (itemId == R.id.Feed) {
            return currentFragment instanceof Feed;
        } else if (itemId == R.id.Profile) {
            return currentFragment instanceof Profile;
        } else if (itemId == R.id.Settings) {
            return currentFragment instanceof Settings;
        } else {
            return false;
        }
    }
}