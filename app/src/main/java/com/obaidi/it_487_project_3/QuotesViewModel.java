package com.obaidi.it_487_project_3;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.Random;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import java.util.ArrayList; // Import ArrayList
import java.util.Arrays; // Import Arrays
import java.util.HashSet; // Import HashSet
import java.util.List;
import java.util.Random;
import java.util.Set; // Import Set

public class QuotesViewModel extends ViewModel {

    private static final String TAG = "QuotesViewModel"; // For logging
    private static final String DATASET = "HeshamHaroon/arabic-quotes";
    private static final String CONFIG = "default";
    private static final String SPLIT = "train";
    private static final int FETCH_LENGTH = 100; // How many quotes to fetch at once


    // --- Author Exclusion List ---
    // Add the exact author names you want to exclude here
    private static final Set<String> EXCLUDED_AUTHORS = new HashSet<>(Arrays.asList(
            "صدام حسين"
            // Add more authors exactly as they appear in the data
    ));
    // -----------------------------

    private final MutableLiveData<String> _currentQuote = new MutableLiveData<>();
    public LiveData<String> currentQuote = _currentQuote;

    private final MutableLiveData<String> _currentAuthor = new MutableLiveData<>(); // LiveData for Author
    public LiveData<String> currentAuthor = _currentAuthor; // Public LiveData

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private QuoteApiService apiService;
    private Random random = new Random();
    private List<QuoteRow> cachedQuotes = null; // Cache *filtered* quotes

    public QuotesViewModel() {
        apiService = RetrofitClient.getClient().create(QuoteApiService.class);
        fetchQuotesIfNeeded();
    }

    public void fetchNewQuote() {
        if (cachedQuotes != null && !cachedQuotes.isEmpty()) {
            displayRandomQuoteFromCache();
        } else {
            fetchQuotesFromApi();
        }
    }

    private void fetchQuotesIfNeeded() {
        if (_currentQuote.getValue() == null) {
            fetchQuotesFromApi();
        }
    }

    private void fetchQuotesFromApi() {
        _isLoading.setValue(true);
        _errorMessage.setValue(null);

        Call<QuoteApiResponse> call = apiService.getArabicQuotes(DATASET, CONFIG, SPLIT, 0, FETCH_LENGTH);

        call.enqueue(new Callback<QuoteApiResponse>() {
            @Override
            public void onResponse(Call<QuoteApiResponse> call, Response<QuoteApiResponse> response) {
                _isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null && response.body().getRows() != null) {
                    // --- Filter the results ---
                    List<QuoteRow> fetchedRows = response.body().getRows();
                    List<QuoteRow> filteredList = new ArrayList<>();
                    for (QuoteRow row : fetchedRows) {
                        if (row != null && row.getQuoteData() != null && row.getQuoteData().getAuthor() != null) {
                            String author = row.getQuoteData().getAuthor().trim(); // Trim whitespace
                            if (!EXCLUDED_AUTHORS.contains(author)) {
                                filteredList.add(row); // Add if author is NOT excluded
                            }
                        }
                    }
                    // --- End Filtering ---

                    cachedQuotes = filteredList; // Update cache with FILTERED list

                    if (!cachedQuotes.isEmpty()) {
                        displayRandomQuoteFromCache();
                    } else {
                        Log.w(TAG, "No usable quotes found after filtering excluded authors.");
                        _errorMessage.setValue("No quotes found after filtering.");
                        _currentQuote.setValue("لا توجد اقتباسات مناسبة."); // Update message
                        _currentAuthor.setValue("");
                        // Optional: Automatically try fetching again with a different offset? (More complex)
                    }
                } else {
                    // ... (handle API error response as before) ...
                    Log.e(TAG, "API Error Response: " + response.code() + " - " + response.message());
                    _errorMessage.setValue("Failed to fetch quotes. Code: " + response.code());
                    _currentQuote.setValue("خطأ في تحميل الاقتباس.");
                    _currentAuthor.setValue("");
                }
            }

            @Override
            public void onFailure(Call<QuoteApiResponse> call, Throwable t) {
                // ... (handle API failure as before) ...
                _isLoading.setValue(false);
                Log.e(TAG, "API Failure: " + t.getMessage(), t);
                _errorMessage.setValue("Network error: " + t.getMessage());
                _currentQuote.setValue("خطأ في الشبكة.");
                _currentAuthor.setValue("");
            }
        });
    }

    /**
     * Displays a random quote from the cache.
     * @return true if a quote was successfully displayed, false otherwise.
     */
    private boolean displayRandomQuoteFromCache() {
        if (cachedQuotes == null || cachedQuotes.isEmpty()) {
            return false; // Indicate cache is empty or failed
        }

        int randomIndex = random.nextInt(cachedQuotes.size());
        QuoteRow randomRow = cachedQuotes.get(randomIndex);
        QuoteData data = (randomRow != null) ? randomRow.getQuoteData() : null;

        if (data != null) {
            _currentQuote.setValue(data.getQuoteText() != null ? data.getQuoteText() : "اقتباس غير متوفر.");
            _currentAuthor.setValue(data.getAuthor() != null ? "- " + data.getAuthor() : "");
            return true; // Success
        } else {
            Log.w(TAG, "Found null data in cached quote row at index: " + randomIndex);
            _currentQuote.setValue("اقتباس غير متوفر.");
            _currentAuthor.setValue("");
            return false; // Indicate failure
        }
    }
}