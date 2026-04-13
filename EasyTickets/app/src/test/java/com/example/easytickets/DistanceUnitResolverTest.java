package com.example.easytickets;

import static org.junit.Assert.assertEquals;

import com.example.easytickets.util.DistanceUnitResolver;

import org.junit.Test;

/**
 * Unit tests for {@link DistanceUnitResolver} covering country-based unit selection and
 * chip label formatting.
 */
public class DistanceUnitResolverTest {

    private final DistanceUnitResolver distanceUnitResolver = new DistanceUnitResolver();

    @Test
    public void resolveForCountry_returnsMilesForUnitedStates() {
        assertEquals(DistanceUnitResolver.UNIT_MILES, distanceUnitResolver.resolveForCountry("US"));
    }

    @Test
    public void resolveForCountry_returnsKilometersForNonUsMarkets() {
        assertEquals(DistanceUnitResolver.UNIT_KILOMETERS, distanceUnitResolver.resolveForCountry("CA"));
    }

    @Test
    public void formatChipLabel_usesReadableSuffixes() {
        assertEquals("25 mi", distanceUnitResolver.formatChipLabel(25, DistanceUnitResolver.UNIT_MILES));
        assertEquals("25 km", distanceUnitResolver.formatChipLabel(25, DistanceUnitResolver.UNIT_KILOMETERS));
    }
}
