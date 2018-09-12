package com.divroll.factory.sdk;


import com.google.gwt.user.client.Window;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;

public class TestHelper {
    public static void expected(Class<? extends Throwable> expectedClass, Testable testable) {
        try {
            testable.test();
            fail("Expected "+ expectedClass.getCanonicalName() +" not thrown.");
        } catch (Throwable actual) {
            Window.alert(actual.getMessage());
            assertEquals("Expected "+ expectedClass.getCanonicalName() +" to be thrown.", expectedClass, actual.getClass());
        }
    }

    interface Testable {
        public void test() throws Throwable;
    }
}
