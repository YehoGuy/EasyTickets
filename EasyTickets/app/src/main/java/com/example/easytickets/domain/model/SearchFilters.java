package com.example.easytickets.domain.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SearchFilters implements Serializable {

    private final ArrayList<EventCategory> selectedCategories;
    private final int radiusValue;
    private final String radiusUnit;

    public SearchFilters(List<EventCategory> selectedCategories, int radiusValue, String radiusUnit) {
        this.selectedCategories = new ArrayList<>(selectedCategories);
        this.radiusValue = radiusValue;
        this.radiusUnit = radiusUnit == null ? "" : radiusUnit;
    }

    public List<EventCategory> getSelectedCategories() {
        return Collections.unmodifiableList(selectedCategories);
    }

    public int getRadiusValue() {
        return radiusValue;
    }

    public String getRadiusUnit() {
        return radiusUnit;
    }

    public boolean hasRadius() {
        return radiusValue > 0 && !radiusUnit.isEmpty();
    }
}
