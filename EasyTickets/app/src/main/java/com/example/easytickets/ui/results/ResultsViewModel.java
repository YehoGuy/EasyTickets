package com.example.easytickets.ui.results;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.easytickets.data.RepositoryCallback;
import com.example.easytickets.data.ticketmaster.TicketmasterRepository;
import com.example.easytickets.domain.model.EventMapGroup;
import com.example.easytickets.domain.model.EventSummary;
import com.example.easytickets.domain.model.SearchRequest;
import com.example.easytickets.util.EventGroupingUtils;

import java.util.Collections;
import java.util.List;

public class ResultsViewModel extends ViewModel {

    private final TicketmasterRepository ticketmasterRepository;

    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<List<EventMapGroup>> eventGroups = new MutableLiveData<>(Collections.emptyList());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>("");
    private final MutableLiveData<Boolean> emptyStateVisible = new MutableLiveData<>(false);

    private SearchRequest activeSearchRequest;

    public ResultsViewModel(TicketmasterRepository ticketmasterRepository) {
        this.ticketmasterRepository = ticketmasterRepository;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<List<EventMapGroup>> getEventGroups() {
        return eventGroups;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getEmptyStateVisible() {
        return emptyStateVisible;
    }

    public void loadSearchResults(SearchRequest searchRequest) {
        activeSearchRequest = searchRequest;
        loading.setValue(true);
        errorMessage.setValue("");
        emptyStateVisible.setValue(false);

        ticketmasterRepository.searchEvents(searchRequest, new RepositoryCallback<List<EventSummary>>() {
            @Override
            public void onSuccess(List<EventSummary> data) {
                List<EventMapGroup> groupedEvents = EventGroupingUtils.groupEvents(data);
                loading.postValue(false);
                eventGroups.postValue(groupedEvents);
                emptyStateVisible.postValue(groupedEvents.isEmpty());
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                errorMessage.postValue(message);
            }
        });
    }

    public void retry() {
        if (activeSearchRequest != null) {
            loadSearchResults(activeSearchRequest);
        }
    }
}
