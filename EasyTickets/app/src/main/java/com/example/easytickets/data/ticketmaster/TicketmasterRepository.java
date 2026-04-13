package com.example.easytickets.data.ticketmaster;

import com.example.easytickets.data.RepositoryCallback;
import com.example.easytickets.domain.model.EventCategory;
import com.example.easytickets.domain.model.EventSummary;
import com.example.easytickets.domain.model.SearchRequest;

import java.util.List;

public interface TicketmasterRepository {

    void fetchEventCategories(RepositoryCallback<List<EventCategory>> callback);

    void searchEvents(SearchRequest request, RepositoryCallback<List<EventSummary>> callback);

    List<EventCategory> getFallbackCategories();
}
