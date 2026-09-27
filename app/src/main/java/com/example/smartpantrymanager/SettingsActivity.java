package com.example.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Button btnBackFromSettings;
    private Switch switchExpiryReminders;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnBackFromSettings = findViewById(R.id.btnBackFromSettings);
        switchExpiryReminders = findViewById(R.id.switchExpiryReminders);

        btnBackFromSettings.setOnClickListener(v -> finish());

        preferences = getSharedPreferences(
                "SmartPantrySettings",
                MODE_PRIVATE
        );

        boolean expiryRemindersEnabled = preferences.getBoolean(
                "expiry_reminders",
                true
        );

        switchExpiryReminders.setChecked(expiryRemindersEnabled);

        updateSwitchText(expiryRemindersEnabled);

        switchExpiryReminders.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {

                    preferences.edit()
                            .putBoolean("expiry_reminders", isChecked)
                            .apply();

                    updateSwitchText(isChecked);
                }
        );
    }

    private void updateSwitchText(boolean isEnabled) {

        if (isEnabled) {
            switchExpiryReminders.setText("ON");
        } else {
            switchExpiryReminders.setText("OFF");
        }
    }
}