package com.example.smartpantrymanager;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Button btnBackFromSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        btnBackFromSettings = findViewById(R.id.btnBackFromSettings);

        btnBackFromSettings.setOnClickListener(v -> finish());
    }
}