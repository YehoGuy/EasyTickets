package com.example.easytickets.data.ticketmaster;

import com.example.easytickets.domain.model.EventCategory;
import com.example.easytickets.domain.model.SearchFilters;
import com.example.easytickets.domain.model.SearchRequest;
import com.example.easytickets.util.GeoHashUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds Ticketmaster query parameters from the normalized {@link SearchRequest} model.
 * It builds nearby hotel geoPoint searches while applying category and radius filters.
 */
public class TicketmasterQueryFactory {

    public Map<String, String> buildEventSearchQuery(SearchRequest request) {
        Map<String, String> query = new LinkedHashMap<>();
        query.put("size", "50");

        SearchFilters filters = request.getSearchFilters();
        if (filters != null && !filters.getSelectedCategories().isEmpty()) {
            query.put("segmentId", joinCategoryIds(filters.getSelectedCategories()));
        }

        query.put("geoPoint", GeoHashUtils.encode(
                request.getOriginLatitude(),
                request.getOriginLongitude(),
                9
        ));
        query.put("sort", "distance,asc");
        if (!request.getCountryCode().isEmpty()) {
            query.put("countryCode", request.getCountryCode());
        }

        if (filters != null && filters.hasRadius()) {
            query.put("radius", String.valueOf(filters.getRadiusValue()));
            query.put("unit", filters.getRadiusUnit());
        }

        return query;
    }

    private String joinCategoryIds(List<EventCategory> categories) {
        StringBuilder builder = new StringBuilder();
        for (EventCategory category : categories) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(category.getId());
        }
        return builder.toString();
    }
}
