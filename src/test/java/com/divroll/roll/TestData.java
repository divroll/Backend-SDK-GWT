package com.divroll.roll;

import com.google.gwt.http.client.RequestException;
import com.google.gwt.user.client.Window;
import io.reactivex.Single;
import org.gwtproject.http.client.GetRequest;
import org.gwtproject.http.client.HttpClient;
import org.gwtproject.http.client.JsonNode;
import org.gwtproject.http.client.exceptions.NotFoundRequestException;
import org.gwtproject.http.client.exceptions.UnauthorizedRequestException;
import org.json.JSONObject;

public class TestData {

    public static Single<TestApplication> getNewApplication() throws RequestException {
        DataFactory df = new DataFactory();
        GetRequest getRequest = (GetRequest) HttpClient.get(
                Divroll.getServerUrl() + "/applications/" + df.getName());
        if(Divroll.getAppId() != null) {
            getRequest.header(DivrollBase.HEADER_APP_ID, Divroll.getAppId());
        }
        if(Divroll.getApiKey() != null) {
            getRequest.header(DivrollBase.HEADER_API_KEY, Divroll.getApiKey());
        }
        return getRequest.asJson().map(response -> {
            if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 200) {
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
