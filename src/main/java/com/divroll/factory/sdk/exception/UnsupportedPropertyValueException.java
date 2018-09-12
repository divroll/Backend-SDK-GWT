package com.divroll.factory.sdk.exception;

public class UnsupportedPropertyValueException extends DivrollException {
    public UnsupportedPropertyValueException(String mesage) {
        super(mesage);
    }

    public UnsupportedPropertyValueException() {
        super("");
    }
}
