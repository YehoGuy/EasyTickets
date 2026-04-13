package com.example.easytickets.ui.results;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.easytickets.di.AppContainer;

public class ResultsViewModelFactory implements ViewModelProvider.Factory {

    private final AppContainer appContainer;

    public ResultsViewModelFactory(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ResultsViewModel.class)) {
            return (T) new ResultsViewModel(appContainer.getTicketmasterRepository());
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
