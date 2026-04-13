package com.example.easytickets.util;

import java.util.Locale;

/**
 * Converts market context into the radius unit labels shown in the UI.
 * This keeps miles-versus-kilometers logic consistent across the search forms.
 */
public class DistanceUnitResolver {

    public static final String UNIT_MILES = "miles";
    public static final String UNIT_KILOMETERS = "km";

    public String resolveForCountry(String countryCode) {
        return "US".equalsIgnoreCase(countryCode) ? UNIT_MILES : UNIT_KILOMETERS;
    }

    public String resolveForDeviceLocale() {
        return resolveForCountry(Locale.getDefault().getCountry());
    }

    public String formatChipLabel(int value, String unit) {
        if (UNIT_MILES.equals(unit)) {
            return value + " mi";
        }
        return value + " km";
    }
}
