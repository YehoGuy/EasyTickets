package com.example.easytickets.domain.mapper;

import com.example.easytickets.data.ticketmaster.TicketmasterResponses;
import com.example.easytickets.domain.model.EventCategory;
import com.example.easytickets.domain.model.EventSummary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Maps raw Ticketmaster DTOs into stable domain models consumed by the UI.
 * It normalizes missing values, extracts top-level categories, and flattens venue and image data.
 */
public class TicketmasterMapper {

    public List<EventCategory> toEventCategories(TicketmasterResponses.ClassificationSearchResponse response) {
        Map<String, EventCategory> categories = new LinkedHashMap<>();
        if (response == null || response.embedded == null || response.embedded.classifications == null) {
            return new ArrayList<>();
        }

        for (TicketmasterResponses.ClassificationDto classification : response.embedded.classifications) {
            if (classification == null) {
                continue;
            }

            String id = safeValue(classification.id);
            String name = safeValue(classification.name);

            if (classification.segment != null) {
                id = safeValue(classification.segment.id);
                name = safeValue(classification.segment.name);
            }

            if (id.isEmpty() || name.isEmpty() || "Undefined".equalsIgnoreCase(name)) {
                continue;
            }

            if (!categories.containsKey(id)) {
                categories.put(id, new EventCategory(id, name));
            }
        }

        return new ArrayList<>(categories.values());
    }

    public List<EventSummary> toEventSummaries(TicketmasterResponses.EventSearchResponse response) {
        List<EventSummary> events = new ArrayList<>();
        if (response == null || response.embedded == null || response.embedded.events == null) {
            return events;
        }

        for (TicketmasterResponses.EventDto eventDto : response.embedded.events) {
            EventSummary event = toEventSummary(eventDto);
            if (event != null) {
                events.add(event);
            }
        }
        return events;
    }

    public EventSummary toEventSummary(TicketmasterResponses.EventDto eventDto) {
        if (eventDto == null) {
            return null;
        }

        TicketmasterResponses.VenueDto venue = null;
        if (eventDto.embedded != null && eventDto.embedded.venues != null && !eventDto.embedded.venues.isEmpty()) {
            venue = eventDto.embedded.venues.get(0);
        }

        double latitude = 0D;
        double longitude = 0D;
        if (venue != null && venue.location != null) {
            latitude = parseDouble(venue.location.latitude);
            longitude = parseDouble(venue.location.longitude);
        }

        String localDate = "";
        String localTime = "";
        String dateTime = "";
        if (eventDto.dates != null && eventDto.dates.start != null) {
            localDate = safeValue(eventDto.dates.start.localDate);
            localTime = safeValue(eventDto.dates.start.localTime);
            dateTime = safeValue(eventDto.dates.start.dateTime);
        }

        String category = "";
        if (eventDto.classifications != null && !eventDto.classifications.isEmpty()) {
            TicketmasterResponses.ClassificationDto classification = eventDto.classifications.get(0);
            if (classification.segment != null) {
                category = safeValue(classification.segment.name);
            }
            if (category.isEmpty()) {
                category = safeValue(classification.name);
            }
        }

        return new EventSummary(
                safeValue(eventDto.id),
                safeValue(eventDto.name),
                safeValue(eventDto.url),
                localDate,
                localTime,
                dateTime,
                venue == null ? "" : safeValue(venue.id),
                venue == null ? "" : safeValue(venue.name),
                buildVenueAddress(venue),
                category,
                selectImage(eventDto.images),
                venue != null && venue.city != null ? safeValue(venue.city.name) : "",
                venue != null && venue.country != null ? safeValue(venue.country.countryCode) : "",
                latitude,
                longitude,
                safeValue(eventDto.distance)
        );
    }

    private String buildVenueAddress(TicketmasterResponses.VenueDto venue) {
        if (venue == null) {
            return "";
        }

        List<String> parts = new ArrayList<>();
        if (venue.address != null && safeValue(venue.address.line1).length() > 0) {
            parts.add(venue.address.line1);
        }
        if (venue.city != null && safeValue(venue.city.name).length() > 0) {
            parts.add(venue.city.name);
        }
        if (venue.state != null && safeValue(venue.state.name).length() > 0) {
            parts.add(venue.state.name);
        }
        if (venue.country != null && safeValue(venue.country.name).length() > 0) {
            parts.add(venue.country.name);
        }
        return joinWithComma(parts);
    }

    private String selectImage(List<TicketmasterResponses.ImageDto> images) {
        if (images == null || images.isEmpty()) {
            return "";
        }

        TicketmasterResponses.ImageDto selected = images.get(0);
        for (TicketmasterResponses.ImageDto image : images) {
            if (image == null || image.url == null) {
                continue;
            }
            if ("16_9".equalsIgnoreCase(image.ratio)) {
                selected = image;
                break;
            }
        }
        return safeValue(selected.url);
    }

    private String joinWithComma(List<String> parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(part);
        }
        return builder.toString();
    }

    private double parseDouble(String value) {
        try {
            return value == null ? 0D : Double.parseDouble(value);
        } catch (NumberFormatException ignored) {
            return 0D;
        }
    }

    private String safeValue(String value) {
        return value == null ? "" : value.trim();
    }
}
