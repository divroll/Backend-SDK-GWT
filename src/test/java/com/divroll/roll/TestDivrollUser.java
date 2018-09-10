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
public class TestDivrollUser extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "com.divroll.sdk";
    }

    public void testCreateUserPublic() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser user = new DivrollUser();
        user.setAcl(DivrollACL.buildPublicReadWrite());
        user.create("username", "password");

        assertNotNull(user.getEntityId());
        assertEquals("username", user.getUsername());
        assertNotNull(user.getAuthToken());
        //assertTrue(user.getPassword() == null);

        user.retrieve();
    }

    public void testCreateUserInvalidACLShouldThrowException() throws RequestException {
        expected(BadRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());
                DivrollUser user = new DivrollUser();
                DivrollACL acl = DivrollACL.build();
                acl.setAclWrite(Arrays.asList("")); // invalid
                acl.setAclRead(Arrays.asList(""));  // invalid
                user.setAcl(acl);
                user.create("username", "password");
            }
        });
    }
    
    public void testCreateUserMasterKey() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser user = new DivrollUser();
        user.setAcl(null);
        user.create("username", "password");

        assertNotNull(user.getEntityId());
        assertEquals("username", user.getUsername());
        assertTrue(user.getAcl().getAclRead().isEmpty());
        assertTrue(user.getAcl().getAclWrite().isEmpty());
    }

    public void testCreateUserMasterKeyOnlyShouldThrowException() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollUser user = new DivrollUser();
                user.setAcl(null);
                user.create("username", "password");

                assertNotNull(user.getEntityId());
                assertEquals("username", user.getUsername());
                assertTrue(user.getAcl().getAclRead().isEmpty());
                assertTrue(user.getAcl().getAclWrite().isEmpty());

                // This wil throw exception since the created Role has
                // Master Key-only access
                user.retrieve();
            }
        });

    }

    public void testCreateUserMasterKeyOnly() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());


        DivrollUser user = new DivrollUser();
        user.setAcl(DivrollACL.buildMasterKeyOnly());
        user.create("username", "password");

        assertNotNull(user.getEntityId());
        assertEquals("username", user.getUsername());
        assertTrue(user.getAcl().getAclRead().isEmpty());
        assertTrue(user.getAcl().getAclWrite().isEmpty());

        user.retrieve();

        assertNotNull(user.getEntityId());
        assertNotNull(user.getUsername());
        assertNotNull(user.getAcl());
        assertNotNull(user.getAuthToken());
    }

    public void testCreateUserInvalidAppId() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize("WRONG", application.getApiToken());
                DivrollUser user = new DivrollUser();
                user.setAcl(DivrollACL.buildMasterKeyOnly());
                user.create("username", "password");
            }
        });
    }

    public void testCreateUserInvalidApiToken() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), "WRONG");
                DivrollUser user = new DivrollUser();
                user.setAcl(DivrollACL.buildMasterKeyOnly());
                user.create("username", "password");
            }
        });
    }

    public void testCreateUserInvalidMasterKey() {
        // TODO
    }

    public void testGetUserInvalidAppId() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollUser user = new DivrollUser();
                user.setAcl(DivrollACL.buildMasterKeyOnly());
                user.create("username", "password");

                assertNotNull(user.getEntityId());
                assertNotNull(user.getAcl());
                assertNotNull(user.getAuthToken());

                Divroll.initialize("WRONG", application.getApiToken());
                user.retrieve();
                assertNull(user.getEntityId());
                assertNull(user.getAcl());
                assertNull(user.getAuthToken());
            }
        });
    }

    public void testGetUserInvalidApiToken() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();

                Divroll.initialize(application.getAppId(), application.getApiToken());
                DivrollUser user = new DivrollUser();
                user.create("username", "password");

                assertNotNull(user.getEntityId());
                assertNotNull(user.getAcl());
                assertNotNull(user.getUsername());
                assertNotNull(user.getAuthToken());

                Divroll.initialize(application.getAppId(), "WRONG");
                user.retrieve();
                assertNull(user.getEntityId());
                assertNull(user.getAcl());
                assertNull(user.getUsername());
            }
        });
    }

    public void testGetUserInvalidMasterKey() throws RequestException {
        // TODO
    }

    public void testCreateAndGetUserWithACLUsingAuthToken() throws RequestException {
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

        assertNotNull(divrollRole.getEntityId());
        assertTrue(divrollRole.getAcl().getAclWrite().contains("0-0"));
        assertTrue(divrollRole.getAcl().getPublicRead());
    }

    public void testCreateAndGetUserWithACLMissingAuthTokenShouldFail() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollUser divrollAdmin = new DivrollUser();
                divrollAdmin.setAcl(DivrollACL.buildMasterKeyOnly());
                divrollAdmin.create("admin", "password");

                String adminUserId = divrollAdmin.getEntityId();

                DivrollUser divrollUser = new DivrollUser();
                DivrollACL acl = new DivrollACL();
                acl.setAclWrite(Arrays.asList(adminUserId));
                acl.setAclRead(Arrays.asList(adminUserId));
                divrollUser.setAcl(acl);
                divrollUser.create("user", "password");

                assertNotNull(divrollUser.getEntityId());
                assertTrue(divrollUser.getAcl().getAclWrite().contains("0-0"));
                assertTrue(divrollUser.getAcl().getAclRead().contains("0-0"));

                divrollUser.retrieve();

//        assertNotNull(divrollUser.getEntityId());
//        assertTrue(divrollUser.getAcl().getAclWrite().contains("0-0"));
//        assertTrue(divrollUser.getAcl().getAclRead().contains("0-0"));
            }
        });

    }

    public void testUpdatePublicUserMissingAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
        divrollUser.create("username", "password");

        assertNotNull(divrollUser.getEntityId());
        assertEquals("username", divrollUser.getUsername());
        assertTrue(divrollUser.getAcl().getPublicRead());
        assertTrue(divrollUser.getAcl().getPublicWrite());

        divrollUser.update("new_username", "new_password");
        assertEquals("new_username", divrollUser.getUsername());
        assertTrue(divrollUser.getAcl().getPublicRead());
        assertTrue(divrollUser.getAcl().getPublicWrite());
    }

    public void testUpdateUserWithACLMissingAuthTokenShouldFail() throws RequestException {
        expected(BadRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollUser divrollUser = new DivrollUser();
                divrollUser.create("admin", "password");

                assertNotNull(divrollUser.getEntityId());

                String userId = divrollUser.getEntityId();

                DivrollUser user = new DivrollUser();
                DivrollACL divrollACL = DivrollACL.build();
                divrollACL.setAclRead(Arrays.asList(userId));
                divrollACL.setAclWrite(Arrays.asList(userId));
                user.setAcl(divrollACL);
                user.create("username", "password");

                assertNotNull(user.getEntityId());
                assertTrue(user.getAcl().getAclWrite().contains("0-0"));
                assertTrue(user.getAcl().getAclRead().contains("0-0"));

                user.update("new_username", "new_password");

                assertNotNull(user.getEntityId());
                assertTrue(user.getAcl().getAclWrite().contains("0-0"));
                assertTrue(user.getAcl().getAclRead().contains("0-0"));
            }
        });
    }

    public void testUpdatePublicUserUsingAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser admin = new DivrollUser();
        admin.create("admin", "password");

        assertNotNull(admin.getEntityId());

        String userId = admin.getEntityId();

        DivrollUser user = new DivrollUser();
        DivrollACL divrollACL = DivrollACL.build();
        divrollACL.setAclRead(Arrays.asList(userId));
        divrollACL.setAclWrite(Arrays.asList(userId));
        user.setAcl(divrollACL);
        user.create("username", "password");

        assertNotNull(user.getEntityId());
        assertTrue(user.getAcl().getAclWrite().contains("0-0"));
        assertTrue(user.getAcl().getAclRead().contains("0-0"));

        admin.login("admin", "password");

        user.update("new_username", "new_password");

        assertNotNull(user.getEntityId());
        assertTrue(user.getAcl().getAclWrite().contains("0-0"));
        assertTrue(user.getAcl().getAclRead().contains("0-0"));
    }

    public void testUpdatePublicUserUsingMasterKey() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
        divrollUser.create("username", "password");

        assertNotNull(divrollUser.getEntityId());
        assertEquals("username", divrollUser.getUsername());
        assertTrue(divrollUser.getAcl().getPublicRead());
        assertTrue(divrollUser.getAcl().getPublicWrite());

        assertTrue(divrollUser.getAcl().getPublicRead());
        assertTrue(divrollUser.getAcl().getPublicWrite());

        divrollUser.update("new_username", "new_password");

        assertNotNull(divrollUser.getEntityId());
        assertEquals("new_username", divrollUser.getUsername());
        assertTrue(divrollUser.getAcl().getPublicRead());
        assertTrue(divrollUser.getAcl().getPublicWrite());

        assertTrue(divrollUser.getAcl().getPublicRead());
        assertTrue(divrollUser.getAcl().getPublicWrite());


    }

    public void testUpdatePublicUserChangeACLUsingMasterKey() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
        divrollUser.create("username", "password");

        assertEquals("username", divrollUser.getUsername());
        assertTrue(divrollUser.getAcl().getPublicRead());
        assertTrue(divrollUser.getAcl().getPublicWrite());

        divrollUser.setAcl(DivrollACL.buildMasterKeyOnly());
        divrollUser.update();

        assertEquals("username", divrollUser.getUsername());
        assertFalse(divrollUser.getAcl().getPublicRead());
        assertFalse(divrollUser.getAcl().getPublicWrite());

    }

    public void testUpdatePublicRoleChangeACLUsingAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser admin = new DivrollUser();
        admin.create("admin", "password");

        assertNotNull(admin.getEntityId());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
        divrollUser.create("username", "password");

        assertEquals("username", divrollUser.getUsername());
        assertTrue(divrollUser.getAcl().getPublicRead());
        assertTrue(divrollUser.getAcl().getPublicWrite());

        DivrollACL divrollACL = DivrollACL.build();
        divrollACL.setAclRead(Arrays.asList(admin.getEntityId()));
        divrollACL.setAclWrite(Arrays.asList(admin.getEntityId()));
        divrollUser.setAcl(divrollACL);

        admin.login("admin", "password");

        assertNotNull(admin.getAuthToken());

        divrollUser.update();
//
        assertEquals("username", divrollUser.getUsername());
        assertFalse(divrollUser.getAcl().getPublicRead());
        assertFalse(divrollUser.getAcl().getPublicWrite());
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

                DivrollRole adminRole = new DivrollRole("Admin");
                adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
                adminRole.create();

                DivrollRole managerRole = new DivrollRole("Manager");
                managerRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
                managerRole.create();

                String adminRoleId = adminRole.getEntityId();

                DivrollUser adminUser = new DivrollUser();
                adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
                adminUser.getRoles().add(adminRole);
                adminUser.getRoles().add(managerRole);
                adminUser.create("admin", "password");

                assertNotNull(adminUser.getEntityId());
                assertEquals(2, adminUser.getRoles().size());

                adminUser.setRoles(Arrays.asList(managerRole)); // change to Manager role only
                adminUser.update();
                assertEquals(1, adminUser.getRoles().size());
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

    public void testDeletePublicUser() throws RequestException {
        expected(BadRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollUser divrollUser = new DivrollUser();
                divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
                divrollUser.create("username", "password");

                assertNotNull(divrollUser.getEntityId());

                divrollUser.delete();

                divrollUser.retrieve();

                assertNull(divrollUser.getEntityId());
            }
        });
    }

    public void testDeletePublicUserWithAuthToken() throws RequestException {
        expected(BadRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());


                DivrollUser adminUser = new DivrollUser();
                adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
                adminUser.create("admin", "password");

                adminUser.login("admin", "password");

                DivrollUser divrollUser = new DivrollUser();
                divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
                divrollUser.create("user", "password");

                assertNotNull(divrollUser.getEntityId());

                adminUser.login("admin", "password");

                divrollUser.delete();

                divrollUser.retrieve();

                assertNull(divrollUser.getEntityId());
            }
        });
    }

    public void testDeletePublicUserWithMasterKey() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollRole role = new DivrollRole("Admin");
        role.setAcl(DivrollACL.buildPublicReadWrite());
        role.create();

        assertNotNull(role.getEntityId());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
        divrollUser.create("username", "password");

        assertNotNull(divrollUser.getEntityId());

        divrollUser.delete();

    }

    public void testDeleteUserWithACLWithAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole adminRole = new DivrollRole("Admin");
        adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        adminRole.create();

        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.getRoles().add(adminRole);
        adminUser.create("admin", "password");

        assertNotNull(adminUser.getRoles());
        assertFalse(adminUser.getRoles().isEmpty());

        DivrollUser divrollUser = new DivrollUser();
        DivrollACL acl = new DivrollACL();
        acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
        divrollUser.setAcl(acl);
        divrollUser.create("username", "password");

        assertNotNull(divrollUser.getEntityId());
        assertNotNull(divrollUser.getAcl().getAclWrite().contains(adminRole.getEntityId()));

        adminUser.login("admin", "password");
        assertNotNull(adminUser.getAuthToken());
        assertNotNull(Divroll.getAuthToken());

        divrollUser.delete();
    }

    public void testDeleteUserWithACLWithoutAuthToken() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollRole adminRole = new DivrollRole("Admin");
                adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
                adminRole.create();

                DivrollUser adminUser = new DivrollUser();
                adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
                adminUser.getRoles().add(adminRole);
                adminUser.create("admin", "password");

                assertNotNull(adminUser.getRoles());
                assertFalse(adminUser.getRoles().isEmpty());

                DivrollUser divrollUser = new DivrollUser();
                DivrollACL acl = new DivrollACL();
                acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
                divrollUser.setAcl(acl);
                divrollUser.create("username", "password");

                assertNotNull(divrollUser.getEntityId());
                assertNotNull(divrollUser.getAcl().getAclWrite().contains(adminRole.getEntityId()));

                divrollUser.delete();
            }
        });
    }

}
