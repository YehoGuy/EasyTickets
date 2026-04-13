package com.example.easytickets.domain.model;

import java.io.Serializable;

public class PlaceSelection implements Serializable {

    private final String placeId;
    private final String displayName;
    private final String address;
    private final double latitude;
    private final double longitude;
    private final String countryCode;
    private final String cityName;

    public PlaceSelection(
            String placeId,
            String displayName,
            String address,
            double latitude,
            double longitude,
            String countryCode,
            String cityName
    ) {
        this.placeId = placeId == null ? "" : placeId;
        this.displayName = displayName == null ? "" : displayName;
        this.address = address == null ? "" : address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.countryCode = countryCode == null ? "" : countryCode;
        this.cityName = cityName == null ? "" : cityName;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAddress() {
        return address;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCityName() {
        return cityName;
    }
}
