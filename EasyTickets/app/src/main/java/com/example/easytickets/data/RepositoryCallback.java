package com.example.easytickets.data;

public interface RepositoryCallback<T> {
    void onSuccess(T data);

    void onError(String message);
}
