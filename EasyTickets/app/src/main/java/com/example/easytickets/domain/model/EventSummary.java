package com.example.easytickets.domain.model;

import java.io.Serializable;

public class EventSummary implements Serializable {

    private final String id;
    private final String name;
    private final String eventUrl;
    private final String localDate;
    private final String localTime;
    private final String dateTime;
    private final String venueId;
    private final String venueName;
    private final String venueAddress;
    private final String categoryName;
    private final String imageUrl;
    private final String cityName;
    private final String countryCode;
    private final double latitude;
    private final double longitude;
    private final String distance;

    public EventSummary(
            String id,
            String name,
            String eventUrl,
            String localDate,
            String localTime,
            String dateTime,
            String venueId,
            String venueName,
            String venueAddress,
            String categoryName,
            String imageUrl,
            String cityName,
            String countryCode,
            double latitude,
            double longitude,
            String distance
    ) {
        this.id = emptyIfNull(id);
        this.name = emptyIfNull(name);
        this.eventUrl = emptyIfNull(eventUrl);
        this.localDate = emptyIfNull(localDate);
        this.localTime = emptyIfNull(localTime);
        this.dateTime = emptyIfNull(dateTime);
        this.venueId = emptyIfNull(venueId);
        this.venueName = emptyIfNull(venueName);
        this.venueAddress = emptyIfNull(venueAddress);
        this.categoryName = emptyIfNull(categoryName);
        this.imageUrl = emptyIfNull(imageUrl);
        this.cityName = emptyIfNull(cityName);
        this.countryCode = emptyIfNull(countryCode);
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = emptyIfNull(distance);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEventUrl() {
        return eventUrl;
    }

    public String getLocalDate() {
        return localDate;
    }

    public String getLocalTime() {
        return localTime;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getVenueId() {
        return venueId;
    }

    public String getVenueName() {
        return venueName;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDistance() {
        return distance;
    }

    public boolean hasCoordinates() {
        return !(latitude == 0D && longitude == 0D);
    }

    public String getDisplayDateTime() {
        if (!localDate.isEmpty() && !localTime.isEmpty()) {
            return localDate + " - " + localTime;
        }
        if (!localDate.isEmpty()) {
            return localDate;
        }
        return dateTime;
    }

    public EventDetails toEventDetails() {
        return new EventDetails(this);
    }

    private static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }
}
