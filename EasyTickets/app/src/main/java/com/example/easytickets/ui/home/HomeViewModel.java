package com.example.easytickets.ui.home;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.easytickets.data.RepositoryCallback;
import com.example.easytickets.data.location.LocationRepository;
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
 * It loads event categories once, brokers hotel and city autocomplete/place resolution,
 * and requests device location for the my-location path.
 */
public class HomeViewModel extends ViewModel {

    private final TicketmasterRepository ticketmasterRepository;
    private final PlacesRepository placesRepository;
    private final LocationRepository locationRepository;

    private final MutableLiveData<List<EventCategory>> categories = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> categoriesLoading = new MutableLiveData<>(false);
    private final MutableLiveData<List<PlaceSuggestion>> hotelSuggestions = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<List<PlaceSuggestion>> citySuggestions = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<Boolean> hotelSearchLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> citySearchLoading = new MutableLiveData<>(false);
    private final MutableLiveData<PlaceSelection> selectedHotel = new MutableLiveData<>();
    private final MutableLiveData<PlaceSelection> selectedCity = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hotelSelectionLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> citySelectionLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Location> currentLocation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> currentLocationLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> message = new MutableLiveData<>("");

    private String latestHotelQuery = "";
    private String latestCityQuery = "";

    public HomeViewModel(
            TicketmasterRepository ticketmasterRepository,
            PlacesRepository placesRepository,
            LocationRepository locationRepository
    ) {
        this.ticketmasterRepository = ticketmasterRepository;
        this.placesRepository = placesRepository;
        this.locationRepository = locationRepository;
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

    public LiveData<List<PlaceSuggestion>> getCitySuggestions() {
        return citySuggestions;
    }

    public LiveData<Boolean> getHotelSearchLoading() {
        return hotelSearchLoading;
    }

    public LiveData<Boolean> getCitySearchLoading() {
        return citySearchLoading;
    }

    public LiveData<PlaceSelection> getSelectedHotel() {
        return selectedHotel;
    }

    public LiveData<PlaceSelection> getSelectedCity() {
        return selectedCity;
    }

    public LiveData<Boolean> getHotelSelectionLoading() {
        return hotelSelectionLoading;
    }

    public LiveData<Boolean> getCitySelectionLoading() {
        return citySelectionLoading;
    }

    public LiveData<Location> getCurrentLocation() {
        return currentLocation;
    }

    public LiveData<Boolean> getCurrentLocationLoading() {
        return currentLocationLoading;
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

    public void searchCities(String query) {
        latestCityQuery = normalizeQuery(query);
        if (latestCityQuery.length() < 2) {
            citySearchLoading.setValue(false);
            citySuggestions.setValue(Collections.emptyList());
            return;
        }

        citySearchLoading.setValue(true);
        placesRepository.searchCities(latestCityQuery, new RepositoryCallback<List<PlaceSuggestion>>() {
            @Override
            public void onSuccess(List<PlaceSuggestion> data) {
                if (!latestCityQuery.equals(normalizeQuery(query))) {
                    return;
                }
                citySearchLoading.postValue(false);
                citySuggestions.postValue(data);
            }

            @Override
            public void onError(String errorMessage) {
                citySearchLoading.postValue(false);
                citySuggestions.postValue(Collections.emptyList());
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

    public void resolveCitySelection(PlaceSuggestion suggestion) {
        citySelectionLoading.setValue(true);
        placesRepository.fetchPlaceSelection(suggestion.getPlaceId(), new RepositoryCallback<PlaceSelection>() {
            @Override
            public void onSuccess(PlaceSelection data) {
                citySelectionLoading.postValue(false);
                selectedCity.postValue(data);
                citySuggestions.postValue(Collections.emptyList());
            }

            @Override
            public void onError(String errorMessage) {
                citySelectionLoading.postValue(false);
                message.postValue(errorMessage);
            }
        });
    }

    public void clearHotelSelection() {
        selectedHotel.setValue(null);
    }

    public void clearCitySelection() {
        selectedCity.setValue(null);
    }

    public void clearHotelSuggestions() {
        hotelSuggestions.setValue(Collections.emptyList());
    }

    public void clearCitySuggestions() {
        citySuggestions.setValue(Collections.emptyList());
    }

    public void requestCurrentLocation() {
        currentLocationLoading.setValue(true);
        locationRepository.getCurrentLocation(new RepositoryCallback<Location>() {
            @Override
            public void onSuccess(Location data) {
                currentLocationLoading.postValue(false);
                currentLocation.postValue(data);
            }

            @Override
            public void onError(String errorMessage) {
                currentLocationLoading.postValue(false);
                message.postValue(errorMessage);
            }
        });
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
