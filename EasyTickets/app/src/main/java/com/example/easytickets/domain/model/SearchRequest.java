package com.example.easytickets.domain.model;

import java.io.Serializable;

/**
 * Normalized search payload passed from the home flow to the results flow.
 * It captures the chosen search mode, origin information, market context, and any filters.
 */
public class SearchRequest implements Serializable {

    private final SearchMode searchMode;
    private final String originLabel;
    private final double originLatitude;
    private final double originLongitude;
    private final String cityName;
    private final String countryCode;
    private final SearchFilters searchFilters;

    public SearchRequest(
            SearchMode searchMode,
            String originLabel,
            double originLatitude,
            double originLongitude,
            String cityName,
            String countryCode,
            SearchFilters searchFilters
    ) {
        this.searchMode = searchMode;
        this.originLabel = originLabel == null ? "" : originLabel;
        this.originLatitude = originLatitude;
        this.originLongitude = originLongitude;
        this.cityName = cityName == null ? "" : cityName;
        this.countryCode = countryCode == null ? "" : countryCode;
        this.searchFilters = searchFilters;
    }

    public SearchMode getSearchMode() {
        return searchMode;
    }

    public String getOriginLabel() {
        return originLabel;
    }

    public double getOriginLatitude() {
        return originLatitude;
    }

    public double getOriginLongitude() {
        return originLongitude;
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public SearchFilters getSearchFilters() {
        return searchFilters;
    }
}
