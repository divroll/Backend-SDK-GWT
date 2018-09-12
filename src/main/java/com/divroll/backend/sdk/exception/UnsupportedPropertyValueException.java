package com.divroll.backend.sdk.exception;

public class UnsupportedPropertyValueException extends DivrollException {
    public UnsupportedPropertyValueException(String mesage) {
        super(mesage);
    }

    public UnsupportedPropertyValueException() {
        super("");
    }
}
