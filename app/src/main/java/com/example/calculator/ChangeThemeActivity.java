package com.example.calculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.calculator.databinding.ActivityChangeThemeBinding;

public class ChangeThemeActivity extends AppCompatActivity {

    // ViewBinding object for accessing views in activity_change_theme.xml
    ActivityChangeThemeBinding switchBinding;

    // SharedPreferences to store theme preference (dark/light mode)
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // ✅ Load saved theme preference before inflating layout
        sharedPreferences = this.getSharedPreferences("com.example.calculator", Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("switch", false);

        // Apply saved theme
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES); // Dark mode
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);  // Light mode
        }

        super.onCreate(savedInstanceState); // Always call after applying theme

        // ✅ Inflate layout using ViewBinding
        switchBinding = ActivityChangeThemeBinding.inflate(getLayoutInflater());
        setContentView(switchBinding.getRoot());

        // Back button in toolbar → finishes activity
        switchBinding.toolbar2.setNavigationOnClickListener(v -> finish());

        // ✅ Switch toggle listener for theme change
        switchBinding.myswitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();

            if (isChecked) {
                // Switch ON → enable dark mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                editor.putBoolean("switch", true);
            } else {
                // Switch OFF → enable light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                editor.putBoolean("switch", false);
            }
            editor.apply(); // Save preference
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ Ensure switch reflects saved theme when coming back to this screen
        switchBinding.myswitch.setChecked(sharedPreferences.getBoolean("switch", false));
    }
}
