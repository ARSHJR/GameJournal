package com.example.gamelog;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;

/**
 * Interface defining the API endpoints
 */
public interface ApiService {
    @GET("games")
    Call<List<GameApiItem>> getGames();
}
