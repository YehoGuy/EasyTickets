package com.example.easytickets.util;

import com.example.easytickets.domain.model.EventMapGroup;
import com.example.easytickets.domain.model.EventSummary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class EventGroupingUtils {

    private EventGroupingUtils() {
    }

    public static List<EventMapGroup> groupEvents(List<EventSummary> events) {
        Map<String, List<EventSummary>> groupedEvents = new LinkedHashMap<>();
        for (EventSummary event : events) {
            if (event == null || !event.hasCoordinates()) {
                continue;
            }
            String key = buildGroupKey(event);
            List<EventSummary> bucket = groupedEvents.get(key);
            if (bucket == null) {
                bucket = new ArrayList<>();
                groupedEvents.put(key, bucket);
            }
            bucket.add(event);
        }

        List<EventMapGroup> groups = new ArrayList<>();
        for (Map.Entry<String, List<EventSummary>> entry : groupedEvents.entrySet()) {
            List<EventSummary> bucket = entry.getValue();
            EventSummary anchor = bucket.get(0);
            groups.add(new EventMapGroup(
                    entry.getKey(),
                    anchor.getVenueName(),
                    anchor.getLatitude(),
                    anchor.getLongitude(),
                    new ArrayList<>(bucket)
            ));
        }
        return groups;
    }

    public static String buildGroupKey(EventSummary event) {
        if (event.getVenueId() != null && !event.getVenueId().isEmpty()) {
            return "venue:" + event.getVenueId();
        }
        return String.format(
                Locale.US,
                "coord:%s:%s:%s",
                roundCoordinate(event.getLatitude()),
                roundCoordinate(event.getLongitude()),
                normalize(event.getVenueName())
        );
    }

    private static String roundCoordinate(double value) {
        return String.format(Locale.US, "%.5f", value);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.US);
    }
}
