package com.divroll.domino.client;

public class StatusInfo {
    private Integer code;
    private String message;

    public StatusInfo() {}

    public StatusInfo(Integer code, String message) {
        setCode(code);
        setMessage(message);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
