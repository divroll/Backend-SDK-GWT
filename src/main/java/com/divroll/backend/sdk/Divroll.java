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

import com.google.gwt.user.client.Cookies;

public class Divroll {

    private static String divrollServerUrl;
    private static String applicationId;
    private static String applicationKey;
    private static String applicationMasterKey;
    private static String authenticationToken;
    private static String currentNamespace;
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

    public static void setNamespace(String namespace) {
        currentNamespace = namespace;
    }

    public static String getNamespace() {
        return currentNamespace;
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
