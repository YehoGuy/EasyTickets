package com.example.easytickets.data;

/**
 * Simple callback contract used by repositories to return either data or a user-facing error message
 * from asynchronous operations.
 *
 * @param <T> type returned on success
 */
public interface RepositoryCallback<T> {
    void onSuccess(T data);

    void onError(String message);
}
