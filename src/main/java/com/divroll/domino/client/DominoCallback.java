package com.divroll.domino.client;

import com.dotweblabs.shape.client.HttpRequestException;

public interface DominoCallback {
    public void success();
    public void failure(HttpRequestException exception);
}
