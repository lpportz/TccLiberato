package com.example.ovep.fragments;

import android.view.View;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.example.ovep.R;

public class NavBarBottonFragment {

    public static void setupNavigation(final FragmentActivity activity) {
        ImageButton btnHome = activity.findViewById(R.id.btn_home);
        ImageButton btnProfile = activity.findViewById(R.id.btn_profile);

        // FragmentManager para trocar os fragments
        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        // Fragmento inicial
        loadFragment(new HomeFragment(), fragmentManager);

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new HomeFragment(), fragmentManager);
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new UserFragment(), fragmentManager);
            }
        });
    }

    private static void loadFragment(Fragment fragment, FragmentManager fragmentManager) {
        fragmentManager.beginTransaction()
                .replace(R.id.frame_container, fragment)
                .commit();
    }
}
