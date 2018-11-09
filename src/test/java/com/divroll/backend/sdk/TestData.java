package com.divroll.backend.sdk;

import com.divroll.http.client.HttpRequestWithBody;
import com.google.gwt.http.client.RequestException;
import io.reactivex.Single;
import com.divroll.http.client.GetRequest;
import com.divroll.http.client.HttpClient;
import com.divroll.http.client.JsonNode;
import com.divroll.http.client.exceptions.NotFoundRequestException;
import com.divroll.http.client.exceptions.UnauthorizedRequestException;
import org.json.JSONObject;

public class TestData {

    public static Single<TestApplication> getNewApplication() throws RequestException {
        DataFactory df = new DataFactory();
        HttpRequestWithBody httpRequest = (HttpRequestWithBody) HttpClient.post(
                Divroll.getServerUrl() + "/applications/" + df.getName());
        if(Divroll.getAppId() != null) {
            httpRequest.header(DivrollBase.HEADER_APP_ID, Divroll.getAppId());
        }
        if(Divroll.getApiKey() != null) {
            httpRequest.header(DivrollBase.HEADER_API_KEY, Divroll.getApiKey());
        }

        JSONObject userObject = new JSONObject();
        userObject.put("username", df.getEmailAddress());
        userObject.put("password", "password");
        userObject.put("role", "role");
        JSONObject payload = new JSONObject();
        JSONObject applicationObj = new JSONObject();
        payload.put("application", applicationObj);
        httpRequest.body(payload.toString());

        return httpRequest.asJson().map(response -> {
            if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 200 || response.getStatus() == 201) {
                JsonNode body = response.getBody();
                JSONObject bodyObj = body.getObject();
                JSONObject application = bodyObj.getJSONObject("application");
                return new TestApplication(application.getString("appId"),
                        application.getString("apiKey"),
                        application.getString("masterKey"));
            }
            return null;
        });
    }

}
