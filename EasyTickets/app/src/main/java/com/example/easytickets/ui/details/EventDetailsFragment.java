package com.example.easytickets.ui.details;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.example.easytickets.R;
import com.example.easytickets.databinding.FragmentEventDetailsBinding;
import com.example.easytickets.domain.model.EventDetails;
import com.example.easytickets.domain.model.EventSummary;
import com.example.easytickets.ui.common.BaseEasyTicketsFragment;

public class EventDetailsFragment extends BaseEasyTicketsFragment {

    public static final String ARG_EVENT_DETAILS = "arg_event_details";

    private FragmentEventDetailsBinding binding;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = FragmentEventDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.detailsBackButton.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        bindEventDetails();
    }

    private void bindEventDetails() {
        EventDetails eventDetails = getEventDetails();
        if (eventDetails == null || eventDetails.getSummary() == null) {
            showSnackbar(binding.getRoot(), "Missing event details.");
            return;
        }

        EventSummary event = eventDetails.getSummary();
        binding.detailsTitle.setText(event.getName());
        binding.detailsDateValue.setText(valueOrFallback(event.getDisplayDateTime()));
        binding.detailsVenueValue.setText(valueOrFallback(event.getVenueName()));
        binding.detailsAddressValue.setText(valueOrFallback(event.getVenueAddress()));
        binding.detailsCategoryValue.setText(valueOrFallback(event.getCategoryName()));
        binding.detailsUrlValue.setText(valueOrFallback(event.getEventUrl()));

        if (event.getImageUrl().isEmpty()) {
            binding.detailsImage.setVisibility(View.GONE);
        } else {
            binding.detailsImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(event.getImageUrl())
                    .centerCrop()
                    .into(binding.detailsImage);
        }

        boolean hasUrl = !event.getEventUrl().isEmpty();
        binding.detailsOpenWebsite.setEnabled(hasUrl);
        binding.detailsOpenWebsite.setOnClickListener(view ->
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(event.getEventUrl()))));
    }

    private EventDetails getEventDetails() {
        Bundle arguments = getArguments();
        if (arguments == null) {
            return null;
        }
        return (EventDetails) arguments.getSerializable(ARG_EVENT_DETAILS);
    }

    private String valueOrFallback(String value) {
        return value == null || value.isEmpty() ? getString(R.string.value_not_available) : value;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
