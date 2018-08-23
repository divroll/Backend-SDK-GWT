package com.divroll.domino.client;

import com.dotweblabs.shape.client.GetRequest;
import com.dotweblabs.shape.client.HttpRequestException;
import com.dotweblabs.shape.client.Shape;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class TestData {

    public static void getNewApplication(DominoCallbackWithResponse<TestApplication> callback) {
        try {
            GetRequest getRequest = (GetRequest) Shape.get(Domino.getServerUrl() + "/applications");
            if(Domino.getAppId() != null) {
                getRequest.header(DominoBase.HEADER_APP_ID, Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                getRequest.header(DominoBase.HEADER_API_KEY, Domino.getApiKey());
            }
            getRequest.asJson(new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable throwable) {
                    callback.failure((HttpRequestException) throwable);
                }
                @Override
                public void onSuccess(String body) {
                    JSONObject bodyObj = JSONParser.parseStrict(body).isObject();
                    JSONObject application = bodyObj.get("application").isObject();
                    TestApplication testApplication = new TestApplication(application.get("appId").isString().stringValue(),
                            application.get("apiKey").isString().stringValue(),
                            application.get("masterKey").isString().stringValue());
                    callback.success(testApplication);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
