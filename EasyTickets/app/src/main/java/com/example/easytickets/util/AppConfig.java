package com.example.easytickets.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates API key configuration read from {@code BuildConfig}.
 * It exposes validation helpers and builds the setup instructions shown when required keys are missing.
 */
public class AppConfig {

    private final String googleMapsApiKey;
    private final String ticketmasterApiKey;

    public AppConfig(String googleMapsApiKey, String ticketmasterApiKey) {
        this.googleMapsApiKey = sanitize(googleMapsApiKey);
        this.ticketmasterApiKey = sanitize(ticketmasterApiKey);
    }

    public String getGoogleMapsApiKey() {
        return googleMapsApiKey;
    }

    public String getTicketmasterApiKey() {
        return ticketmasterApiKey;
    }

    public boolean hasGoogleMapsKey() {
        return isConfigured(googleMapsApiKey);
    }

    public boolean hasTicketmasterKey() {
        return isConfigured(ticketmasterApiKey);
    }

    public boolean hasRequiredKeys() {
        return hasGoogleMapsKey() && hasTicketmasterKey();
    }

    public String buildSetupInstructions() {
        StringBuilder builder = new StringBuilder();
        builder.append("EasyTickets needs API keys before it can run.\n\n");
        builder.append("Create or update the file `secrets.properties` in the project root with:\n\n");
        builder.append("GOOGLE_MAPS_API_KEY=YOUR_GOOGLE_KEY\n");
        builder.append("TICKETMASTER_API_KEY=YOUR_TICKETMASTER_KEY\n\n");
        builder.append("Missing right now:\n");
        for (String key : getMissingKeys()) {
            builder.append("- ").append(key).append('\n');
        }
        builder.append("\nGoogle Maps key must have Maps SDK for Android and Places API enabled.");
        return builder.toString();
    }

    private List<String> getMissingKeys() {
        List<String> missingKeys = new ArrayList<>();
        if (!hasGoogleMapsKey()) {
            missingKeys.add("GOOGLE_MAPS_API_KEY");
        }
        if (!hasTicketmasterKey()) {
            missingKeys.add("TICKETMASTER_API_KEY");
        }
        return missingKeys;
    }

    private static boolean isConfigured(String value) {
        return !value.isEmpty() && !value.startsWith("DEFAULT_") && !value.startsWith("YOUR_");
    }

    private static String sanitize(String value) {
        return value == null ? "" : value.trim();
    }
}
