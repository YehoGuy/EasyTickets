package com.example.easytickets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.easytickets.data.ticketmaster.TicketmasterQueryFactory;
import com.example.easytickets.domain.model.EventCategory;
import com.example.easytickets.domain.model.SearchFilters;
import com.example.easytickets.domain.model.SearchMode;
import com.example.easytickets.domain.model.SearchRequest;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Unit tests for {@link TicketmasterQueryFactory} covering nearby and city request generation.
 */
public class TicketmasterQueryFactoryTest {

    private final TicketmasterQueryFactory queryFactory = new TicketmasterQueryFactory();

    @Test
    public void buildEventSearchQuery_buildsNearbySearchForHotels() {
        SearchRequest request = new SearchRequest(
                SearchMode.HOTEL,
                "Hotel Example",
                40.7505,
                -73.9934,
                "New York",
                "US",
                new SearchFilters(selectedCategories(), 25, "miles")
        );

        Map<String, String> query = queryFactory.buildEventSearchQuery(request);

        assertEquals("50", query.get("size"));
        assertEquals("distance,asc", query.get("sort"));
        assertEquals("25", query.get("radius"));
        assertEquals("miles", query.get("unit"));
        assertEquals("US", query.get("countryCode"));
        assertTrue(query.containsKey("geoPoint"));
        assertEquals("KZFzniwnSyZfZ7v7nJ,KZFzniwnSyZfZ7v7nE", query.get("segmentId"));
    }

    @Test
    public void buildEventSearchQuery_buildsCitySearchWithoutGeoPoint() {
        SearchRequest request = new SearchRequest(
                SearchMode.CITY,
                "Tel Aviv",
                32.0853,
                34.7818,
                "Tel Aviv",
                "IL",
                new SearchFilters(selectedCategories(), 0, "")
        );

        Map<String, String> query = queryFactory.buildEventSearchQuery(request);

        assertEquals("date,asc", query.get("sort"));
        assertEquals("Tel Aviv", query.get("city"));
        assertEquals("IL", query.get("countryCode"));
        assertFalse(query.containsKey("geoPoint"));
        assertFalse(query.containsKey("radius"));
    }

    private List<EventCategory> selectedCategories() {
        return Arrays.asList(
                new EventCategory("KZFzniwnSyZfZ7v7nJ", "Music"),
                new EventCategory("KZFzniwnSyZfZ7v7nE", "Sports")
        );
    }
}
