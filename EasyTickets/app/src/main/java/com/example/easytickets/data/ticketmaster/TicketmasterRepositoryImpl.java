package com.example.easytickets.data.ticketmaster;

import android.util.Log;

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

    private static final String TAG = "TicketmasterRepo";
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
                    String errorBody = readErrorBody(response);
                    Log.e(TAG, "Ticketmaster categories request failed. httpCode=" + response.code()
                            + ", errorBody=" + errorBody);
                    callback.onError(buildHttpErrorMessage(
                            "Couldn't load Ticketmaster event categories.",
                            response.code(),
                            errorBody
                    ));
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
                Log.e(TAG, "Ticketmaster categories request failed before response.", throwable);
                callback.onError(buildNetworkErrorMessage(
                        "Couldn't load Ticketmaster event categories.",
                        throwable
                ));
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
                    String errorBody = readErrorBody(response);
                    Log.e(TAG, "Ticketmaster event search failed. httpCode=" + response.code()
                            + ", errorBody=" + errorBody + ", query=" + query);
                    callback.onError(buildHttpErrorMessage(
                            "Ticketmaster search failed.",
                            response.code(),
                            errorBody
                    ));
                    return;
                }

                List<EventSummary> events = mapper.toEventSummaries(response.body());
                callback.onSuccess(events);
            }

            @Override
            public void onFailure(Call<TicketmasterResponses.EventSearchResponse> call, Throwable throwable) {
                Log.e(TAG, "Ticketmaster event search failed before response. query=" + query, throwable);
                callback.onError(buildNetworkErrorMessage(
                        "Couldn't reach Ticketmaster.",
                        throwable
                ));
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

    private String buildHttpErrorMessage(String prefix, int httpCode, String errorBody) {
        String details = safe(errorBody);
        if (!details.isEmpty()) {
            return prefix + " HTTP " + httpCode + ". " + details;
        }
        return prefix + " HTTP " + httpCode + ".";
    }

    private String buildNetworkErrorMessage(String prefix, Throwable throwable) {
        String details = throwable == null ? "" : safe(throwable.getMessage());
        if (!details.isEmpty()) {
            return prefix + " " + details + " (HTTP code N/A).";
        }
        return prefix + " HTTP code N/A.";
    }

    private String readErrorBody(Response<?> response) {
        if (response == null || response.errorBody() == null) {
            return "";
        }
        try {
            return safe(response.errorBody().string());
        } catch (Exception ignored) {
            return "";
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
