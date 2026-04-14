package com.example.easytickets.ui.home;

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
import com.example.easytickets.databinding.FragmentHomeBinding;
import com.example.easytickets.domain.model.SearchRequest;
import com.example.easytickets.ui.common.BaseEasyTicketsFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

/**
 * Top-level home screen that combines the decorative background map with the floating search surface.
 * It hosts the hotel search form and routes completed searches to the results screen.
 */
public class HomeFragment extends BaseEasyTicketsFragment implements SearchFormListener, OnMapReadyCallback {

    public static final String ARG_SEARCH_REQUEST = "arg_search_request";

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(
                requireActivity(),
                new HomeViewModelFactory(getAppContainer())
        ).get(HomeViewModel.class);

        setupBackgroundMap();
        setupHotelForm();
        observeViewModel();
    }

    private void setupBackgroundMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.home_background_map_container);
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.home_background_map_container, mapFragment)
                    .commitNow();
        }
        mapFragment.getMapAsync(this);
    }

    private void setupHotelForm() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.search_form_container, new HotelSearchFragment(), "hotel_search")
                .commit();
    }

    private void observeViewModel() {
        viewModel.getCategoriesLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.categoryProgress.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.home_map_style));
        googleMap.getUiSettings().setAllGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(34.0522, -98.2437), 3.2f));
    }

    @Override
    public void onSearchSubmitted(SearchRequest searchRequest) {
        NavController navController = NavHostFragment.findNavController(this);
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_SEARCH_REQUEST, searchRequest);
        navController.navigate(R.id.resultsMapFragment, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
