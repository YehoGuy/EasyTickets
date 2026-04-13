package com.example.easytickets.ui.common;

import android.content.Context;
import android.view.View;

import com.example.easytickets.R;
import com.example.easytickets.domain.model.EventCategory;
import com.example.easytickets.domain.model.SearchFilters;
import com.example.easytickets.util.DistanceUnitResolver;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class FilterUiHelper {

    private FilterUiHelper() {
    }

    public static void renderCategoryChips(
            Context context,
            ChipGroup chipGroup,
            List<EventCategory> categories,
            Set<String> selectedIds
    ) {
        chipGroup.removeAllViews();
        for (EventCategory category : categories) {
            Chip chip = new Chip(context);
            chip.setText(category.getName());
            chip.setCheckable(true);
            chip.setChecked(selectedIds.contains(category.getId()));
            chip.setTag(category.getId());
            chip.setChipMinHeightResource(R.dimen.filter_chip_min_height);
            chipGroup.addView(chip);
        }
    }

    public static Set<String> collectSelectedCategoryIds(ChipGroup chipGroup) {
        Set<String> selectedIds = new HashSet<>();
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            if (chipGroup.getChildAt(i) instanceof Chip) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                if (chip.isChecked() && chip.getTag() instanceof String) {
                    selectedIds.add((String) chip.getTag());
                }
            }
        }
        return selectedIds;
    }

    public static List<EventCategory> collectSelectedCategories(
            ChipGroup chipGroup,
            List<EventCategory> allCategories
    ) {
        Set<String> selectedIds = collectSelectedCategoryIds(chipGroup);
        List<EventCategory> selectedCategories = new ArrayList<>();
        for (EventCategory category : allCategories) {
            if (selectedIds.contains(category.getId())) {
                selectedCategories.add(category);
            }
        }
        return selectedCategories;
    }

    public static void updateRadiusChipLabels(
            ChipGroup chipGroup,
            DistanceUnitResolver unitResolver,
            String unit
    ) {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            if (chipGroup.getChildAt(i) instanceof Chip) {
                Chip chip = (Chip) chipGroup.getChildAt(i);
                Object tag = chip.getTag();
                if (tag instanceof String) {
                    int value = Integer.parseInt((String) tag);
                    chip.setText(unitResolver.formatChipLabel(value, unit));
                }
            }
        }
    }

    public static SearchFilters buildFilters(
            ChipGroup categoryChipGroup,
            List<EventCategory> allCategories,
            ChipGroup radiusChipGroup,
            String radiusUnit
    ) {
        int radius = 0;
        if (radiusChipGroup != null) {
            int checkedChipId = radiusChipGroup.getCheckedChipId();
            if (checkedChipId != View.NO_ID && radiusChipGroup.findViewById(checkedChipId) instanceof Chip) {
                Chip chip = radiusChipGroup.findViewById(checkedChipId);
                radius = Integer.parseInt((String) chip.getTag());
            }
        }

        return new SearchFilters(
                collectSelectedCategories(categoryChipGroup, allCategories),
                radius,
                radiusUnit
        );
    }
}
