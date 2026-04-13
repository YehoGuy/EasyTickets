package com.example.easytickets.data.location;

import android.location.Location;

import com.example.easytickets.data.RepositoryCallback;

public interface LocationRepository {

    void getCurrentLocation(RepositoryCallback<Location> callback);
}
