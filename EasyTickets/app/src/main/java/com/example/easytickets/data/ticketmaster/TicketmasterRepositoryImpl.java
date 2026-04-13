package com.example.easytickets.data.ticketmaster;

import com.example.easytickets.data.RepositoryCallback;
import com.example.easytickets.domain.mapper.TicketmasterMapper;
import com.example.easytickets.domain.model.EventCategory;
import com.example.easytickets.domain.model.EventSummary;
import com.example.easytickets.domain.model.SearchRequest;
import com.example.easytickets.util.AppConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Concrete Ticketmaster repository that executes Retrofit calls, maps DTOs into domain models,
 * caches category data, and supplies fallback categories when the remote list is unavailable.
 */
public class TicketmasterRepositoryImpl implements TicketmasterRepository {

    private final TicketmasterApiService apiService;
    private final TicketmasterQueryFactory queryFactory;
    private final TicketmasterMapper mapper;
    private final AppConfig appConfig;
    private List<EventCategory> cachedCategories;

    public TicketmasterRepositoryImpl(
            TicketmasterApiService apiService,
            TicketmasterQueryFactory queryFactory,
            AppConfig appConfig
    ) {
        this.apiService = apiService;
        this.queryFactory = queryFactory;
        this.mapper = new TicketmasterMapper();
        this.appConfig = appConfig;
        this.cachedCategories = null;
    }

    @Override
    public void fetchEventCategories(RepositoryCallback<List<EventCategory>> callback) {
        if (!appConfig.hasTicketmasterKey()) {
            callback.onError("Ticketmaster API key is missing.");
            return;
        }

        if (cachedCategories != null && !cachedCategories.isEmpty()) {
            callback.onSuccess(cachedCategories);
            return;
        }

        apiService.getClassifications(30).enqueue(new Callback<TicketmasterResponses.ClassificationSearchResponse>() {
            @Override
            public void onResponse(
                    Call<TicketmasterResponses.ClassificationSearchResponse> call,
                    Response<TicketmasterResponses.ClassificationSearchResponse> response
            ) {
                if (!response.isSuccessful() || response.body() == null) {
                    callback.onError("Couldn't load Ticketmaster event categories.");
                    return;
                }

                List<EventCategory> categories = mapper.toEventCategories(response.body());
                if (categories.isEmpty()) {
                    callback.onError("Ticketmaster returned no event categories.");
                    return;
                }

                cachedCategories = categories;
                callback.onSuccess(categories);
            }

            @Override
            public void onFailure(Call<TicketmasterResponses.ClassificationSearchResponse> call, Throwable throwable) {
                callback.onError("Couldn't load Ticketmaster event categories.");
            }
        });
    }

    @Override
    public void searchEvents(SearchRequest request, RepositoryCallback<List<EventSummary>> callback) {
        if (!appConfig.hasTicketmasterKey()) {
            callback.onError("Ticketmaster API key is missing.");
            return;
        }

        Map<String, String> query = queryFactory.buildEventSearchQuery(request);
        apiService.searchEvents(query).enqueue(new Callback<TicketmasterResponses.EventSearchResponse>() {
            @Override
            public void onResponse(
                    Call<TicketmasterResponses.EventSearchResponse> call,
                    Response<TicketmasterResponses.EventSearchResponse> response
            ) {
                if (!response.isSuccessful()) {
                    callback.onError("Ticketmaster search failed. Please try again.");
                    return;
                }

                List<EventSummary> events = mapper.toEventSummaries(response.body());
                callback.onSuccess(events);
            }

            @Override
            public void onFailure(Call<TicketmasterResponses.EventSearchResponse> call, Throwable throwable) {
                callback.onError("Couldn't reach Ticketmaster. Check your connection and try again.");
            }
        });
    }

    @Override
    public List<EventCategory> getFallbackCategories() {
        return new ArrayList<>(Arrays.asList(
                new EventCategory("KZFzniwnSyZfZ7v7nJ", "Music"),
                new EventCategory("KZFzniwnSyZfZ7v7nE", "Sports"),
                new EventCategory("KZFzniwnSyZfZ7v7na", "Arts & Theatre"),
                new EventCategory("KZFzniwnSyZfZ7v7nn", "Film"),
                new EventCategory("KZFzniwnSyZfZ7v7n1", "Miscellaneous")
        ));
    }
}
