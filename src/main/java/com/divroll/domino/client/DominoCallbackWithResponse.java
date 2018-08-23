package com.divroll.domino.client;

import com.dotweblabs.shape.client.HttpRequestException;

public interface DominoCallbackWithResponse<T> {
    public void success(T t);
    public void failure(HttpRequestException exception);
}
