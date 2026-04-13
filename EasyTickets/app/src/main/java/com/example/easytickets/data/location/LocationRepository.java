package com.example.easytickets.data.location;

import android.location.Location;

import com.example.easytickets.data.RepositoryCallback;

/**
 * Abstraction for retrieving the device location so the home flow does not depend directly on
 * Google Play services APIs.
 */
public interface LocationRepository {

    void getCurrentLocation(RepositoryCallback<Location> callback);
}
