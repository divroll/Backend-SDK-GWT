package com.divroll.roll;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Window;

/**
 *
 * @author <a href="mailto:kerby@dotweblabs.com">Kerby Martino</a>
 * @version 1.0
 * @since 1.0
 */
public class TestDivroll extends GWTTestCase {
    @Override
    public String getModuleName() {
        return "com.divroll.sdk";
    }
    public void test() {
        Window.alert("Sample Test");
    }
}
