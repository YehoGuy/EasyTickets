package com.example.easytickets.ui.results;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.easytickets.databinding.BottomSheetEventGroupBinding;
import com.example.easytickets.domain.model.EventSummary;
import com.example.easytickets.ui.common.EventSummaryAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class EventGroupBottomSheet extends BottomSheetDialogFragment {

    public static final String RESULT_REQUEST_KEY = "event_group_result";
    public static final String RESULT_EVENT_KEY = "result_event";
    private static final String ARG_EVENTS = "arg_events";
    private static final String ARG_VENUE_LABEL = "arg_venue_label";

    private BottomSheetEventGroupBinding binding;

    public static EventGroupBottomSheet newInstance(ArrayList<EventSummary> events, String venueLabel) {
        EventGroupBottomSheet bottomSheet = new EventGroupBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENTS, events);
        args.putString(ARG_VENUE_LABEL, venueLabel);
        bottomSheet.setArguments(args);
        return bottomSheet;
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        binding = BottomSheetEventGroupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ArrayList<EventSummary> events = getEvents();
        String venueLabel = getArguments() == null ? "" : getArguments().getString(ARG_VENUE_LABEL, "");

        binding.bottomSheetTitle.setText(events.size() > 1 ? "Multiple events" : events.get(0).getName());
        binding.bottomSheetSubtitle.setText(venueLabel);

        if (events.size() == 1) {
            EventSummary singleEvent = events.get(0);
            binding.bottomSheetOpenSingleButton.setVisibility(View.VISIBLE);
            binding.bottomSheetOpenSingleButton.setText(singleEvent.getDisplayDateTime());
            binding.bottomSheetOpenSingleButton.setOnClickListener(button -> deliverResult(singleEvent));
            binding.bottomSheetEventsList.setVisibility(View.GONE);
            return;
        }

        binding.bottomSheetOpenSingleButton.setVisibility(View.GONE);
        binding.bottomSheetEventsList.setVisibility(View.VISIBLE);
        binding.bottomSheetEventsList.setLayoutManager(new LinearLayoutManager(requireContext()));
        EventSummaryAdapter adapter = new EventSummaryAdapter(this::deliverResult);
        binding.bottomSheetEventsList.setAdapter(adapter);
        adapter.submitList(events);
    }

    @SuppressWarnings("unchecked")
    private ArrayList<EventSummary> getEvents() {
        if (getArguments() == null) {
            return new ArrayList<>();
        }
        ArrayList<EventSummary> events = (ArrayList<EventSummary>) getArguments().getSerializable(ARG_EVENTS);
        return events == null ? new ArrayList<>() : events;
    }

    private void deliverResult(EventSummary event) {
        Bundle result = new Bundle();
        result.putSerializable(RESULT_EVENT_KEY, event);
        getParentFragmentManager().setFragmentResult(RESULT_REQUEST_KEY, result);
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
