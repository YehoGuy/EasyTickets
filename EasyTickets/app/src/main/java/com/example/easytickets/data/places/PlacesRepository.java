package com.example.easytickets.data.places;

import com.example.easytickets.data.RepositoryCallback;
import com.example.easytickets.domain.model.PlaceSelection;
import com.example.easytickets.domain.model.PlaceSuggestion;

import java.util.List;

public interface PlacesRepository {

    void searchHotels(String query, RepositoryCallback<List<PlaceSuggestion>> callback);

    void searchCities(String query, RepositoryCallback<List<PlaceSuggestion>> callback);

    void fetchPlaceSelection(String placeId, RepositoryCallback<PlaceSelection> callback);
}
