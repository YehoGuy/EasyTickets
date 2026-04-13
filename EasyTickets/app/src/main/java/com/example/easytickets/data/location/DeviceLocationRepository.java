package com.example.easytickets.data.location;

import android.annotation.SuppressLint;
import android.location.Location;

import com.example.easytickets.data.RepositoryCallback;
import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

/**
 * {@link LocationRepository} implementation backed by {@link FusedLocationProviderClient}.
 * It first requests a current fix with permission-level granularity and then falls back to the
 * last known location when a fresh result is unavailable.
 */
public class DeviceLocationRepository implements LocationRepository {

    private final FusedLocationProviderClient fusedLocationProviderClient;

    public DeviceLocationRepository(FusedLocationProviderClient fusedLocationProviderClient) {
        this.fusedLocationProviderClient = fusedLocationProviderClient;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void getCurrentLocation(RepositoryCallback<Location> callback) {
        CancellationTokenSource cancellationTokenSource = new CancellationTokenSource();
        CurrentLocationRequest request = new CurrentLocationRequest.Builder()
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .setDurationMillis(7000)
                .setMaxUpdateAgeMillis(30_000)
                .build();

        fusedLocationProviderClient.getCurrentLocation(request, cancellationTokenSource.getToken())
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onSuccess(location);
                        return;
                    }
                    loadLastLocation(callback);
                })
                .addOnFailureListener(exception -> loadLastLocation(callback));
    }

    @SuppressLint("MissingPermission")
    private void loadLastLocation(RepositoryCallback<Location> callback) {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onSuccess(location);
                    } else {
                        callback.onError("Couldn't determine your current location.");
                    }
                })
                .addOnFailureListener(exception -> callback.onError("Couldn't determine your current location."));
    }
}
