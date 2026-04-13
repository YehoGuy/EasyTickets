package com.example.easytickets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.example.easytickets.domain.model.EventMapGroup;
import com.example.easytickets.domain.model.EventSummary;
import com.example.easytickets.util.EventGroupingUtils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class EventGroupingUtilsTest {

    @Test
    public void groupEvents_mergesEventsWithTheSameVenueId() {
        EventSummary first = buildEvent("1", "venue-1", "Madison Square Garden", 40.7505, -73.9934);
        EventSummary second = buildEvent("2", "venue-1", "Madison Square Garden", 40.7505, -73.9934);

        List<EventMapGroup> groups = EventGroupingUtils.groupEvents(Arrays.asList(first, second));

        assertEquals(1, groups.size());
        assertEquals(2, groups.get(0).getEvents().size());
        assertTrue(groups.get(0).isMultiple());
    }

    @Test
    public void groupEvents_fallsBackToCoordinatesAndVenueName() {
        EventSummary first = buildEvent("1", "", "Riverside Hall", 32.0853, 34.7818);
        EventSummary second = buildEvent("2", "", "Riverside Hall", 32.0853001, 34.7818001);

        List<EventMapGroup> groups = EventGroupingUtils.groupEvents(Arrays.asList(first, second));

        assertEquals(1, groups.size());
        assertEquals(2, groups.get(0).getEvents().size());
    }

    private EventSummary buildEvent(
            String id,
            String venueId,
            String venueName,
            double latitude,
            double longitude
    ) {
        return new EventSummary(
                id,
                "Sample Event " + id,
                "https://example.com/events/" + id,
                "2026-04-13",
                "20:00:00",
                "2026-04-13T20:00:00Z",
                venueId,
                venueName,
                "Main Street",
                "Music",
                "",
                "Tel Aviv",
                "IL",
                latitude,
                longitude,
                ""
        );
    }
}
