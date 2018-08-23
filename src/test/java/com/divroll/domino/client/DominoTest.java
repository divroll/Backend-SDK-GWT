package com.divroll.domino.client;

import com.dotweblabs.shape.client.HttpRequestException;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Window;
import org.junit.Assert;

public class DominoTest extends GWTTestCase {
    @Override
    public String getModuleName() {
        return "com.divroll.domino.sdk";
    }

    public void testInitDomino() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Assert.assertNotNull(testApplication.getAppId());
                Assert.assertNotNull(testApplication.getApiToken());
                Assert.assertNotNull(testApplication.getMasterKey());
                finishTest();
            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });
    }
}
