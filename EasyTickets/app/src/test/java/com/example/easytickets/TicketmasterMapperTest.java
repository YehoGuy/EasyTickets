package com.example.easytickets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.example.easytickets.data.ticketmaster.TicketmasterResponses;
import com.example.easytickets.domain.mapper.TicketmasterMapper;
import com.example.easytickets.domain.model.EventCategory;
import com.example.easytickets.domain.model.EventSummary;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link TicketmasterMapper} covering optional-field handling and
 * top-level category extraction.
 */
public class TicketmasterMapperTest {

    private final TicketmasterMapper mapper = new TicketmasterMapper();

    @Test
    public void toEventSummary_handlesMissingOptionalFields() {
        TicketmasterResponses.EventDto eventDto = new TicketmasterResponses.EventDto();
        eventDto.id = "evt-1";
        eventDto.name = "Concert Night";
        eventDto.url = "https://example.com/concert";
        eventDto.dates = new TicketmasterResponses.DatesDto();
        eventDto.dates.start = new TicketmasterResponses.StartDto();
        eventDto.dates.start.localDate = "2026-05-01";
        eventDto.embedded = new TicketmasterResponses.EventEmbeddedDto();
        eventDto.embedded.venues = Arrays.asList(buildVenue());

        EventSummary summary = mapper.toEventSummary(eventDto);

        assertNotNull(summary);
        assertEquals("Concert Night", summary.getName());
        assertEquals("Venue Name", summary.getVenueName());
        assertFalse(summary.getVenueAddress().isEmpty());
    }

    @Test
    public void toEventCategories_extractsUniqueTopLevelSegments() {
        TicketmasterResponses.ClassificationSearchResponse response = new TicketmasterResponses.ClassificationSearchResponse();
        response.embedded = new TicketmasterResponses.EmbeddedClassifications();

        TicketmasterResponses.ClassificationDto first = new TicketmasterResponses.ClassificationDto();
        first.id = "segment-music";
        first.name = "Music";

        TicketmasterResponses.ClassificationDto duplicate = new TicketmasterResponses.ClassificationDto();
        duplicate.id = "segment-music";
        duplicate.name = "Music";

        response.embedded.classifications = Arrays.asList(first, duplicate);

        List<EventCategory> categories = mapper.toEventCategories(response);

        assertEquals(1, categories.size());
        assertEquals("Music", categories.get(0).getName());
    }

    private TicketmasterResponses.VenueDto buildVenue() {
        TicketmasterResponses.VenueDto venue = new TicketmasterResponses.VenueDto();
        venue.id = "venue-1";
        venue.name = "Venue Name";
        venue.address = new TicketmasterResponses.AddressDto();
        venue.address.line1 = "123 Main Street";
        venue.city = new TicketmasterResponses.CityDto();
        venue.city.name = "New York";
        venue.country = new TicketmasterResponses.CountryDto();
        venue.country.name = "United States";
        venue.country.countryCode = "US";
        venue.location = new TicketmasterResponses.LocationDto();
        venue.location.latitude = "40.7505";
        venue.location.longitude = "-73.9934";
        return venue;
    }
}
