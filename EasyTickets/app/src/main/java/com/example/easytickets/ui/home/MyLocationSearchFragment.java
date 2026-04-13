package com.example.easytickets.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.easytickets.R;
import com.example.easytickets.databinding.FragmentMyLocationSearchBinding;
import com.example.easytickets.domain.model.EventCategory;
import com.example.easytickets.domain.model.SearchFilters;
import com.example.easytickets.domain.model.SearchMode;
import com.example.easytickets.domain.model.SearchRequest;
import com.example.easytickets.ui.common.BaseEasyTicketsFragment;
import com.example.easytickets.ui.common.FilterUiHelper;
import com.example.easytickets.util.DistanceUnitResolver;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MyLocationSearchFragment extends BaseEasyTicketsFragment {

    private FragmentMyLocationSearchBinding binding;
    private HomeViewModel viewModel;
    private final DistanceUnitResolver distanceUnitResolver = new DistanceUnitResolver();
    private final List<EventCategory> categories = new ArrayList<>();

    private ActivityResultLauncher<String[]> permissionLauncher;
    private SearchFilters pendingSearchFilters;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentMyLocationSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean granted = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION))
                            || Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                    if (granted) {
                        submitLocationSearch();
                    } else {
                        if (binding != null) {
                            showSnackbar(binding.getRoot(), getString(R.string.location_permission_denied));
                        }
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(
                requireActivity(),
                new HomeViewModelFactory(getAppContainer())
        ).get(HomeViewModel.class);

        setupRadiusChips();
        setupSearchButton();
        observeViewModel();
    }

    private void setupRadiusChips() {
        checkDefaultRadius(binding.myLocationRadiusChips, "25");
        FilterUiHelper.updateRadiusChipLabels(
                binding.myLocationRadiusChips,
                distanceUnitResolver,
                distanceUnitResolver.resolveForDeviceLocale()
        );
    }

    private void setupSearchButton() {
        binding.myLocationSearchButton.setOnClickListener(view -> {
            pendingSearchFilters = FilterUiHelper.buildFilters(
                    binding.myLocationCategoryChips,
                    categories,
                    binding.myLocationRadiusChips,
                    distanceUnitResolver.resolveForDeviceLocale()
            );

            if (hasLocationPermission()) {
                submitLocationSearch();
            } else {
                permissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                });
            }
        });
    }

    private void observeViewModel() {
        viewModel.getCategories().observe(getViewLifecycleOwner(), updatedCategories -> {
            categories.clear();
            categories.addAll(updatedCategories);
            FilterUiHelper.renderCategoryChips(
                    requireContext(),
                    binding.myLocationCategoryChips,
                    categories,
                    FilterUiHelper.collectSelectedCategoryIds(binding.myLocationCategoryChips)
            );
        });

        viewModel.getCurrentLocationLoading().observe(getViewLifecycleOwner(), isLoading -> {
            boolean loading = Boolean.TRUE.equals(isLoading);
            binding.myLocationProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.myLocationSearchButton.setEnabled(!loading);
        });

        viewModel.getCurrentLocation().observe(getViewLifecycleOwner(), location -> {
            if (pendingSearchFilters == null || location == null) {
                return;
            }

            String countryCode = Locale.getDefault().getCountry();
            SearchRequest request = buildSearchRequest(location, countryCode);
            pendingSearchFilters = null;
            ((SearchFormListener) requireParentFragment()).onSearchSubmitted(request);
        });

        viewModel.getMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                showSnackbar(binding.getRoot(), message);
                viewModel.clearMessage();
            }
        });
    }

    private void submitLocationSearch() {
        viewModel.requestCurrentLocation();
    }

    private SearchRequest buildSearchRequest(Location location, String countryCode) {
        return new SearchRequest(
                SearchMode.MY_LOCATION,
                getString(R.string.my_location_origin_label),
                location.getLatitude(),
                location.getLongitude(),
                "",
                countryCode,
                pendingSearchFilters
        );
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
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
        super.onDestroyView();
        binding = null;
    }
}
