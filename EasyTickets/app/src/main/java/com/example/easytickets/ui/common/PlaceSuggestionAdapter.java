package com.example.easytickets.ui.common;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.easytickets.databinding.ItemPlacePredictionBinding;
import com.example.easytickets.domain.model.PlaceSuggestion;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for Google Places autocomplete suggestions in the hotel search form.
 */
public class PlaceSuggestionAdapter extends RecyclerView.Adapter<PlaceSuggestionAdapter.ViewHolder> {

    public interface OnSuggestionClickedListener {
        void onSuggestionClicked(PlaceSuggestion suggestion);
    }

    private final OnSuggestionClickedListener listener;
    private final List<PlaceSuggestion> suggestions = new ArrayList<>();

    public PlaceSuggestionAdapter(OnSuggestionClickedListener listener) {
        this.listener = listener;
    }

    public void submitList(List<PlaceSuggestion> updatedSuggestions) {
        suggestions.clear();
        suggestions.addAll(updatedSuggestions);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPlacePredictionBinding binding = ItemPlacePredictionBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(suggestions.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final ItemPlacePredictionBinding binding;

        ViewHolder(ItemPlacePredictionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(PlaceSuggestion suggestion, OnSuggestionClickedListener listener) {
            binding.predictionTitle.setText(suggestion.getPrimaryText());
            binding.predictionSubtitle.setText(suggestion.getSecondaryText());
            binding.getRoot().setOnClickListener(view -> listener.onSuggestionClicked(suggestion));
        }
    }
}
