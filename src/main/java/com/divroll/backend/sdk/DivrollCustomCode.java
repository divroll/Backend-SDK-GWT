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

import com.divroll.http.client.*;
import io.reactivex.Single;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class DivrollCustomCode extends DivrollBase {

    private String functionName;
    private String methodName;
    private JSONObject body;
    private Map<String,String> params;

    private DivrollCustomCode() {}

    public DivrollCustomCode(String functionName, String methodName) {
        setFunctionName(functionName);
        setMethodName(methodName);
    }

    public Single<HttpResponse<JsonNode>> post() {

        HttpRequestWithBody httpRequestWithBody = HttpClient.post(Divroll.getServerUrl()
                + "/functions" + "/" + functionName + "/" + methodName);

        if(Divroll.getMasterKey() != null) {
            httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
        }
        if(Divroll.getAppId() != null) {
            httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
        }
        if(Divroll.getApiKey() != null) {
            httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
        }
        if(Divroll.getAuthToken() != null) {
            httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
        }
        if(Divroll.getNamespace() != null) {
//            httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }

        if(params != null) {
            Iterator<Map.Entry<String,String>> it = params.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<String,String> entry = it.next();
                httpRequestWithBody.queryString(entry.getKey(), entry.getValue());
            }
        }

        if(body != null) {
            return httpRequestWithBody.body(body.toString()).asJson().map(response -> {
                return response;
            });
        } else {
            return httpRequestWithBody.asJson().map(response -> {
                return response;
            });
        }

    }

    public Single<HttpResponse<JsonNode>> put() {

        HttpRequestWithBody httpRequestWithBody = HttpClient.post(Divroll.getServerUrl()
                + "/functions" + "/" + functionName + "/" + methodName);

        if(Divroll.getMasterKey() != null) {
            httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
        }
        if(Divroll.getAppId() != null) {
            httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
        }
        if(Divroll.getApiKey() != null) {
            httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
        }
        if(Divroll.getAuthToken() != null) {
            httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
        }
        if(Divroll.getNamespace() != null) {
//            httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }

        if(params != null) {
            Iterator<Map.Entry<String,String>> it = params.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<String,String> entry = it.next();
                httpRequestWithBody.queryString(entry.getKey(), entry.getValue());
            }
        }

        if(body != null) {
            return httpRequestWithBody.body(body.toString()).asJson().map(response -> {
                return response;
            });
        } else {
            return httpRequestWithBody.asJson().map(response -> {
                return response;
            });
        }

    }

    public Single<HttpResponse<JsonNode>> delete() {

        HttpRequestWithBody httpRequestWithBody = HttpClient.delete(Divroll.getServerUrl()
                + "/functions" + "/" + functionName + "/" + methodName);

        if(Divroll.getMasterKey() != null) {
            httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
        }
        if(Divroll.getAppId() != null) {
            httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
        }
        if(Divroll.getApiKey() != null) {
            httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
        }
        if(Divroll.getAuthToken() != null) {
            httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
        }
        if(Divroll.getNamespace() != null) {
//            httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }

        if(params != null) {
            Iterator<Map.Entry<String,String>> it = params.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<String,String> entry = it.next();
                httpRequestWithBody.queryString(entry.getKey(), entry.getValue());
            }
        }

        if(body != null) {
            return httpRequestWithBody.body(body.toString()).asJson().map(response -> {
                return response;
            });
        } else {
            return httpRequestWithBody.asJson().map(response -> {
                return response;
            });
        }

    }

    public Single<HttpResponse<JsonNode>> get() {

        GetRequest httpRequestWithBody = HttpClient.get(Divroll.getServerUrl()
                + "/functions" + "/" + functionName + "/" + methodName);

        if(Divroll.getMasterKey() != null) {
            httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
        }
        if(Divroll.getAppId() != null) {
            httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
        }
        if(Divroll.getApiKey() != null) {
            httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
        }
        if(Divroll.getAuthToken() != null) {
            httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
        }
        if(Divroll.getNamespace() != null) {
            httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }

        if(params != null) {
            Iterator<Map.Entry<String,String>> it = params.entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<String,String> entry = it.next();
                httpRequestWithBody.queryString(entry.getKey(), entry.getValue());
            }
        }

        return httpRequestWithBody.asJson().map(response -> {
            return response;
        });

    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public void setBody(JSONObject body) {
        this.body = body;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getParams() {
        if(params == null) {
            params = new LinkedHashMap<>();
        }
        return params;
    }
}
