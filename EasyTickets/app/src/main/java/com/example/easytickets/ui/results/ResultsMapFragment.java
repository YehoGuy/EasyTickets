package com.example.easytickets.ui.results;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.example.easytickets.R;
import com.example.easytickets.databinding.FragmentResultsMapBinding;
import com.example.easytickets.domain.model.EventDetails;
import com.example.easytickets.domain.model.EventMapGroup;
import com.example.easytickets.domain.model.EventSummary;
import com.example.easytickets.domain.model.SearchRequest;
import com.example.easytickets.ui.common.BaseEasyTicketsFragment;
import com.example.easytickets.ui.details.EventDetailsFragment;
import com.example.easytickets.ui.home.HomeFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Results screen that executes the search, renders the origin and grouped event markers,
 * and opens the event bottom sheet/details flow.
 */
public class ResultsMapFragment extends BaseEasyTicketsFragment implements OnMapReadyCallback {

    private FragmentResultsMapBinding binding;
    private ResultsViewModel viewModel;
    private SearchRequest searchRequest;
    private GoogleMap googleMap;
    private final Map<String, EventMapGroup> markerGroups = new HashMap<>();
    private List<EventMapGroup> latestGroups = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentResultsMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        searchRequest = getSearchRequest();
        viewModel = new ViewModelProvider(
                this,
                new ResultsViewModelFactory(getAppContainer())
        ).get(ResultsViewModel.class);

        setupToolbar();
        setupMap();
        setupBottomSheetResult();
        setupRetry();
        observeViewModel();

        if (searchRequest != null) {
            binding.resultsHeaderSubtitle.setText(searchRequest.getOriginLabel());
            viewModel.loadSearchResults(searchRequest);
        }
    }

    private void setupToolbar() {
        binding.resultsBackButton.setOnClickListener(view ->
                NavHostFragment.findNavController(this).navigateUp());
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.results_map_container);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.results_map_container, mapFragment)
                    .commitNow();
        }
        mapFragment.getMapAsync(this);
    }

    private void setupBottomSheetResult() {
        getParentFragmentManager().setFragmentResultListener(
                EventGroupBottomSheet.RESULT_REQUEST_KEY,
                getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    EventSummary selectedEvent = (EventSummary) bundle.getSerializable(EventGroupBottomSheet.RESULT_EVENT_KEY);
                    if (selectedEvent != null) {
                        openEventDetails(selectedEvent.toEventDetails());
                    }
                }
        );
    }

    private void setupRetry() {
        binding.resultsRetryButton.setOnClickListener(view -> viewModel.retry());
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.resultsProgress.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message == null || message.isEmpty()) {
                binding.resultsErrorCard.setVisibility(View.GONE);
            } else {
                binding.resultsErrorCard.setVisibility(View.VISIBLE);
                binding.resultsErrorMessage.setText(message);
                binding.resultsHintCard.setVisibility(View.GONE);
            }
        });

        viewModel.getEmptyStateVisible().observe(getViewLifecycleOwner(), isVisible -> {
            boolean visible = Boolean.TRUE.equals(isVisible);
            binding.resultsEmptyCard.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (visible) {
                binding.resultsHintCard.setVisibility(View.GONE);
            }
        });

        viewModel.getEventGroups().observe(getViewLifecycleOwner(), eventMapGroups -> {
            latestGroups = eventMapGroups;
            binding.resultsErrorCard.setVisibility(View.GONE);
            if (eventMapGroups == null || eventMapGroups.isEmpty()) {
                binding.resultsHintCard.setVisibility(View.GONE);
            } else {
                binding.resultsHintCard.setVisibility(View.VISIBLE);
                binding.resultsHintBody.setText(getString(
                        R.string.results_hint_body,
                        eventMapGroups.size()
                ));
            }
            renderMarkersIfReady();
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.setOnMarkerClickListener(marker -> {
            EventMapGroup group = markerGroups.get(marker.getId());
            if (group == null) {
                return false;
            }
            EventGroupBottomSheet.newInstance(new ArrayList<>(group.getEvents()), group.getVenueLabel())
                    .show(getParentFragmentManager(), "event_group_sheet");
            return true;
        });
        renderMarkersIfReady();
    }

    private void renderMarkersIfReady() {
        if (googleMap == null || searchRequest == null) {
            return;
        }

        googleMap.clear();
        markerGroups.clear();

        LatLng origin = new LatLng(searchRequest.getOriginLatitude(), searchRequest.getOriginLongitude());
        googleMap.addMarker(new MarkerOptions()
                .position(origin)
                .title(searchRequest.getOriginLabel())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        for (EventMapGroup group : latestGroups) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(group.getLatitude(), group.getLongitude()))
                    .title(group.getMarkerTitle())
                    .snippet(group.getMarkerSnippet())
                    .icon(BitmapDescriptorFactory.defaultMarker(
                            group.isMultiple() ? BitmapDescriptorFactory.HUE_ORANGE : BitmapDescriptorFactory.HUE_RED
                    )));
            if (marker != null) {
                markerGroups.put(marker.getId(), group);
            }
        }

        fitCamera(origin, latestGroups);
    }

    private void fitCamera(LatLng origin, List<EventMapGroup> groups) {
        if (groups == null || groups.isEmpty()) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(origin, 11f));
            return;
        }

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boundsBuilder.include(origin);
        for (EventMapGroup group : groups) {
            boundsBuilder.include(new LatLng(group.getLatitude(), group.getLongitude()));
        }
        binding.resultsMapContainer.post(() ->
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 160)));
    }

    private SearchRequest getSearchRequest() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return null;
        }
        return (SearchRequest) arguments.getSerializable(HomeFragment.ARG_SEARCH_REQUEST);
    }

    private void openEventDetails(EventDetails eventDetails) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EventDetailsFragment.ARG_EVENT_DETAILS, eventDetails);
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.eventDetailsFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
