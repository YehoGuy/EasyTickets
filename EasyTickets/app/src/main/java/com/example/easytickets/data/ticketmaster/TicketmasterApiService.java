package com.example.easytickets.data.ticketmaster;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;

public interface TicketmasterApiService {

    @GET("events.json")
    Call<TicketmasterResponses.EventSearchResponse> searchEvents(@QueryMap Map<String, String> query);

    @GET("classifications.json")
    Call<TicketmasterResponses.ClassificationSearchResponse> getClassifications(@Query("size") int size);
}
