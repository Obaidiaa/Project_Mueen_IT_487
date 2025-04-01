package com.obaidi.it_487_project_3;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface QuoteApiService {

    // Base URL: https://datasets-server.huggingface.co/
    // Endpoint: rows
    @GET("rows")
    Call<QuoteApiResponse> getArabicQuotes(
            @Query("dataset") String dataset, // "HeshamHaroon/arabic-quotes"
            @Query("config") String config,   // "default"
            @Query("split") String split,     // "train"
            @Query("offset") int offset,      // e.g., 0
            @Query("length") int length       // e.g., 100
    );
}