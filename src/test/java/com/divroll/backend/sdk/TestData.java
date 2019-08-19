/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018 to present, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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

    static {
        String serverUrl = "https://divroll-backend.herokuapp.com/divroll";
//        String serverUrl = "http://localhost:8080/divroll";
        setDivrollServerUrl(serverUrl);
    }

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

    public static native void setDivrollServerUrl(String serverUrl) /*-{
        $wnd.divrollServerUrl = serverUrl;
    }-*/;

}
