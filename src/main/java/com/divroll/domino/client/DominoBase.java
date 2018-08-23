package com.divroll.domino.client;

import com.divroll.domino.client.exception.DominoException;
import com.dotweblabs.shape.client.HttpRequestException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Window;

public class DominoBase {

    public static final String HEADER_MASTER_KEY = "X-Domino-Master-Key";
    public static final String HEADER_APP_ID = "X-Domino-App-Id";
    public static final String HEADER_API_KEY = "X-Domino-Api-Key";
    public static final String HEADER_AUTH_TOKEN = "X-Domino-Auth-Token";

    public void throwException(HttpRequestException exception) {
        int code = exception.getCode();
        String statusInfo = exception.getMessage();
        throw new DominoException(statusInfo);
    }

    public StatusInfo checkStatus(String responseBody) {
        Window.alert(responseBody);
        JSONObject bodyObj = JSONParser.parseStrict(responseBody).isObject();
        if(bodyObj.get("org.restlet.engine.application.StatusInfo") != null
            && bodyObj.get("org.restlet.engine.application.StatusInfo").isObject() != null) {
            JSONObject statusInfo = bodyObj.get("org.restlet.engine.application.StatusInfo").isObject();
            int code = new Double(statusInfo.get("code").isNumber().doubleValue()).intValue();
            String message = statusInfo.get("description").isString().stringValue();
            return new StatusInfo(code, message);
        } else if(bodyObj.get("error") != null && bodyObj.get("error").isObject() != null) {
            JSONObject statusInfo = bodyObj.get("error").isObject();
            int code = new Double(statusInfo.get("code").isNumber().doubleValue()).intValue();
            String message = statusInfo.get("description").isString().stringValue();
            return new StatusInfo(code, message);
        }
        return null;
    }

}
