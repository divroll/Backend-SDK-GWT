package com.divroll.domino.client;

import com.divroll.domino.client.exception.BadRequestException;
import com.dotweblabs.shape.client.HttpRequestException;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Window;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class DominoRoleTest extends GWTTestCase {
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
                assertNotNull(testApplication.getAppId());
                assertNotNull(testApplication.getApiToken());
                assertNotNull(testApplication.getMasterKey());
                finishTest();
            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });
    }

    public void testCreateRolePublic() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken());
                DominoRole role = new DominoRole("Admin");
                role.setAcl(DominoACL.buildPublicReadWrite());

                role.create(new DominoCallback() {
                    @Override
                    public void success() {
                        assertNotNull(role.getEntityId());
                        assertEquals("Admin", role.getName());
                        assertTrue(role.getAcl().getPublicRead());
                        assertTrue(role.getAcl().getPublicWrite());
                        Window.alert("ROLE NAME: " + role.getName());
                        role.retrieve(new DominoCallback() {
                            @Override
                            public void success() {
                                assertNotNull(role.getEntityId());
                                assertEquals("Admin", role.getName());
                                assertTrue(role.getAcl().getPublicRead());
                                assertTrue(role.getAcl().getPublicWrite());
                                finishTest();
                            }
                            @Override
                            public void failure(HttpRequestException exception) {
                                fail();
                            }
                        });
                    }
                    @Override
                    public void failure(HttpRequestException exception) {
                        fail();
                    }
                });


            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });


    }

    public void testCreateRoleInvalidACLShouldThrowException() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken());
                DominoRole role = new DominoRole("Admin");
                DominoACL acl = DominoACL.build();
                acl.setAclWrite(Arrays.asList("")); // invalid
                acl.setAclRead(Arrays.asList(""));  // invalid
                role.setAcl(acl);
                role.create(new DominoCallback() {
                    @Override
                    public void success() {
                        Window.alert("ENTITY ID: " + role.getEntityId());
                        assertNotNull(role.getEntityId());
                        finishTest();
                    }
                    @Override
                    public void failure(HttpRequestException exception) {
                        assertEquals(400, exception.getCode());
                    }
                });
            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });
    }

    @Test
    public void testCreateRoleMasterKey() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken());
                DominoRole role = new DominoRole("Admin");
                role.setAcl(null);
                role.create(new DominoCallback() {
                    @Override
                    public void success() {
                        Window.alert("ENTITY ID: " + role.getEntityId());
                        assertNotNull(role.getEntityId());
                        assertEquals("Admin", role.getName());
                        assertFalse(role.getAcl().getPublicRead());
                        assertFalse(role.getAcl().getPublicWrite());
                        finishTest();
                    }
                    @Override
                    public void failure(HttpRequestException exception) {
                        fail();
                    }
                });
            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });


    }

    public void testCreateRoleMasterKeyOnlyShouldThrowException() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken());
                DominoRole role = new DominoRole("Admin");
                role.setAcl(null);
                role.create(new DominoCallback() {
                    @Override
                    public void success() {
                        assertNotNull(role.getEntityId());
                        assertEquals("Admin", role.getName());
                        assertFalse(role.getAcl().getPublicRead());
                        assertFalse(role.getAcl().getPublicWrite());
                        // This wil throw exception since the created Role has
                        // Master Key-only access
                        role.retrieve(new DominoCallback() {
                            @Override
                            public void success() {
                                fail();
                            }
                            @Override
                            public void failure(HttpRequestException exception) {
                                assertEquals(401, exception.getCode());
                            }
                        });
                    }
                    @Override
                    public void failure(HttpRequestException exception) {
                        fail();
                    }
                });


            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });


    }

    @Test
    public void testCreateRoleMasterKeyOnly() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken(), testApplication.getMasterKey());
                DominoRole role = new DominoRole("Admin");
                role.setAcl(DominoACL.buildMasterKeyOnly());
                role.create(new DominoCallback() {
                    @Override
                    public void success() {
                        assertNotNull(role.getEntityId());
                        assertEquals("Admin", role.getName());
                        assertFalse(role.getAcl().getPublicRead());
                        assertFalse(role.getAcl().getPublicWrite());

                        role.retrieve(new DominoCallback() {
                            @Override
                            public void success() {
                                assertNotNull(role.getEntityId());
                                assertNotNull(role.getName());
                                finishTest();
                            }

                            @Override
                            public void failure(HttpRequestException exception) {
                                fail();
                            }
                        });
                    }
                    @Override
                    public void failure(HttpRequestException exception) {
                        fail();
                    }
                });
            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });


    }

    public void testCreateRoleInvalidAppId() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize("WRONG", testApplication.getApiToken());
                DominoRole role = new DominoRole("Admin");
                role.create(new DominoCallback() {
                    @Override
                    public void success() {
                        fail();
                    }
                    @Override
                    public void failure(HttpRequestException exception) {
                        assertEquals(401, exception.getCode());
                    }
                });
            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });

    }

    public void testCreateRoleInvalidApiToken() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize(testApplication.getAppId(),"WRONG");
                DominoRole role = new DominoRole("Admin");
                role.create(new DominoCallback() {
                    @Override
                    public void success() {
                        fail();
                    }
                    @Override
                    public void failure(HttpRequestException exception) {
                        assertEquals(401, exception.getCode());
                    }
                });
            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });
    }

    @Test
    public void testCreateRoleInvalidMasterKey() {
        // TODO
    }

    public void testGetRoleInvalidAppId() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken());
                DominoRole role = new DominoRole("Admin");
                role.create(new DominoCallback() {
                    @Override
                    public void success() {
                        assertNotNull(role.getEntityId());
                        assertNotNull(role.getAcl());
                        assertNotNull(role.getName());

                        Domino.initialize("WRONG", testApplication.getApiToken());
                        role.retrieve(new DominoCallback() {
                            @Override
                            public void success() {
                                assertNull(role.getEntityId());
                                assertNull(role.getAcl());
                                assertNull(role.getName());
                            }
                            @Override
                            public void failure(HttpRequestException exception) {
                                assertEquals(401, exception.getCode());
                            }
                        });

                    }
                    @Override
                    public void failure(HttpRequestException exception) {
                        fail();
                    }
                });


            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });

    }

    public void testGetRoleInvalidApiToken() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken());
                DominoRole role = new DominoRole("Admin");
                role.create(new DominoCallback() {
                    @Override
                    public void success() {
                        assertNotNull(role.getEntityId());
                        assertNotNull(role.getAcl());
                        assertNotNull(role.getName());

                        Domino.initialize(testApplication.getAppId(), "WRONG");
                        role.retrieve(new DominoCallback() {
                            @Override
                            public void success() {
                                assertNull(role.getEntityId());
                                assertNull(role.getAcl());
                                assertNull(role.getName());
                                fail();
                            }

                            @Override
                            public void failure(HttpRequestException exception) {
                                assertEquals(401, exception.getCode());
                            }
                        });

                    }

                    @Override
                    public void failure(HttpRequestException exception) {
                        fail();
                    }
                });


            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });

    }

    @Test
    public void testGetRoleInvalidMasterKey() {
        // TODO
    }

    @Test
    public void testCreateAndGetRoleWithACLUsingAuthToken() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken());

                DominoUser dominoUser = new DominoUser();
                dominoUser.create("admin", "password", new DominoCallback() {
                    @Override
                    public void success() {
                        assertNotNull(dominoUser.getEntityId());

                        String userId = dominoUser.getEntityId();

                        DominoRole dominoRole = new DominoRole();
                        DominoACL dominoACL = DominoACL.build();
                        dominoACL.setPublicRead(true);
                        dominoACL.setAclWrite(Arrays.asList(userId));

                        dominoRole.setAcl(dominoACL);
                        dominoRole.setName("Admin");
                        dominoRole.create(new DominoCallback() {
                            @Override
                            public void success() {
                                assertNotNull(dominoRole.getEntityId());
                                assertTrue(dominoRole.getAcl().getAclWrite().contains("0-0"));
                                assertTrue(dominoRole.getAcl().getPublicRead());

                                dominoUser.login("admin", "password", new DominoCallback() {
                                    @Override
                                    public void success() {
                                        assertNotNull(dominoUser.getAuthToken());
                                        assertNotNull(Domino.getAuthToken());

                                        dominoRole.retrieve(new DominoCallback() {
                                            @Override
                                            public void success() {
                                                assertNotNull(dominoRole.getEntityId());
                                                assertTrue(dominoRole.getAcl().getAclWrite().contains("0-0"));
                                                assertTrue(dominoRole.getAcl().getPublicRead());
                                                finishTest();
                                            }
                                            @Override
                                            public void failure(HttpRequestException exception) {
                                                fail();
                                            }
                                        });
                                    }
                                    @Override
                                    public void failure(HttpRequestException exception) {
                                        fail();
                                    }
                                });
                            }
                            @Override
                            public void failure(HttpRequestException exception) {
                                fail();
                            }
                        });
                    }
                    @Override
                    public void failure(HttpRequestException exception) {
                        fail();
                    }
                });
            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });


    }

    public void testCreateAndGetRoleWithACLMissingAuthTokenShouldFail() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken());
                DominoUser dominoUser = new DominoUser();
                dominoUser.create("admin", "password", new DominoCallback() {
                    @Override
                    public void success() {
                        assertNotNull(dominoUser.getEntityId());

                        String userId = dominoUser.getEntityId();

                        DominoRole dominoRole = new DominoRole();
                        DominoACL dominoACL = DominoACL.build();
                        dominoACL.setAclRead(Arrays.asList(userId));
                        dominoACL.setAclWrite(Arrays.asList(userId));

                        dominoRole.setAcl(dominoACL);
                        dominoRole.setName("Admin");
                        dominoRole.create(new DominoCallback() {
                            @Override
                            public void success() {
                                assertNotNull(dominoRole.getEntityId());
                                assertTrue(dominoRole.getAcl().getAclWrite().contains("0-0"));
                                assertTrue(dominoRole.getAcl().getAclRead().contains("0-0"));

                                dominoRole.retrieve(new DominoCallback() {
                                    @Override
                                    public void success() {
                                        assertNotNull(dominoRole.getEntityId());
                                        assertTrue(dominoRole.getAcl().getAclWrite().contains("0-0"));
                                        assertTrue(dominoRole.getAcl().getAclRead().contains("0-0"));
                                        fail();
                                    }
                                    @Override
                                    public void failure(HttpRequestException exception) {
                                        assertEquals(401, exception.getCode());
                                    }
                                });

                            }

                            @Override
                            public void failure(HttpRequestException exception) {
                                fail();
                            }
                        });


                    }

                    @Override
                    public void failure(HttpRequestException exception) {
                        fail();
                    }
                });


            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });

    }

    @Test
    public void testUpdatePublicRoleMissingAuthToken() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken());
                DominoRole role = new DominoRole("Admin");
                role.setAcl(DominoACL.buildPublicReadWrite());
                role.create(new DominoCallback() {
                    @Override
                    public void success() {
                        String roleId = role.getEntityId();

                        assertNotNull(role.getEntityId());
                        assertEquals("Admin", role.getName());
                        assertTrue(role.getAcl().getPublicRead());
                        assertTrue(role.getAcl().getPublicWrite());

                        role.setName("Super Admin");
                        role.update(new DominoCallback() {
                            @Override
                            public void success() {
                                role.retrieve(new DominoCallback() {
                                    @Override
                                    public void success() {
                                        assertEquals("Super Admin", role.getName());
                                    }
                                    @Override
                                    public void failure(HttpRequestException exception) {
                                        fail();
                                    }
                                });
                            }
                            @Override
                            public void failure(HttpRequestException exception) {
                                fail();
                            }
                        });


                    }

                    @Override
                    public void failure(HttpRequestException exception) {
                        fail();
                    }
                });


            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });



    }

    public void testUpdateRoleWithACLMissingAuthTokenShouldFail() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken());

                DominoUser dominoUser = new DominoUser();
                dominoUser.create("admin", "password", new DominoCallback() {
                    @Override
                    public void success() {
                        assertNotNull(dominoUser.getEntityId());

                        String userId = dominoUser.getEntityId();

                        DominoRole dominoRole = new DominoRole();
                        DominoACL dominoACL = DominoACL.build();
                        dominoACL.setAclRead(Arrays.asList(userId));
                        dominoACL.setAclWrite(Arrays.asList(userId));

                        dominoRole.setAcl(dominoACL);
                        dominoRole.setName("Admin");
                        dominoRole.create(new DominoCallback() {
                            @Override
                            public void success() {
                                assertNotNull(dominoRole.getEntityId());
                                assertTrue(dominoRole.getAcl().getAclWrite().contains("0-0"));
                                assertTrue(dominoRole.getAcl().getAclRead().contains("0-0"));

                                dominoRole.setName("Super Admin");
                                dominoRole.update(new DominoCallback() {
                                    @Override
                                    public void success() {
                                        assertNotNull(dominoRole.getEntityId());
                                        assertTrue(dominoRole.getAcl().getAclWrite().contains("0-0"));
                                        assertTrue(dominoRole.getAcl().getAclRead().contains("0-0"));
                                        fail();
                                    }

                                    @Override
                                    public void failure(HttpRequestException exception) {
                                        assertEquals(401, exception.getCode());
                                    }
                                });


                            }

                            @Override
                            public void failure(HttpRequestException exception) {
                                fail();
                            }
                        });


                    }

                    @Override
                    public void failure(HttpRequestException exception) {
                        fail();
                    }
                });



            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });


    }
/*
    @Test
    public void testUpdatePublicRoleUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        assertNotNull(role.getEntityId());
        assertEquals("Admin", role.getName());
        assertTrue(role.getAcl().getPublicRead());
        assertTrue(role.getAcl().getPublicWrite());

        DominoUser dominoUser = new DominoUser();
        dominoUser.create("admin", "password");
        dominoUser.login("admin", "password");

        role.setName("Super Admin");
        role.update();

        role.retrieve();

        assertEquals("Super Admin", role.getName());
    }


    @Test
    public void testUpdatePublicRoleUsingMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        assertNotNull(role.getEntityId());
        assertEquals("Admin", role.getName());
        assertTrue(role.getAcl().getPublicRead());
        assertTrue(role.getAcl().getPublicWrite());

        assertTrue(role.getAcl().getPublicRead());
        assertTrue(role.getAcl().getPublicWrite());

        role.setName("Super Admin");
        role.update();

        role.retrieve();

        assertEquals("Super Admin", role.getName());
        assertTrue(role.getAcl().getPublicRead());
        assertTrue(role.getAcl().getPublicWrite());

    }

    @Test
    public void testUpdatePublicRoleChangeACLUsingMasterKey() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        assertNotNull(role.getEntityId());
        assertEquals("Admin", role.getName());
        assertTrue(role.getAcl().getPublicRead());
        assertTrue(role.getAcl().getPublicWrite());

        assertTrue(role.getAcl().getPublicRead());
        assertTrue(role.getAcl().getPublicWrite());

        role.setAcl(DominoACL.buildMasterKeyOnly());
        role.update();

        assertEquals("Admin", role.getName());
        assertFalse(role.getAcl().getPublicRead());
        assertFalse(role.getAcl().getPublicWrite());

        role.retrieve();

        assertEquals("Admin", role.getName());
        assertFalse(role.getAcl().getPublicRead());
        assertFalse(role.getAcl().getPublicWrite());
    }

    @Test
    public void testUpdatePublicRoleChangeACLUsingAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        assertNotNull(role.getEntityId());
        assertEquals("Admin", role.getName());
        assertTrue(role.getAcl().getPublicRead());
        assertTrue(role.getAcl().getPublicWrite());

        assertTrue(role.getAcl().getPublicRead());
        assertTrue(role.getAcl().getPublicWrite());

        role.setAcl(DominoACL.buildMasterKeyOnly());
        role.update();

        assertEquals("Admin", role.getName());
        assertFalse(role.getAcl().getPublicRead());
        assertFalse(role.getAcl().getPublicWrite());
    }

    @Test
    public void testCreateUserWithRole() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        role.create();

        String adminRoleId = role.getEntityId();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.getRoles().add(role);
        adminUser.create("admin", "password");
    }

    @Test
    public void testCreateUserWithRoles() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        role.create();

        DominoRole managerRole = new DominoRole("Manager");
        managerRole.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        managerRole.create();

        String adminRoleId = role.getEntityId();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.getRoles().add(role);
        adminUser.getRoles().add(managerRole);
        adminUser.create("admin", "password");
    }

    @Test(expected = BadRequestException.class)
    public void testCreateUserWithRolesThenUpdateRoleWithoutAuthTokenShouldFail() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        role.create();

        DominoRole managerRole = new DominoRole("Manager");
        managerRole.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        managerRole.create();

        String adminRoleId = role.getEntityId();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.getRoles().add(role);
        adminUser.getRoles().add(managerRole);
        adminUser.create("admin", "password");

        assertNotNull(adminUser.getEntityId());

        adminUser.setRoles(Arrays.asList(managerRole));
        adminUser.update();
    }


    @Test
    public void testCreateUserWithRolesThenUpdateRole() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        role.create();

        DominoRole managerRole = new DominoRole("Manager");
        managerRole.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        managerRole.create();

        String adminRoleId = role.getEntityId();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.getRoles().add(role);
        adminUser.getRoles().add(managerRole);
        adminUser.create("admin", "password");

        assertEquals(2, adminUser.getRoles().size());

        adminUser.login("admin", "password");

        assertNotNull(adminUser.getEntityId());

        adminUser.setRoles(Arrays.asList(managerRole));
        adminUser.update();

        assertEquals(1, adminUser.getRoles().size());
    }

    @Test
    public void testUpdateUserWithAuthTokenWithRole() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
        role.create();

        String adminRoleId = role.getEntityId();

        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.getRoles().add(role);
        adminUser.create("admin", "password");

        DominoUser dominoUser = new DominoUser();
        DominoACL userACL = new DominoACL();
        userACL.setPublicRead(true);
        userACL.setPublicWrite(false);
        userACL.setAclWrite(Arrays.asList(adminRoleId));
        dominoUser.setAcl(userACL);
        dominoUser.create("user", "password");

        assertNotNull(adminUser.getRoles());
        assertFalse(adminUser.getRoles().isEmpty());

        adminUser.login("admin", "password");
        String authToken = adminUser.getAuthToken();

        assertNotNull(authToken);
        assertNotNull(Domino.getAuthToken());

        System.out.println("Updating...");

        dominoUser.update("new_username", "new_password");
        assertEquals("new_username", dominoUser.getUsername());
    }

    @Test
    public void testDeletePublicRole() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        assertNotNull(role.getEntityId());

        role.delete();
    }

    @Test(expected = BadRequestException.class)
    public void testDeletePublicRoleThenRetrieveShouldFail() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());

        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        assertNotNull(role.getEntityId());

        role.delete();

        role.retrieve();

        assertNull(role.getEntityId());
    }

    @Test
    public void testDeletePublicRoleWithAuthToken() {
        TestApplication application = TestData.getNewApplication();
        Domino.initialize(application.getAppId(), application.getApiToken());


        DominoUser adminUser = new DominoUser();
        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
        adminUser.create("admin", "password");

        adminUser.login("admin", "password");
        DominoRole role = new DominoRole("Admin");
        role.setAcl(DominoACL.buildPublicReadWrite());
        role.create();

        assertNotNull(role.getEntityId());

        adminUser.login("admin", "password");

        role.delete();
    }
*/
    public void testDeletePublicRoleWithAuthTokenThenRetrieveShouldFail() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken());
                DominoUser adminUser = new DominoUser();
                adminUser.setAcl(DominoACL.buildMasterKeyOnly());
                adminUser.create("admin", "password", new DominoCallback() {
                    @Override
                    public void success() {
                        adminUser.login("admin", "password", new DominoCallback() {
                            @Override
                            public void success() {
                                DominoRole role = new DominoRole("Admin");
                                role.setAcl(DominoACL.buildPublicReadWrite());
                                role.create(new DominoCallback() {
                                    @Override
                                    public void success() {
                                        assertNotNull(role.getEntityId());

                                        adminUser.login("admin", "password", new DominoCallback() {
                                            @Override
                                            public void success() {
                                                role.delete(new DominoCallback() {
                                                    @Override
                                                    public void success() {
                                                        role.retrieve(new DominoCallback() {
                                                            @Override
                                                            public void success() {
                                                                assertNull(role.getEntityId());
                                                                finishTest();
                                                            }
                                                            @Override
                                                            public void failure(HttpRequestException exception) {
                                                                assertEquals(404, exception.getCode());
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void failure(HttpRequestException exception) {
                                                        fail();
                                                    }
                                                });


                                            }

                                            @Override
                                            public void failure(HttpRequestException exception) {
                                                fail();

                                            }
                                        });


                                    }

                                    @Override
                                    public void failure(HttpRequestException exception) {
                                        fail();

                                    }
                                });


                            }

                            @Override
                            public void failure(HttpRequestException exception) {
                                fail();
                            }
                        });

                    }

                    @Override
                    public void failure(HttpRequestException exception) {
                        fail();
                    }
                });


            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });



    }

    @Test
    public void testDeletePublicRoleWithMasterKey() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken(), testApplication.getMasterKey());
                DominoRole role = new DominoRole("Admin");
                role.setAcl(DominoACL.buildPublicReadWrite());
                role.create(new DominoCallback() {
                    @Override
                    public void success() {
                        assertNotNull(role.getEntityId());
                        role.delete(new DominoCallback() {
                            @Override
                            public void success() {
                                finishTest();
                            }

                            @Override
                            public void failure(HttpRequestException exception) {
                                fail();
                            }
                        });
                    }
                    @Override
                    public void failure(HttpRequestException exception) {
                        fail();
                    }
                });


            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });

    }

    public void testDeletePublicRoleWithMasterKeyTheRetrieveShouldFail() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken(), testApplication.getMasterKey());
                DominoRole role = new DominoRole("Admin");
                role.setAcl(DominoACL.buildPublicReadWrite());
                role.create(new DominoCallback() {
                    @Override
                    public void success() {
                        assertNotNull(role.getEntityId());

                        role.delete(new DominoCallback() {
                            @Override
                            public void success() {
                                role.retrieve(new DominoCallback() {
                                    @Override
                                    public void success() {
                                        assertNull(role.getEntityId());
                                        fail();
                                    }
                                    @Override
                                    public void failure(HttpRequestException exception) {
                                        assertEquals(404, exception.getCode());
                                    }
                                });

                            }
                            @Override
                            public void failure(HttpRequestException exception) {
                                fail();
                            }
                        });


                    }

                    @Override
                    public void failure(HttpRequestException exception) {
                        fail();
                    }
                });


            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });


    }

    public void testDeleteRoleWithACLWithAuthToken() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken());
                DominoRole role = new DominoRole("Admin");
                role.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
                role.create(new DominoCallback() {
                    @Override
                    public void success() {
                        String adminRoleId = role.getEntityId();

                        DominoUser adminUser = new DominoUser();
                        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
                        adminUser.getRoles().add(role);
                        adminUser.create("admin", "password", new DominoCallback() {
                            @Override
                            public void success() {
                                assertNotNull(adminUser.getRoles());
                                assertFalse(adminUser.getRoles().isEmpty());

                                adminUser.login("admin", "password", new DominoCallback() {
                                    @Override
                                    public void success() {
                                        String authToken = adminUser.getAuthToken();

                                        assertNotNull(authToken);
                                        assertNotNull(Domino.getAuthToken());

                                        role.delete(new DominoCallback() {
                                            @Override
                                            public void success() {
                                                fail();
                                            }
                                            @Override
                                            public void failure(HttpRequestException exception) {
                                                assertEquals(401, exception.getCode());
                                            }
                                        });
                                    }

                                    @Override
                                    public void failure(HttpRequestException exception) {
                                        fail();
                                    }
                                });
                            }
                            @Override
                            public void failure(HttpRequestException exception) {
                                fail();
                            }
                        });
                    }
                    @Override
                    public void failure(HttpRequestException exception) {
                        fail();
                    }
                });
            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });


    }

    public void testDeleteRoleWithACLWithoutAuthToken() {
        delayTestFinish(3000);
        TestData.getNewApplication(new DominoCallbackWithResponse<TestApplication>() {
            @Override
            public void success(TestApplication testApplication) {
                Window.alert("APP ID: " + testApplication.getAppId());
                Window.alert("API TOKEN: " + testApplication.getApiToken());
                Window.alert("MASTER KEY: " + testApplication.getMasterKey());
                Domino.initialize(testApplication.getAppId(), testApplication.getApiToken());
                DominoRole role = new DominoRole("Admin");
                role.setAcl(DominoACL.buildPublicReadMasterKeyWrite());
                role.create(new DominoCallback() {
                    @Override
                    public void success() {
                        DominoUser adminUser = new DominoUser();
                        adminUser.setAcl(DominoACL.buildMasterKeyOnly());
                        adminUser.getRoles().add(role);
                        adminUser.create("admin", "password", new DominoCallback() {
                            @Override
                            public void success() {
                                role.delete(new DominoCallback() {
                                    @Override
                                    public void success() {
                                        fail();
                                    }
                                    @Override
                                    public void failure(HttpRequestException exception) {
                                        assertEquals(401, exception.getCode());
                                    }
                                });
                            }

                            @Override
                            public void failure(HttpRequestException exception) {
                                fail();
                            }
                        });

                    }

                    @Override
                    public void failure(HttpRequestException exception) {
                        fail();
                    }
                });


            }
            @Override
            public void failure(HttpRequestException exception) {
                fail();
            }
        });


    }

}
