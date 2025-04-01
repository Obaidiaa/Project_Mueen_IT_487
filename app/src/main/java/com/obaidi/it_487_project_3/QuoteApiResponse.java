package com.obaidi.it_487_project_3;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuoteApiResponse {
    @SerializedName("rows")
    private List<QuoteRow> rows;

    public List<QuoteRow> getRows() {
        return rows;
    }
}