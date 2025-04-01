package com.obaidi.it_487_project_3;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;

public class QuotesFragment extends Fragment {

    private TextView textViewQuote;
    private TextView textViewAuthor; // Add TextView for author
    private ProgressBar progressBar;
    private MaterialButton refreshButton;
    private QuotesViewModel quotesViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quotes, container, false);

        textViewQuote = view.findViewById(R.id.text_view_quote);
        textViewAuthor = view.findViewById(R.id.text_view_author); // Find author TextView
        progressBar = view.findViewById(R.id.progress_bar_quote);
        refreshButton = view.findViewById(R.id.button_refresh_quote);

        quotesViewModel = new ViewModelProvider(this).get(QuotesViewModel.class);

        // Observe LiveData
        quotesViewModel.currentQuote.observe(getViewLifecycleOwner(), quote -> {
            textViewQuote.setText(quote != null ? quote : getString(R.string.default_quote_placeholder));
            textViewQuote.setVisibility(View.VISIBLE);
        });

        // Observe Author LiveData
        quotesViewModel.currentAuthor.observe(getViewLifecycleOwner(), author -> {
            textViewAuthor.setText(author != null ? author : ""); // Set author text
            // Show/hide based on whether author is empty, only if not loading
            if (!Boolean.TRUE.equals(quotesViewModel.isLoading.getValue())) {
                textViewAuthor.setVisibility(author != null && !author.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });

        quotesViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                textViewQuote.setVisibility(View.INVISIBLE);
                textViewAuthor.setVisibility(View.INVISIBLE); // Hide author while loading
                refreshButton.setEnabled(false);
            } else {
                progressBar.setVisibility(View.GONE);
                textViewQuote.setVisibility(View.VISIBLE);
                // Author visibility is handled by its own observer now based on content
                String currentAuthor = quotesViewModel.currentAuthor.getValue();
                textViewAuthor.setVisibility(currentAuthor != null && !currentAuthor.isEmpty() ? View.VISIBLE : View.GONE);
                refreshButton.setEnabled(true);
            }
        });

        quotesViewModel.errorMessage.observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.GONE);
                refreshButton.setEnabled(true);
                textViewQuote.setVisibility(View.VISIBLE); // Show default error quote
                textViewAuthor.setVisibility(View.GONE);   // Hide author on error
            }
        });

        refreshButton.setOnClickListener(v -> quotesViewModel.fetchNewQuote());

        return view;
    }
}