package com.example.easytickets.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventMapGroup implements Serializable {

    private final String groupId;
    private final String venueLabel;
    private final double latitude;
    private final double longitude;
    private final ArrayList<EventSummary> events;

    public EventMapGroup(
            String groupId,
            String venueLabel,
            double latitude,
            double longitude,
            ArrayList<EventSummary> events
    ) {
        this.groupId = groupId;
        this.venueLabel = venueLabel == null ? "" : venueLabel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.events = events;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getVenueLabel() {
        return venueLabel;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public List<EventSummary> getEvents() {
        return Collections.unmodifiableList(events);
    }

    public boolean isMultiple() {
        return events.size() > 1;
    }

    public EventSummary getPrimaryEvent() {
        return events.get(0);
    }

    public String getMarkerTitle() {
        return isMultiple() ? "Multiple events" : getPrimaryEvent().getName();
    }

    public String getMarkerSnippet() {
        if (isMultiple()) {
            return events.size() + " events at " + venueLabel;
        }
        return getPrimaryEvent().getVenueName();
    }
}
