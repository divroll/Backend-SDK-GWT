package com.divroll.roll;

import com.google.gwt.http.client.RequestException;
import com.google.gwt.junit.client.GWTTestCase;
import org.gwtproject.http.client.exceptions.BadRequestException;
import org.gwtproject.http.client.exceptions.UnauthorizedRequestException;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.Arrays;

import static com.divroll.roll.TestHelper.expected;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDivrollRole extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "com.divroll.sdk";
    }

    public void testCreateRolePublic() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());
        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();
        assertNotNull(role.getEntityId());
        assertEquals("Admin", role.getName());
        assertTrue(role.getAcl().getPublicRead());
        assertTrue(role.getAcl().getPublicWrite());

        role.retrieve();
    }

    public void testCreateRoleInvalidACLShouldThrowException() throws RequestException {
        expected(BadRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollRole role = new DivrollRole("Admin");
                DivrollACL acl = DivrollACL.build();
                acl.setAclWrite(Arrays.asList("")); // invalid
                acl.setAclRead(Arrays.asList(""));  // invalid
                role.setAcl(acl);
                role.create();
                assertNotNull(role.getEntityId());
            }
        });
    }

    public void testCreateRoleMasterKey() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(null);
        role.create();

        assertNotNull(role.getEntityId());
        assertEquals("Admin", role.getName());
        assertFalse(role.getAcl().getPublicRead());
        assertFalse(role.getAcl().getPublicWrite());
    }

    public void testCreateRoleMasterKeyOnlyShouldThrowException() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollRole role = new DivrollRole("Admin");
                role.setAcl(null);
                role.create();

                assertNotNull(role.getEntityId());
                assertEquals("Admin", role.getName());
                assertFalse(role.getAcl().getPublicRead());
                assertFalse(role.getAcl().getPublicWrite());

                // This wil throw exception since the created Role has
                // Master Key-only access
                role.retrieve();
            }
        });
    }

    public void testCreateRoleMasterKeyOnly() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildMasterKeyOnly());
        role.create();

        assertNotNull(role.getEntityId());
        assertEquals("Admin", role.getName());
        assertFalse(role.getAcl().getPublicRead());
        assertFalse(role.getAcl().getPublicWrite());

        role.retrieve();

        assertNotNull(role.getEntityId());
        assertNotNull(role.getName());
    }

    public void testCreateRoleInvalidAppId() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize("WRONG", application.getApiToken());
                DivrollRole role = new DivrollRole("Admin");
                role.create();
            }
        });
    }

    public void testCreateRoleInvalidApiToken() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), "WRONG");
                DivrollRole role = new DivrollRole("Admin");
                role.create();
            }
        });
    }

    public void testCreateRoleInvalidMasterKey() {
        // TODO
    }

    public void testGetRoleInvalidAppId() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());
                DivrollRole role = new DivrollRole("Admin");
                role.create();

                assertNotNull(role.getEntityId());
                assertNotNull(role.getAcl());
                assertNotNull(role.getName());

                Divroll.initialize("WRONG", application.getApiToken());
                role.retrieve();
                assertNull(role.getEntityId());
                assertNull(role.getAcl());
                assertNull(role.getName());
            }
        });
    }

    public void testGetRoleInvalidApiToken() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());
                DivrollRole role = new DivrollRole("Admin");
                role.create();

                assertNotNull(role.getEntityId());
                assertNotNull(role.getAcl());
                assertNotNull(role.getName());

                Divroll.initialize(application.getAppId(), "WRONG");
                role.retrieve();
                assertNull(role.getEntityId());
                assertNull(role.getAcl());
                assertNull(role.getName());
            }
        });
    }

    public void testGetRoleInvalidMasterKey() throws RequestException {
        // TODO
    }

    public void testCreateAndGetRoleWithACLUsingAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.create("admin", "password");

        assertNotNull(divrollUser.getEntityId());

        String userId = divrollUser.getEntityId();

        DivrollRole divrollRole = new DivrollRole();
        DivrollACL divrollACL = DivrollACL.build();
        divrollACL.setPublicRead(true);
        divrollACL.setAclWrite(Arrays.asList(userId));

        divrollRole.setAcl(divrollACL);
        divrollRole.setName("Admin");
        divrollRole.create();

        assertNotNull(divrollRole.getEntityId());
        assertTrue(divrollRole.getAcl().getAclWrite().contains("0-0"));
        assertTrue(divrollRole.getAcl().getPublicRead());

        divrollUser.login("admin", "password");
        assertNotNull(divrollUser.getAuthToken());
        assertNotNull(Divroll.getAuthToken());

        divrollRole.retrieve();
//
        assertNotNull(divrollRole.getEntityId());
        assertTrue(divrollRole.getAcl().getAclWrite().contains("0-0"));
        assertTrue(divrollRole.getAcl().getPublicRead());
    }

    public void testCreateAndGetRoleWithACLMissingAuthTokenShouldFail() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollUser divrollUser = new DivrollUser();
                divrollUser.create("admin", "password");

                assertNotNull(divrollUser.getEntityId());

                String userId = divrollUser.getEntityId();

                DivrollRole divrollRole = new DivrollRole();
                DivrollACL divrollACL = DivrollACL.build();
                divrollACL.setAclRead(Arrays.asList(userId));
                divrollACL.setAclWrite(Arrays.asList(userId));

                divrollRole.setAcl(divrollACL);
                divrollRole.setName("Admin");
                divrollRole.create();

                assertNotNull(divrollRole.getEntityId());
                assertTrue(divrollRole.getAcl().getAclWrite().contains("0-0"));
                assertTrue(divrollRole.getAcl().getAclRead().contains("0-0"));

                divrollRole.retrieve();

                assertNotNull(divrollRole.getEntityId());
                assertTrue(divrollRole.getAcl().getAclWrite().contains("0-0"));
                assertTrue(divrollRole.getAcl().getAclRead().contains("0-0"));
            }
        });

    }

    public void testUpdatePublicRoleMissingAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        assertNotNull(role.getEntityId());
        assertEquals("Admin", role.getName());
        assertTrue(role.getAcl().getPublicRead());
        assertTrue(role.getAcl().getPublicWrite());

        role.setName("Super Admin");
        role.update();

        role.retrieve();

        assertEquals("Super Admin", role.getName());
    }

    public void testUpdateRoleWithACLMissingAuthTokenShouldFail() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollUser divrollUser = new DivrollUser();
                divrollUser.create("admin", "password");

                assertNotNull(divrollUser.getEntityId());

                String userId = divrollUser.getEntityId();

                DivrollRole divrollRole = new DivrollRole();
                DivrollACL divrollACL = DivrollACL.build();
                divrollACL.setAclRead(Arrays.asList(userId));
                divrollACL.setAclWrite(Arrays.asList(userId));

                divrollRole.setAcl(divrollACL);
                divrollRole.setName("Admin");
                divrollRole.create();

                assertNotNull(divrollRole.getEntityId());
                assertTrue(divrollRole.getAcl().getAclWrite().contains("0-0"));
                assertTrue(divrollRole.getAcl().getAclRead().contains("0-0"));

                divrollRole.setName("Super Admin");
                divrollRole.update();

                assertNotNull(divrollRole.getEntityId());
                assertTrue(divrollRole.getAcl().getAclWrite().contains("0-0"));
                assertTrue(divrollRole.getAcl().getAclRead().contains("0-0"));
            }
        });

    }

    public void testUpdatePublicRoleUsingAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        assertNotNull(role.getEntityId());
        assertEquals("Admin", role.getName());
        assertTrue(role.getAcl().getPublicRead());
        assertTrue(role.getAcl().getPublicWrite());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.create("admin", "password");
        divrollUser.login("admin", "password");

        role.setName("Super Admin");
        role.update();

        role.retrieve();

        assertEquals("Super Admin", role.getName());
    }


    public void testUpdatePublicRoleUsingMasterKey() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
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

    public void testUpdatePublicRoleChangeACLUsingMasterKey() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        assertNotNull(role.getEntityId());
        assertEquals("Admin", role.getName());
        assertTrue(role.getAcl().getPublicRead());
        assertTrue(role.getAcl().getPublicWrite());

        assertTrue(role.getAcl().getPublicRead());
        assertTrue(role.getAcl().getPublicWrite());

        role.setAcl(DivrollACL.buildMasterKeyOnly());
        role.update();

        assertEquals("Admin", role.getName());
        assertFalse(role.getAcl().getPublicRead());
        assertFalse(role.getAcl().getPublicWrite());

        role.retrieve();

        assertEquals("Admin", role.getName());
        assertFalse(role.getAcl().getPublicRead());
        assertFalse(role.getAcl().getPublicWrite());
    }

    public void testUpdatePublicRoleChangeACLUsingAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        String roleId = role.getEntityId();

        assertNotNull(role.getEntityId());
        assertEquals("Admin", role.getName());
        assertTrue(role.getAcl().getPublicRead());
        assertTrue(role.getAcl().getPublicWrite());

        assertTrue(role.getAcl().getPublicRead());
        assertTrue(role.getAcl().getPublicWrite());

        role.setAcl(DivrollACL.buildMasterKeyOnly());
        role.update();

        assertEquals("Admin", role.getName());
        assertFalse(role.getAcl().getPublicRead());
        assertFalse(role.getAcl().getPublicWrite());
    }

    public void testCreateUserWithRole() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        role.create();

        String adminRoleId = role.getEntityId();

        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.getRoles().add(role);
        adminUser.create("admin", "password");
    }

    public void testCreateUserWithRoles() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        role.create();

        DivrollRole managerRole = new DivrollRole("Manager");
        managerRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        managerRole.create();

        String adminRoleId = role.getEntityId();

        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.getRoles().add(role);
        adminUser.getRoles().add(managerRole);
        adminUser.create("admin", "password");
    }

    public void testCreateUserWithRolesThenUpdateRoleWithoutAuthTokenShouldFail() throws RequestException {
        expected(BadRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollRole role = new DivrollRole("Admin");
                role.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
                role.create();

                DivrollRole managerRole = new DivrollRole("Manager");
                managerRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
                managerRole.create();

                String adminRoleId = role.getEntityId();

                DivrollUser adminUser = new DivrollUser();
                adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
                adminUser.getRoles().add(role);
                adminUser.getRoles().add(managerRole);
                adminUser.create("admin", "password");

                assertNotNull(adminUser.getEntityId());

                adminUser.setRoles(Arrays.asList(managerRole));
                adminUser.update();
            }
        });
    }

    public void testCreateUserWithRolesThenUpdateRole() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        role.create();

        DivrollRole managerRole = new DivrollRole("Manager");
        managerRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        managerRole.create();

        String adminRoleId = role.getEntityId();

        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
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

    public void testUpdateUserWithAuthTokenWithRole() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        role.create();

        String adminRoleId = role.getEntityId();

        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.getRoles().add(role);
        adminUser.create("admin", "password");

        DivrollUser divrollUser = new DivrollUser();
        DivrollACL userACL = new DivrollACL();
        userACL.setPublicRead(true);
        userACL.setPublicWrite(false);
        userACL.setAclWrite(Arrays.asList(adminRoleId));
        divrollUser.setAcl(userACL);
        divrollUser.create("user", "password");

        assertNotNull(adminUser.getRoles());
        assertFalse(adminUser.getRoles().isEmpty());

        adminUser.login("admin", "password");
        String authToken = adminUser.getAuthToken();

        assertNotNull(authToken);
        assertNotNull(Divroll.getAuthToken());



        divrollUser.update("new_username", "new_password");
        assertEquals("new_username", divrollUser.getUsername());
    }

    public void testDeletePublicRole() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        assertNotNull(role.getEntityId());

        role.delete();
    }

    public void testDeletePublicRoleThenRetrieveShouldFail() throws RequestException {
        expected(BadRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollRole role = new DivrollRole("Admin");
                role.setAcl(DivrollACL.buildPublicReadWrite());
                role.create();

                assertNotNull(role.getEntityId());

                role.delete();

                role.retrieve();

                assertNull(role.getEntityId());
            }
        });

    }

    public void testDeletePublicRoleWithAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());


        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.create("admin", "password");

        adminUser.login("admin", "password");
        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        assertNotNull(role.getEntityId());

        adminUser.login("admin", "password");

        role.delete();
    }

    public void testDeletePublicRoleWithAuthTokenThenRetrieveShouldFail() throws RequestException {
        expected(BadRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());


                DivrollUser adminUser = new DivrollUser();
                adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
                adminUser.create("admin", "password");

                adminUser.login("admin", "password");
                DivrollRole role = new DivrollRole("Admin");
                role.setAcl(DivrollACL.buildPublicReadWrite());
                role.create();

                assertNotNull(role.getEntityId());

                adminUser.login("admin", "password");

                role.delete();

                role.retrieve();

                assertNull(role.getEntityId());
            }
        });
    }

    public void testDeletePublicRoleWithMasterKey() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        assertNotNull(role.getEntityId());

        role.delete();
    }

    public void testDeletePublicRoleWithMasterKeyTheRetrieveShouldFail() throws RequestException {
        expected(BadRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

                DivrollRole role = new DivrollRole("Admin");
                role.setAcl(DivrollACL.buildPublicReadWrite());
                role.create();

                assertNotNull(role.getEntityId());

                role.delete();

                role.retrieve();

                assertNull(role.getEntityId());
            }
        });
    }

    public void testDeleteRoleWithACLWithAuthToken() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollRole role = new DivrollRole("Admin");
                role.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
                role.create();

                String adminRoleId = role.getEntityId();

                DivrollUser adminUser = new DivrollUser();
                adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
                adminUser.getRoles().add(role);
                adminUser.create("admin", "password");

                assertNotNull(adminUser.getRoles());
                assertFalse(adminUser.getRoles().isEmpty());

                adminUser.login("admin", "password");
                String authToken = adminUser.getAuthToken();

                assertNotNull(authToken);
                assertNotNull(Divroll.getAuthToken());

                role.delete();
            }
        });

    }

    public void testDeleteRoleWithACLWithoutAuthToken() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
                    @Override
                    public void test() throws Throwable {
                        TestApplication application = TestData.getNewApplication();
                        Divroll.initialize(application.getAppId(), application.getApiToken());

                        DivrollRole role = new DivrollRole("Admin");
                        role.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
                        role.create();

                        DivrollUser adminUser = new DivrollUser();
                        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
                        adminUser.getRoles().add(role);
                        adminUser.create("admin", "password");

                        role.delete();
                    }
                }
        );
    }

}
