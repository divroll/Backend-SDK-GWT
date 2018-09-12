package com.divroll.roll;

import com.divroll.roll.exception.DivrollException;
import org.gwtproject.http.client.HttpResponse;
import org.gwtproject.http.client.JsonNode;
import org.json.JSONObject;

public abstract class DivrollBase {

    public static final String HEADER_MASTER_KEY = "X-Divroll-Master-Key";
    public static final String HEADER_APP_ID = "X-Divroll-App-Id";
    public static final String HEADER_API_KEY = "X-Divroll-Api-Key";
    public static final String HEADER_AUTH_TOKEN = "X-Divroll-Auth-Token";

    public void throwException(HttpResponse<JsonNode> response) throws DivrollException {
        JsonNode body = response.getBody();
        JSONObject jsonObject = body.getObject();
        JSONObject statusInfo = jsonObject.getJSONObject("org.restlet.engine.application.StatusInfo");
        throw new DivrollException(statusInfo.getString("description"));
    }

}
