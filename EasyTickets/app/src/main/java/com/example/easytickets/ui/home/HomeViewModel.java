package com.example.easytickets.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.easytickets.data.RepositoryCallback;
import com.example.easytickets.data.places.PlacesRepository;
import com.example.easytickets.data.ticketmaster.TicketmasterRepository;
import com.example.easytickets.domain.model.EventCategory;
import com.example.easytickets.domain.model.PlaceSelection;
import com.example.easytickets.domain.model.PlaceSuggestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shared ViewModel for the home flow.
 * It loads event categories once and brokers hotel autocomplete and place resolution.
 */
public class HomeViewModel extends ViewModel {

    private final TicketmasterRepository ticketmasterRepository;
    private final PlacesRepository placesRepository;

    private final MutableLiveData<List<EventCategory>> categories = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> categoriesLoading = new MutableLiveData<>(false);
    private final MutableLiveData<List<PlaceSuggestion>> hotelSuggestions = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> hotelSearchLoading = new MutableLiveData<>(false);
    private final MutableLiveData<PlaceSelection> selectedHotel = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hotelSelectionLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>("");

    private String latestHotelQuery = "";

    public HomeViewModel(
            TicketmasterRepository ticketmasterRepository,
            PlacesRepository placesRepository
    ) {
        this.ticketmasterRepository = ticketmasterRepository;
        this.placesRepository = placesRepository;
        loadEventCategories();
    }

    public LiveData<List<EventCategory>> getCategories() {
        return categories;
    }

    public LiveData<Boolean> getCategoriesLoading() {
        return categoriesLoading;
    }

    public LiveData<List<PlaceSuggestion>> getHotelSuggestions() {
        return hotelSuggestions;
    }

    public LiveData<Boolean> getHotelSearchLoading() {
        return hotelSearchLoading;
    }

    public LiveData<PlaceSelection> getSelectedHotel() {
        return selectedHotel;
    }

    public LiveData<Boolean> getHotelSelectionLoading() {
        return hotelSelectionLoading;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public void clearMessage() {
        message.setValue("");
    }

    public void searchHotels(String query) {
        latestHotelQuery = normalizeQuery(query);
        if (latestHotelQuery.length() < 2) {
            hotelSearchLoading.setValue(false);
            hotelSuggestions.setValue(Collections.emptyList());
            return;
        }

        hotelSearchLoading.setValue(true);
        placesRepository.searchHotels(latestHotelQuery, new RepositoryCallback<List<PlaceSuggestion>>() {
            @Override
            public void onSuccess(List<PlaceSuggestion> data) {
                if (!latestHotelQuery.equals(normalizeQuery(query))) {
                    return;
                }
                hotelSearchLoading.postValue(false);
                hotelSuggestions.postValue(data);
            }

            @Override
            public void onError(String errorMessage) {
                hotelSearchLoading.postValue(false);
                hotelSuggestions.postValue(Collections.emptyList());
                message.postValue(errorMessage);
            }
        });
    }

    public void resolveHotelSelection(PlaceSuggestion suggestion) {
        hotelSelectionLoading.setValue(true);
        placesRepository.fetchPlaceSelection(suggestion.getPlaceId(), new RepositoryCallback<PlaceSelection>() {
            @Override
            public void onSuccess(PlaceSelection data) {
                hotelSelectionLoading.postValue(false);
                selectedHotel.postValue(data);
                hotelSuggestions.postValue(Collections.emptyList());
            }

            @Override
            public void onError(String errorMessage) {
                hotelSelectionLoading.postValue(false);
                message.postValue(errorMessage);
            }
        });
    }

    public void clearHotelSelection() {
        selectedHotel.setValue(null);
    }

    public void clearHotelSuggestions() {
        hotelSuggestions.setValue(Collections.emptyList());
    }

    private void loadEventCategories() {
        categoriesLoading.setValue(true);
        ticketmasterRepository.fetchEventCategories(new RepositoryCallback<List<EventCategory>>() {
            @Override
            public void onSuccess(List<EventCategory> data) {
                categoriesLoading.postValue(false);
                categories.postValue(data);
            }

            @Override
            public void onError(String errorMessage) {
                categoriesLoading.postValue(false);
                categories.postValue(ticketmasterRepository.getFallbackCategories());
                message.postValue(errorMessage);
            }
        });
    }

    private String normalizeQuery(String query) {
        return query == null ? "" : query.trim();
    }
}
