package com.divroll.backend.sdk;

import com.google.gwt.user.client.Cookies;

public class Divroll {

    private static String divrollServerUrl;
    private static String applicationId;
    private static String applicationKey;
    private static String applicationMasterKey;
    private static String authenticationToken;
    private static DivrollUser currentUser = null;

    private Divroll() {}

    public static void initialize(String applicationId, String apiKey) {
        Divroll.applicationId = applicationId;
        applicationKey = apiKey;
    }

    public static void initialize(String appId, String apiKey, String masterKey) {
        applicationId = appId;
        applicationKey = apiKey;
        applicationMasterKey = masterKey;
    }

    public static String getServerUrl() {
        if(divrollServerUrl == null) {
            divrollServerUrl = getDivrollServerUrlFromJs();
            if(divrollServerUrl == null) {
                divrollServerUrl = "http://localhost:8080/divroll";
            }
        }
        return divrollServerUrl;
    }

    public static void initialize(String serverUrl, String appId, String apiKey, String masterKey) {
        applicationId = appId;
        applicationKey = apiKey;
        applicationMasterKey = masterKey;
        divrollServerUrl = serverUrl;
    }

    public static String getAppId() {
        return applicationId;
    }

    public static String getApiKey() {
        return applicationKey;
    }

    public static String getMasterKey() {
        return applicationMasterKey;
    }

    public static String getAuthToken() {
        return authenticationToken;
    }

    public static void setAuthToken(String authToken) {
        authenticationToken = authToken;
    }

    private static native String getDivrollServerUrlFromJs() /*-{
        return $wnd.divrollServerUrl;
    }-*/;

    public static DivrollUser getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(DivrollUser currentUser) {
        Divroll.currentUser = currentUser;
    }
}
