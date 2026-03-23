package com.example.gamelog;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        MaterialCardView gamesCard = findViewById(R.id.games_card);
        gamesCard.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, GamesActivity.class)));

        MaterialCardView settingsCard = findViewById(R.id.settings_card);
        settingsCard.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, SettingsActivity.class)));

        // Button to explore games from REST API
        MaterialCardView exploreApiCard = findViewById(R.id.explore_api_card);
        exploreApiCard.setOnClickListener(v -> startActivity(new Intent(DashboardActivity.this, ApiGamesActivity.class)));
    }
}
