package com.example.easytickets.data.ticketmaster;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Namespace for the minimal set of Ticketmaster response DTOs used by the app.
 * The file is organized into event-search DTOs first and classification DTOs second.
 */
public final class TicketmasterResponses {

    private TicketmasterResponses() {
    }

    public static class EventSearchResponse {
        @SerializedName("_embedded")
        public EmbeddedEvents embedded;
    }

    public static class EmbeddedEvents {
        public List<EventDto> events;
    }

    public static class EventDto {
        public String id;
        public String name;
        public String url;
        public String distance;
        public DatesDto dates;
        public List<ImageDto> images;
        public List<ClassificationDto> classifications;
        @SerializedName("_embedded")
        public EventEmbeddedDto embedded;
    }

    public static class EventEmbeddedDto {
        public List<VenueDto> venues;
    }

    public static class DatesDto {
        public StartDto start;
    }

    public static class StartDto {
        public String localDate;
        public String localTime;
        public String dateTime;
    }

    public static class ImageDto {
        public String ratio;
        public String url;
    }

    public static class VenueDto {
        public String id;
        public String name;
        public AddressDto address;
        public CityDto city;
        public StateDto state;
        public CountryDto country;
        public LocationDto location;
    }

    public static class AddressDto {
        public String line1;
    }

    public static class CityDto {
        public String name;
    }

    public static class StateDto {
        public String name;
    }

    public static class CountryDto {
        public String name;
        public String countryCode;
    }

    public static class LocationDto {
        public String latitude;
        public String longitude;
    }

    public static class ClassificationSearchResponse {
        @SerializedName("_embedded")
        public EmbeddedClassifications embedded;
    }

    public static class EmbeddedClassifications {
        public List<ClassificationDto> classifications;
    }

    public static class ClassificationDto {
        public String id;
        public String name;
        public SegmentDto segment;
    }

    public static class SegmentDto {
        public String id;
        public String name;
    }
}
