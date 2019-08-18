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

import com.divroll.http.client.GetRequest;
import com.divroll.http.client.HttpClient;
import com.divroll.http.client.HttpResponse;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Window;
import elemental.client.Browser;
import io.reactivex.Single;
import com.divroll.backend.sdk.helper.Pair;

/**
 *
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class DivrollEntityTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "com.divroll.backend.sdk";
    }

    public void test() {
        Window.alert("Sample Test");
    }

    public void testCreateBlob() throws RequestException {
        TestData.getNewApplication().flatMap(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken(), testApplication.getMasterKey());
            assertNotNull(testApplication.getApiToken());
            assertNotNull(testApplication.getAppId());
            assertNotNull(testApplication.getMasterKey());
            DivrollEntity entity = new DivrollEntity("Todo");
            return entity.create();
        }).flatMap(divrollEntity -> divrollEntity.setBlobProperty("text", "Hello world!".getBytes("UTF-8"))).subscribe(success -> {
            Browser.getWindow().getConsole().log("Blob created");
            finishTest();
        }, error -> {
            Browser.getWindow().getConsole().error(error.getMessage());
            fail();
        });
        delayTestFinish(5000);
    }

    public void testGetBlob() throws RequestException {
        TestData.getNewApplication().flatMap(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken(), testApplication.getMasterKey());
            assertNotNull(testApplication.getApiToken());
            assertNotNull(testApplication.getAppId());
            assertNotNull(testApplication.getMasterKey());
            DivrollEntity entity = new DivrollEntity("Todo");
            entity.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
            return entity.create();
        }).flatMap(divrollEntity -> divrollEntity.setBlobProperty("text", "Hello world!".getBytes("UTF-8"))
                .flatMap(isSuccess -> Single.just(new Pair<DivrollEntity, Boolean>(divrollEntity, isSuccess))))
                .flatMap(pair -> {
                    if(pair.second) {
                        DivrollEntity entity = pair.first;
                        String blobPath = entity.getBlobPath("text");
                        Browser.getWindow().getConsole().log("Blob path - " + blobPath);
                        String completeUrl = Divroll.getServerUrl() + "/blobs/" + blobPath;
                        Browser.getWindow().getConsole().log("Complete URL - " + completeUrl);
                        GetRequest getRequest = HttpClient.get(completeUrl);
                        return getRequest.asString();
                    } else {
                        Boolean success = false;
                        return Single.just(success);
                    }
                }).subscribe(response -> {
                    if(response instanceof Boolean) {
                        Browser.getWindow().getConsole().error("Create blob failed");
                        fail();
                    } else if(response instanceof HttpResponse) {
                        Browser.getWindow().getConsole().info("Create blob success");
                        HttpResponse<String> httpResponse = (HttpResponse<String>) response;
                        Browser.getWindow().getConsole().info("Response - " + httpResponse.getBody());
                        finishTest();
                    }
                }, error -> {
                    fail();
                });
        delayTestFinish(5000);
    }

}
