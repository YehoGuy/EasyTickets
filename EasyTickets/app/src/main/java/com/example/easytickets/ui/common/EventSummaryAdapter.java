package com.example.easytickets.ui.common;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytickets.databinding.ItemEventSummaryBinding;
import com.example.easytickets.domain.model.EventSummary;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for the results bottom sheet when a marker represents one or more events.
 * It renders compact event rows and forwards click events upstream.
 */
public class EventSummaryAdapter extends RecyclerView.Adapter<EventSummaryAdapter.ViewHolder> {

    public interface OnEventClickedListener {
        void onEventClicked(EventSummary event);
    }

    private final OnEventClickedListener listener;
    private final List<EventSummary> events = new ArrayList<>();

    public EventSummaryAdapter(OnEventClickedListener listener) {
        this.listener = listener;
    }

    public void submitList(List<EventSummary> updatedEvents) {
        events.clear();
        events.addAll(updatedEvents);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEventSummaryBinding binding = ItemEventSummaryBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(events.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemEventSummaryBinding binding;

        ViewHolder(ItemEventSummaryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(EventSummary event, OnEventClickedListener listener) {
            binding.eventSummaryName.setText(event.getName());
            binding.eventSummaryDate.setText(event.getDisplayDateTime());
            binding.eventSummaryVenue.setText(event.getVenueName());
            binding.getRoot().setOnClickListener(view -> listener.onEventClicked(event));
        }
    }
}
