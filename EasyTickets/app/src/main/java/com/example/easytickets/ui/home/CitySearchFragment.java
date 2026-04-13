package com.example.easytickets.ui.home;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.easytickets.databinding.FragmentCitySearchBinding;
import com.example.easytickets.domain.model.EventCategory;
import com.example.easytickets.domain.model.PlaceSelection;
import com.example.easytickets.domain.model.PlaceSuggestion;
import com.example.easytickets.domain.model.SearchFilters;
import com.example.easytickets.domain.model.SearchMode;
import com.example.easytickets.domain.model.SearchRequest;
import com.example.easytickets.ui.common.BaseEasyTicketsFragment;
import com.example.easytickets.ui.common.FilterUiHelper;
import com.example.easytickets.ui.common.PlaceSuggestionAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Home-screen child fragment for city-based searches.
 * It owns city autocomplete, selected-city presentation, category filters,
 * and {@link SearchRequest} creation for the city flow.
 */
public class CitySearchFragment extends BaseEasyTicketsFragment {

    private static final long AUTOCOMPLETE_DELAY_MS = 350L;

    private FragmentCitySearchBinding binding;
    private HomeViewModel viewModel;
    private PlaceSuggestionAdapter placeSuggestionAdapter;
    private final Handler autocompleteHandler = new Handler(Looper.getMainLooper());
    private final List<EventCategory> categories = new ArrayList<>();

    private boolean suppressTextWatcher;
    private PlaceSelection selectedPlace;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentCitySearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(
                requireActivity(),
                new HomeViewModelFactory(getAppContainer())
        ).get(HomeViewModel.class);

        setupPredictionList();
        setupQueryField();
        setupSearchButton();
        observeViewModel();
    }

    private void setupPredictionList() {
        placeSuggestionAdapter = new PlaceSuggestionAdapter(this::onSuggestionClicked);
        binding.cityPredictionsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.cityPredictionsList.setAdapter(placeSuggestionAdapter);
    }

    private void setupQueryField() {
        binding.cityQueryEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (suppressTextWatcher) {
                    return;
                }

                if (selectedPlace != null) {
                    viewModel.clearCitySelection();
                }

                autocompleteHandler.removeCallbacksAndMessages(null);
                String query = charSequence == null ? "" : charSequence.toString();
                autocompleteHandler.postDelayed(() -> {
                    if (query.trim().length() < 2) {
                        viewModel.clearCitySuggestions();
                    } else {
                        viewModel.searchCities(query);
                    }
                }, AUTOCOMPLETE_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void setupSearchButton() {
        binding.citySearchButton.setOnClickListener(view -> {
            if (selectedPlace == null) {
                showSnackbar(binding.getRoot(), "Select a city before searching.");
                return;
            }

            SearchFilters filters = FilterUiHelper.buildFilters(
                    binding.cityCategoryChips,
                    categories,
                    null,
                    ""
            );
            SearchRequest request = new SearchRequest(
                    SearchMode.CITY,
                    selectedPlace.getDisplayName(),
                    selectedPlace.getLatitude(),
                    selectedPlace.getLongitude(),
                    selectedPlace.getCityName(),
                    selectedPlace.getCountryCode(),
                    filters
            );
            ((SearchFormListener) requireParentFragment()).onSearchSubmitted(request);
        });
    }

    private void observeViewModel() {
        viewModel.getCategories().observe(getViewLifecycleOwner(), updatedCategories -> {
            categories.clear();
            categories.addAll(updatedCategories);
            FilterUiHelper.renderCategoryChips(
                    requireContext(),
                    binding.cityCategoryChips,
                    categories,
                    FilterUiHelper.collectSelectedCategoryIds(binding.cityCategoryChips)
            );
        });

        viewModel.getCitySuggestions().observe(getViewLifecycleOwner(), suggestions -> {
            placeSuggestionAdapter.submitList(suggestions);
            binding.cityPredictionsList.setVisibility(suggestions.isEmpty() ? View.GONE : View.VISIBLE);
        });

        viewModel.getCitySearchLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.cityPredictionProgress.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));

        viewModel.getCitySelectionLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.citySelectionProgress.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));

        viewModel.getSelectedCity().observe(getViewLifecycleOwner(), placeSelection -> {
            selectedPlace = placeSelection;
            if (placeSelection == null) {
                binding.citySelectedPlaceCard.setVisibility(View.GONE);
                binding.citySearchButton.setEnabled(false);
                return;
            }

            suppressTextWatcher = true;
            binding.cityQueryEditText.setText(placeSelection.getDisplayName());
            binding.cityQueryEditText.setSelection(placeSelection.getDisplayName().length());
            suppressTextWatcher = false;

            binding.citySelectedPlaceCard.setVisibility(View.VISIBLE);
            binding.citySelectedPlaceName.setText(placeSelection.getDisplayName());
            binding.citySelectedPlaceAddress.setText(placeSelection.getAddress());
            binding.citySearchButton.setEnabled(true);
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                showSnackbar(binding.getRoot(), message);
                viewModel.clearMessage();
            }
        });
    }

    private void onSuggestionClicked(PlaceSuggestion suggestion) {
        binding.cityPredictionsList.setVisibility(View.GONE);
        viewModel.resolveCitySelection(suggestion);
    }

    @Override
    public void onDestroyView() {
        autocompleteHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
        binding = null;
    }
}
