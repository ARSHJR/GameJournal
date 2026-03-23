package com.example.gamelog;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Activity that fetches and displays games from a REST API using Retrofit
 */
public class ApiGamesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GameApiAdapter adapter;
    private TextView loadingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_games);

        recyclerView = findViewById(R.id.api_games_recycler_view);
        loadingText = findViewById(R.id.loading_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Start API call flow
        fetchGames();
    }

    private void fetchGames() {
        // Use Retrofit setup to get the service and make the call
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<GameApiItem>> call = apiService.getGames();

        call.enqueue(new Callback<List<GameApiItem>>() {
            @Override
            public void onResponse(Call<List<GameApiItem>> call, Response<List<GameApiItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<GameApiItem> games = response.body();
                    
                    // On API success -> set adapter and bind data to RecyclerView
                    adapter = new GameApiAdapter(games);
                    recyclerView.setAdapter(adapter);
                    
                    // Hide loading text and show recycler view
                    loadingText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(ApiGamesActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                    loadingText.setText("Error loading data");
                }
            }

            @Override
            public void onFailure(Call<List<GameApiItem>> call, Throwable t) {
                // On failure -> show Toast
                Toast.makeText(ApiGamesActivity.this, "Failed to load data: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                loadingText.setText("Failed to connect to server");
            }
        });
    }
}
