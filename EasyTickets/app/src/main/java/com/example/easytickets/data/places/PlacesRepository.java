package com.example.easytickets.data.places;

import com.example.easytickets.data.RepositoryCallback;
import com.example.easytickets.domain.model.PlaceSelection;
import com.example.easytickets.domain.model.PlaceSuggestion;

import java.util.List;

/**
 * Abstraction over place search and place-details lookup used by the hotel and city flows.
 */
public interface PlacesRepository {

    void searchHotels(String query, RepositoryCallback<List<PlaceSuggestion>> callback);

    void searchCities(String query, RepositoryCallback<List<PlaceSuggestion>> callback);

    void fetchPlaceSelection(String placeId, RepositoryCallback<PlaceSelection> callback);
}
