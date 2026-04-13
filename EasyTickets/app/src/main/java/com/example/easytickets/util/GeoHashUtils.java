package com.example.easytickets.util;

/**
 * Minimal geohash encoder used to translate latitude and longitude into Ticketmaster's
 * {@code geoPoint} query format.
 */
public final class GeoHashUtils {

    private static final String BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

    private GeoHashUtils() {
    }

    public static String encode(double latitude, double longitude, int precision) {
        double[] latRange = {-90.0, 90.0};
        double[] lngRange = {-180.0, 180.0};
        StringBuilder geohash = new StringBuilder();
        boolean isEven = true;
        int bit = 0;
        int ch = 0;

        while (geohash.length() < precision) {
            double mid;
            if (isEven) {
                mid = (lngRange[0] + lngRange[1]) / 2D;
                if (longitude >= mid) {
                    ch |= 1 << (4 - bit);
                    lngRange[0] = mid;
                } else {
                    lngRange[1] = mid;
                }
            } else {
                mid = (latRange[0] + latRange[1]) / 2D;
                if (latitude >= mid) {
                    ch |= 1 << (4 - bit);
                    latRange[0] = mid;
                } else {
                    latRange[1] = mid;
                }
            }

            isEven = !isEven;
            if (bit < 4) {
                bit++;
            } else {
                geohash.append(BASE32.charAt(ch));
                bit = 0;
                ch = 0;
            }
        }
        return geohash.toString();
    }
}
