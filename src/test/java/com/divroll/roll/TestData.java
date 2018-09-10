package com.divroll.roll;

import com.google.gwt.http.client.RequestException;
import org.gwtproject.http.client.GetRequest;
import org.gwtproject.http.client.HttpClient;
import org.gwtproject.http.client.HttpResponse;
import org.gwtproject.http.client.JsonNode;
import org.json.JSONObject;

public class TestData {

    public static TestApplication getNewApplication() {
        try {
            DataFactory df = new DataFactory();
            GetRequest getRequest = (GetRequest) HttpClient.get(
                    Divroll.getServerUrl() + "/applications/" + df.getName());
            if(Divroll.getAppId() != null) {
                getRequest.header(DivrollBase.HEADER_APP_ID, Divroll.getAppId());
            }
            if(Divroll.getApiKey() != null) {
                getRequest.header(DivrollBase.HEADER_API_KEY, Divroll.getApiKey());
            }
            HttpResponse<JsonNode> response = getRequest.asJson();
            if(response.getStatus() == 404) {

            } else if(response.getStatus() == 401) {

            } else if(response.getStatus() == 200) {
                JsonNode body = response.getBody();
                JSONObject bodyObj = body.getObject();
                JSONObject application = bodyObj.getJSONObject("application");
                return new TestApplication(application.getString("appId"),
                        application.getString("apiKey"),
                        application.getString("masterKey"));
            }
        } catch (RequestException e) {
            e.printStackTrace();
        }
        return null;
    }

}
