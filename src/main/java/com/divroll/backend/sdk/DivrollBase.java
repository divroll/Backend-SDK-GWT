package com.divroll.backend.sdk;

import com.divroll.backend.sdk.exception.DivrollException;
import com.divroll.backend.sdk.helper.DivrollEntityHelper;
import com.divroll.http.client.HttpResponse;
import com.divroll.http.client.JsonNode;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public abstract class DivrollBase {

    public static final String HEADER_MASTER_KEY = "X-Divroll-Master-Key";
    public static final String HEADER_APP_ID = "X-Divroll-App-Id";
    public static final String HEADER_API_KEY = "X-Divroll-Api-Key";
    public static final String HEADER_AUTH_TOKEN = "X-Divroll-Auth-Token";
    public static final String HEADER_NAMESPACE = "X-Divroll-Namespace";

    public void throwException(HttpResponse<JsonNode> response) throws DivrollException {
        JsonNode body = response.getBody();
        JSONObject jsonObject = body.getObject();
        JSONObject statusInfo = jsonObject.getJSONObject("org.restlet.engine.application.StatusInfo");
        throw new DivrollException(statusInfo.getString("description"));
    }



}
