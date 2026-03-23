package com.example.gamelog;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Singleton Retrofit Client configuration
 */
public class RetrofitClient {
    // 10.0.2.2 is the special IP for Android Emulator to access localhost on the host machine
    // If using a physical device, replace this with your computer's local IP address (e.g., 192.168.x.x)
    private static final String BASE_URL = "http://10.0.2.2:5000/";
    private static Retrofit retrofit = null;

    public static ApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}
