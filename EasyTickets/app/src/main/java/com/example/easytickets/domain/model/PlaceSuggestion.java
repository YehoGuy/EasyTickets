package com.example.easytickets.domain.model;

import java.io.Serializable;

public class PlaceSuggestion implements Serializable {

    private final String placeId;
    private final String primaryText;
    private final String secondaryText;

    public PlaceSuggestion(String placeId, String primaryText, String secondaryText) {
        this.placeId = placeId == null ? "" : placeId;
        this.primaryText = primaryText == null ? "" : primaryText;
        this.secondaryText = secondaryText == null ? "" : secondaryText;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getPrimaryText() {
        return primaryText;
    }

    public String getSecondaryText() {
        return secondaryText;
    }
}
