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

import com.example.easytickets.databinding.FragmentHotelSearchBinding;
import com.example.easytickets.domain.model.EventCategory;
import com.example.easytickets.domain.model.PlaceSelection;
import com.example.easytickets.domain.model.PlaceSuggestion;
import com.example.easytickets.domain.model.SearchFilters;
import com.example.easytickets.domain.model.SearchMode;
import com.example.easytickets.domain.model.SearchRequest;
import com.example.easytickets.ui.common.BaseEasyTicketsFragment;
import com.example.easytickets.ui.common.FilterUiHelper;
import com.example.easytickets.ui.common.PlaceSuggestionAdapter;
import com.example.easytickets.util.DistanceUnitResolver;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;

/**
 * Home-screen child fragment for hotel-based searches.
 * It manages hotel autocomplete, selected-hotel presentation, category/radius filters,
 * and {@link SearchRequest} creation for nearby event queries.
 */
public class HotelSearchFragment extends BaseEasyTicketsFragment {

    private static final long AUTOCOMPLETE_DELAY_MS = 350L;

    private FragmentHotelSearchBinding binding;
    private HomeViewModel viewModel;
    private PlaceSuggestionAdapter placeSuggestionAdapter;
    private final DistanceUnitResolver distanceUnitResolver = new DistanceUnitResolver();
    private final Handler autocompleteHandler = new Handler(Looper.getMainLooper());

    private final List<EventCategory> categories = new ArrayList<>();
    private Runnable pendingAutocompleteRunnable;
    private boolean suppressTextWatcher;
    private PlaceSelection selectedPlace;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentHotelSearchBinding.inflate(inflater, container, false);
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
        setupRadiusChips();
        setupQueryField();
        setupSearchButton();
        observeViewModel();
    }

    private void setupPredictionList() {
        placeSuggestionAdapter = new PlaceSuggestionAdapter(this::onSuggestionClicked);
        binding.hotelPredictionsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.hotelPredictionsList.setAdapter(placeSuggestionAdapter);
    }

    private void setupRadiusChips() {
        checkDefaultRadius(binding.hotelRadiusChips, "25");
        FilterUiHelper.updateRadiusChipLabels(
                binding.hotelRadiusChips,
                distanceUnitResolver,
                distanceUnitResolver.resolveForDeviceLocale()
        );
    }

    private void setupQueryField() {
        binding.hotelQueryEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (suppressTextWatcher) {
                    return;
                }

                if (selectedPlace != null) {
                    viewModel.clearHotelSelection();
                }

                autocompleteHandler.removeCallbacksAndMessages(null);
                String query = charSequence == null ? "" : charSequence.toString();
                pendingAutocompleteRunnable = () -> {
                    if (query.trim().length() < 2) {
                        viewModel.clearHotelSuggestions();
                    } else {
                        viewModel.searchHotels(query);
                    }
                };
                autocompleteHandler.postDelayed(pendingAutocompleteRunnable, AUTOCOMPLETE_DELAY_MS);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void setupSearchButton() {
        binding.hotelSearchButton.setOnClickListener(view -> {
            if (selectedPlace == null) {
                showSnackbar(binding.getRoot(), "Select a hotel before searching.");
                return;
            }

            String unit = distanceUnitResolver.resolveForCountry(selectedPlace.getCountryCode());
            SearchFilters filters = FilterUiHelper.buildFilters(
                    binding.hotelCategoryChips,
                    categories,
                    binding.hotelRadiusChips,
                    unit
            );
            SearchRequest request = new SearchRequest(
                    SearchMode.HOTEL,
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
                    binding.hotelCategoryChips,
                    categories,
                    FilterUiHelper.collectSelectedCategoryIds(binding.hotelCategoryChips)
            );
        });

        viewModel.getHotelSuggestions().observe(getViewLifecycleOwner(), suggestions -> {
            placeSuggestionAdapter.submitList(suggestions);
            binding.hotelPredictionsList.setVisibility(suggestions.isEmpty() ? View.GONE : View.VISIBLE);
        });

        viewModel.getHotelSearchLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.hotelPredictionProgress.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));

        viewModel.getHotelSelectionLoading().observe(getViewLifecycleOwner(), isLoading ->
                binding.hotelSelectionProgress.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));

        viewModel.getSelectedHotel().observe(getViewLifecycleOwner(), placeSelection -> {
            selectedPlace = placeSelection;
            if (placeSelection == null) {
                binding.hotelSelectedPlaceCard.setVisibility(View.GONE);
                binding.hotelSearchButton.setEnabled(false);
                return;
            }

            suppressTextWatcher = true;
            binding.hotelQueryEditText.setText(placeSelection.getDisplayName());
            binding.hotelQueryEditText.setSelection(placeSelection.getDisplayName().length());
            suppressTextWatcher = false;

            binding.hotelSelectedPlaceCard.setVisibility(View.VISIBLE);
            binding.hotelSelectedPlaceName.setText(placeSelection.getDisplayName());
            binding.hotelSelectedPlaceAddress.setText(placeSelection.getAddress());
            binding.hotelSearchButton.setEnabled(true);
            FilterUiHelper.updateRadiusChipLabels(
                    binding.hotelRadiusChips,
                    distanceUnitResolver,
                    distanceUnitResolver.resolveForCountry(placeSelection.getCountryCode())
            );
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                showSnackbar(binding.getRoot(), message);
                viewModel.clearMessage();
            }
        });
    }

    private void onSuggestionClicked(PlaceSuggestion suggestion) {
        binding.hotelPredictionsList.setVisibility(View.GONE);
        viewModel.resolveHotelSelection(suggestion);
    }

    private void checkDefaultRadius(com.google.android.material.chip.ChipGroup chipGroup, String tag) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            if (chipGroup.getChildAt(i) instanceof Chip) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                if (tag.equals(chip.getTag())) {
                    chip.setChecked(true);
                    return;
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        autocompleteHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
        binding = null;
    }
}
