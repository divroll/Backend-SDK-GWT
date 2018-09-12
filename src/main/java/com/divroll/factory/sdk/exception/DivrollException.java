package com.divroll.factory.sdk.exception;

import com.google.gwt.http.client.RequestException;

public class DivrollException extends RequestException {
    public DivrollException(String mesage) {
        super(mesage);
    }

    public DivrollException() {
    }
}
