package com.example.easytickets.di;

import android.content.Context;

import com.example.easytickets.BuildConfig;
import com.example.easytickets.data.location.DeviceLocationRepository;
import com.example.easytickets.data.location.LocationRepository;
import com.example.easytickets.data.places.GooglePlacesRepository;
import com.example.easytickets.data.places.PlacesRepository;
import com.example.easytickets.data.ticketmaster.TicketmasterApiService;
import com.example.easytickets.data.ticketmaster.TicketmasterQueryFactory;
import com.example.easytickets.data.ticketmaster.TicketmasterRepository;
import com.example.easytickets.data.ticketmaster.TicketmasterRepositoryImpl;
import com.example.easytickets.util.AppConfig;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Central composition root for the app.
 * It validates configuration and constructs the shared repositories, network stack,
 * Places client, and location provider used across fragments and view models.
 */
public class AppContainer {

    private static final String TICKETMASTER_BASE_URL = "https://app.ticketmaster.com/discovery/v2/";

    private final AppConfig appConfig;
    private final TicketmasterRepository ticketmasterRepository;
    private final PlacesRepository placesRepository;
    private final LocationRepository locationRepository;

    public AppContainer(Context context) {
        Context appContext = context.getApplicationContext();
        appConfig = new AppConfig(
                BuildConfig.GOOGLE_MAPS_API_KEY,
                BuildConfig.TICKETMASTER_API_KEY
        );

        PlacesClient placesClient = null;
        if (appConfig.hasGoogleMapsKey()) {
            if (!Places.isInitialized()) {
                Places.initialize(appContext, appConfig.getGoogleMapsApiKey());
            }
            placesClient = Places.createClient(appContext);
        }

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);

        Interceptor apiKeyInterceptor = chain -> {
            Request original = chain.request();
            HttpUrl.Builder urlBuilder = original.url().newBuilder();
            if (appConfig.hasTicketmasterKey()) {
                urlBuilder.addQueryParameter("apikey", appConfig.getTicketmasterApiKey());
            }
            Request request = original.newBuilder().url(urlBuilder.build()).build();
            return chain.proceed(request);
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(apiKeyInterceptor)
                .addInterceptor(loggingInterceptor)
                .build();

        Gson gson = new GsonBuilder().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TICKETMASTER_BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        TicketmasterApiService apiService = retrofit.create(TicketmasterApiService.class);
        TicketmasterQueryFactory queryFactory = new TicketmasterQueryFactory();

        ticketmasterRepository = new TicketmasterRepositoryImpl(apiService, queryFactory, appConfig);
        placesRepository = new GooglePlacesRepository(placesClient, appConfig);
        locationRepository = new DeviceLocationRepository(
                LocationServices.getFusedLocationProviderClient(appContext)
        );
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    public TicketmasterRepository getTicketmasterRepository() {
        return ticketmasterRepository;
    }

    public PlacesRepository getPlacesRepository() {
        return placesRepository;
    }

    public LocationRepository getLocationRepository() {
        return locationRepository;
    }
}
