package com.obaidi.it_487_project_3;

import com.google.gson.annotations.SerializedName;
// import java.util.List; // Uncomment if you want to use tags later

public class QuoteData {
    @SerializedName("quote")
    private String quoteText;

    @SerializedName("author") // Add author field mapping
    private String author;

    // Uncomment if you want to process tags
    // @SerializedName("tags")
    // private List<String> tags;

    public String getQuoteText() {
        // Optional: Trim extra whitespace often found in this dataset
        return quoteText != null ? quoteText.trim() : null;
    }

    public String getAuthor() { // Add getter for author
        return author != null ? author.trim() : "غير معروف"; // Provide default if null
    }

    // public List<String> getTags() { return tags; } // Uncomment if using tags
}