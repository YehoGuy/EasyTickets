package com.example.easytickets;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.easytickets.databinding.ActivityMainBinding;
import com.example.easytickets.databinding.ActivitySetupRequiredBinding;
import com.example.easytickets.di.AppContainer;

/**
 * Single activity host for the app.
 * It enables edge-to-edge rendering, validates local API key configuration,
 * and either shows the setup-required screen or the fragment navigation host.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        AppContainer appContainer = ((EasyTicketsApplication) getApplication()).getAppContainer();
        if (!appContainer.getAppConfig().hasRequiredKeys()) {
            ActivitySetupRequiredBinding binding = ActivitySetupRequiredBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            applyWindowInsets(binding.main);
            binding.setupMessage.setText(appContainer.getAppConfig().buildSetupInstructions());
            return;
        }

        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        applyWindowInsets(binding.main);
    }

    private void applyWindowInsets(View root) {
        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
