package com.example.easytickets.ui.home;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.easytickets.di.AppContainer;

/**
 * Factory that wires the shared home dependencies into {@link HomeViewModel} instances.
 */
public class HomeViewModelFactory implements ViewModelProvider.Factory {

    private final AppContainer appContainer;

    public HomeViewModelFactory(AppContainer appContainer) {
        this.appContainer = appContainer;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(HomeViewModel.class)) {
            return (T) new HomeViewModel(
                    appContainer.getTicketmasterRepository(),
                    appContainer.getPlacesRepository()
            );
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
