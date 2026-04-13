package com.example.easytickets.ui.home;

import com.example.easytickets.domain.model.SearchRequest;

/**
 * Callback implemented by the home container so child search fragments can submit a completed
 * {@link SearchRequest} without owning navigation.
 */
public interface SearchFormListener {
    void onSearchSubmitted(SearchRequest searchRequest);
}
