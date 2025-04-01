package com.obaidi.it_487_project_3;

import com.google.gson.annotations.SerializedName;

public class QuoteRow {
    @SerializedName("row")
    private QuoteData quoteData;

    public QuoteData getQuoteData() {
        return quoteData;
    }
}