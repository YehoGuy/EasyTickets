package com.example.easytickets.data.places;

import android.util.Log;

import com.example.easytickets.data.RepositoryCallback;
import com.example.easytickets.domain.model.PlaceSelection;
import com.example.easytickets.domain.model.PlaceSuggestion;
import com.google.android.gms.common.api.ApiException;
import com.example.easytickets.util.AppConfig;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AddressComponent;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link PlacesRepository} implementation that handles both autocomplete and place-details
 * resolution through the Google Places SDK.
 * The file is structured around lightweight prediction queries first and selected-place
 * resolution second.
 */
public class GooglePlacesRepository implements PlacesRepository {

    private static final String TAG = "GooglePlacesRepo";
    private static final List<String> SUPPORTED_COUNTRIES = Arrays.asList("US", "CA", "MX", "AU", "NZ");
    private static final List<String> HOTEL_TYPES = Collections.singletonList("lodging");
    private static final List<String> CITY_TYPES = Collections.singletonList("(cities)");

    private final PlacesClient placesClient;
    private final AppConfig appConfig;

    public GooglePlacesRepository(PlacesClient placesClient, AppConfig appConfig) {
        this.placesClient = placesClient;
        this.appConfig = appConfig;
    }

    @Override
    public void searchHotels(String query, RepositoryCallback<List<PlaceSuggestion>> callback) {
        searchPredictions(query, HOTEL_TYPES, callback);
    }

    @Override
    public void searchCities(String query, RepositoryCallback<List<PlaceSuggestion>> callback) {
        searchPredictions(query, CITY_TYPES, callback);
    }

    @Override
    public void fetchPlaceSelection(String placeId, RepositoryCallback<PlaceSelection> callback) {
        if (!appConfig.hasGoogleMapsKey() || placesClient == null) {
            callback.onError("Google Maps and Places API key is missing.");
            return;
        }

        List<Place.Field> fields = Arrays.asList(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS_COMPONENTS
        );
        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, fields);
        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    Place place = response.getPlace();
                    LatLng latLng = place.getLatLng();
                    if (latLng == null) {
                        callback.onError("Place details are missing a location.");
                        return;
                    }
                    callback.onSuccess(new PlaceSelection(
                            place.getId(),
                            safe(place.getName()),
                            safe(place.getAddress()),
                            latLng.latitude,
                            latLng.longitude,
                            extractCountryCode(place),
                            extractCityName(place)
                    ));
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Place details lookup failed for placeId=" + placeId, exception);
                    callback.onError(buildPlacesErrorMessage(
                            "Couldn't load place details.",
                            exception
                    ));
                });
    }

    private void searchPredictions(
            String query,
            List<String> typeFilters,
            RepositoryCallback<List<PlaceSuggestion>> callback
    ) {
        if (!appConfig.hasGoogleMapsKey() || placesClient == null) {
            callback.onError("Google Maps and Places API key is missing.");
            return;
        }

        String normalizedQuery = query == null ? "" : query.trim();
        if (normalizedQuery.length() < 2) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(normalizedQuery)
                .setCountries(SUPPORTED_COUNTRIES)
                .setTypesFilter(typeFilters)
                .build();

        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    List<PlaceSuggestion> suggestions = new ArrayList<>();
                    response.getAutocompletePredictions().forEach(prediction -> suggestions.add(
                            new PlaceSuggestion(
                                    prediction.getPlaceId(),
                                    prediction.getPrimaryText(null).toString(),
                                    prediction.getSecondaryText(null).toString()
                            )
                    ));
                    callback.onSuccess(suggestions);
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Autocomplete search failed for query=" + normalizedQuery, exception);
                    callback.onError(buildPlacesErrorMessage(
                            "Couldn't search Google Places right now.",
                            exception
                    ));
                });
    }

    private String buildPlacesErrorMessage(String prefix, Exception exception) {
        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            String statusMessage = sanitizeStatusMessage(apiException.getStatusMessage());
            if (!statusMessage.isEmpty()) {
                return prefix + " " + statusMessage + " (code " + apiException.getStatusCode() + ").";
            }
            return prefix + " Google Places returned error code " + apiException.getStatusCode() + ".";
        }

        String message = exception == null ? "" : safe(exception.getMessage());
        if (!message.isEmpty()) {
            return prefix + " " + message;
        }
        return prefix;
    }

    private String sanitizeStatusMessage(String value) {
        String message = safe(value);
        if (message.endsWith(".")) {
            return message.substring(0, message.length() - 1);
        }
        return message;
    }

    private String extractCountryCode(Place place) {
        if (place.getAddressComponents() == null) {
            return "";
        }
        for (AddressComponent component : place.getAddressComponents().asList()) {
            if (component.getTypes().contains("country")) {
                return safe(component.getShortName());
            }
        }
        return "";
    }

    private String extractCityName(Place place) {
        if (place.getAddressComponents() != null) {
            for (AddressComponent component : place.getAddressComponents().asList()) {
                List<String> types = component.getTypes();
                if (types.contains("locality")
                        || types.contains("postal_town")
                        || types.contains("administrative_area_level_3")) {
                    return safe(component.getName());
                }
            }
        }
        return safe(place.getName());
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
