package com.divroll.roll;

import com.google.gwt.http.client.RequestException;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Window;
import org.gwtproject.http.client.exceptions.NotFoundRequestException;
import org.gwtproject.http.client.exceptions.UnauthorizedRequestException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.List;

import static com.divroll.roll.TestHelper.expected;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDivrollEntity extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "com.divroll.sdk";
    }

    public void testCreateEntityUsingMasterKey()  throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollEntity entity = new DivrollEntity("TestEntity");
        entity.setProperty("username", "TestUser");
        entity.setProperty("age", 30);
        entity.setProperty("nickname", "testo");
        entity.create();

        assertNotNull(entity.getEntityId());
    }

    public void testCreateEntityWithMapUsingMasterKey()  throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollEntity entity = new DivrollEntity("TestEntity");
        entity.setProperty("username", "TestUser");
        entity.setProperty("age", 30);
        entity.setProperty("nickname", "testo");

        Map<String,Object> map = new LinkedHashMap<String,Object>();
        map.put("test1", "Test Data");
        map.put("test2", 123);
        map.put("test3", false);

        entity.setProperty("embed", map);

        List<Object> list = new LinkedList<Object>();
        list.add("Hello");
        list.add("World");
        list.add(456);
        list.add(true);

        entity.setProperty("list", list);

        entity.create();

        String entityId = entity.getEntityId();

        assertNotNull(entityId);

        DivrollEntity entity1 = new DivrollEntity("TestEntity");
        entity1.setEntityId(entityId);

        entity1.retrieve();

        assertNotNull(entity1.getEntityId());
        assertNotNull(entity1.getProperty("embed"));

        assertEquals("Test Data", ((Map<String,Object>) (entity1.getProperty("embed"))).get("test1"));
        assertEquals(123, ((Double)((Map<String,Object>) (entity1.getProperty("embed"))).get("test2")).longValue());
        assertEquals(false, ((Map<String,Object>) (entity1.getProperty("embed"))).get("test3"));

        assertNotNull(entity1.getProperty("list"));
        assertEquals("Hello", ((List)entity1.getProperty("list")).get(0));
        assertEquals("World", ((List)entity1.getProperty("list")).get(1));
        assertEquals(456, ((Double) ((List)entity1.getProperty("list")).get(2)).longValue());
        assertEquals(true, ((List)entity1.getProperty("list")).get(3));

        assertEquals("TestUser", entity1.getProperty("username"));
        assertEquals(30, entity1.getIntegerProperty("age").intValue());
        assertEquals("testo", entity1.getProperty("nickname"));


    }

    public void testCreatePublicEntityThenUpdate() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollEntity entity = new DivrollEntity("TestEntity");
        entity.setProperty("username", "TestUser");
        entity.setProperty("age", 30);
        entity.setProperty("nickname", "testo");
        entity.setAcl(DivrollACL.buildPublicReadWrite());
        entity.create();

        assertNotNull(entity.getEntityId());

        entity.setProperty("age", 31);

        entity.update();
    }

    public void testCreateEntityInvalidACL() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());
        DivrollEntity userProfile = new DivrollEntity("UserProfile");
        DivrollACL acl = DivrollACL.build();
        acl.setAclWrite(Arrays.asList("")); // invalid
        acl.setAclRead(Arrays.asList(""));  // invalid
        userProfile.setAcl(acl);
        userProfile.create();
        assertNotNull(userProfile.getEntityId());
    }

    public void testCreateEntityMasterKey() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollEntity userProfile = new DivrollEntity("UserProfile");
        userProfile.setAcl(null);
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        assertNotNull(userProfile.getEntityId());
        assertEquals("Johnny", userProfile.getProperty("nickname"));
        Double age = (Double) userProfile.getProperty("age");
        assertEquals(30, age.intValue());
        assertNull(userProfile.getAcl());
        assertNull(userProfile.getAcl());
    }

    public void testCreateEntityMasterKeyOnlyShouldThrowException() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollEntity userProfile = new DivrollEntity("UserProfile");
                userProfile.setAcl(null);
                userProfile.setProperty("nickname", "Johnny");
                userProfile.setProperty("age", 30);
                userProfile.create();

                assertNotNull(userProfile.getEntityId());
                assertEquals("Johnny", userProfile.getProperty("nickname"));
                Double age = (Double) userProfile.getProperty("age");
                assertEquals(30, age.intValue());
                assertNull(userProfile.getAcl());
                assertNull(userProfile.getAcl());

                // This wil throw exception since the created Role has
                // Master Key-only access
                userProfile.retrieve();
            }
        });

    }

    public void testCreateEntityMasterKeyOnly() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollEntity userProfile = new DivrollEntity("UserProfile");
        userProfile.setAcl(DivrollACL.buildMasterKeyOnly());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        assertNotNull(userProfile.getEntityId());
        assertEquals("Johnny", userProfile.getProperty("nickname"));
        assertTrue(userProfile.getAcl().getAclRead().isEmpty());
        assertTrue(userProfile.getAcl().getAclWrite().isEmpty());

        userProfile.retrieve();

        assertNotNull(userProfile.getEntityId());
        assertEquals("Johnny", userProfile.getProperty("nickname"));
        assertTrue(userProfile.getAcl().getAclRead().isEmpty());
        assertTrue(userProfile.getAcl().getAclWrite().isEmpty());
    }

    public void testCreateEntityInvalidAppId() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize("WRONG", application.getApiToken());
                DivrollEntity userProfile = new DivrollEntity("UserProfile");
                userProfile.setAcl(DivrollACL.buildMasterKeyOnly());
                userProfile.setProperty("nickname", "Johnny");
                userProfile.setProperty("age", 30);
                userProfile.create();
            }
        });

    }

    public void testCreateEntityInvalidApiToken() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), "WRONG");
                DivrollEntity userProfile = new DivrollEntity("UserProfile");
                userProfile.setAcl(DivrollACL.buildMasterKeyOnly());
                userProfile.setProperty("nickname", "Johnny");
                userProfile.setProperty("age", 30);
                userProfile.create();
            }
        });

    }

    public void testCreateUserInvalidMasterKey() {
        // TODO
    }

    public void testGetEntityInvalidAppId() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollEntity userProfile = new DivrollEntity("UserProfile");
                userProfile.setAcl(DivrollACL.buildMasterKeyOnly());
                userProfile.setProperty("nickname", "Johnny");
                userProfile.setProperty("age", 30);
                userProfile.create();

                assertNotNull(userProfile.getEntityId());
                assertEquals("Johnny", userProfile.getProperty("nickname"));
                assertTrue(userProfile.getAcl().getAclRead().isEmpty());
                assertTrue(userProfile.getAcl().getAclWrite().isEmpty());

                Divroll.initialize("WRONG", application.getApiToken());
                userProfile.retrieve();
                assertNull(userProfile.getEntityId());
                assertNull(userProfile.getAcl());
            }
        });

    }

    public void testGetUserInvalidApiToken() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollEntity userProfile = new DivrollEntity("UserProfile");
                userProfile.setAcl(DivrollACL.buildMasterKeyOnly());
                userProfile.setProperty("nickname", "Johnny");
                userProfile.setProperty("age", 30);
                userProfile.create();

                assertNotNull(userProfile.getEntityId());
                assertEquals("Johnny", userProfile.getProperty("nickname"));
                assertTrue(userProfile.getAcl().getAclRead().isEmpty());
                assertTrue(userProfile.getAcl().getAclWrite().isEmpty());

                Divroll.initialize(application.getAppId(), "WRONG");
                userProfile.retrieve();
                assertNull(userProfile.getEntityId());
                assertNull(userProfile.getAcl());
            }
        });

    }

    public void testGetUserInvalidMasterKey() {
        // TODO
    }

    public void testCreateAndGetEntityWithACLUsingAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser adminUser = new DivrollUser();
        adminUser.create("admin", "password");

        assertNotNull(adminUser.getEntityId());

        String userId = adminUser.getEntityId();

        DivrollEntity userProfile = new DivrollEntity("UserProfile");

        DivrollACL divrollACL = DivrollACL.build();
        divrollACL.setPublicRead(true);
        divrollACL.setAclWrite(Arrays.asList(userId));
        userProfile.setAcl(divrollACL);

        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        assertNotNull(userProfile.getEntityId());
        assertTrue(userProfile.getAcl().getAclWrite().contains(userId));
        assertTrue(userProfile.getAcl().getPublicRead());

        adminUser.login("admin", "password");
        assertNotNull(adminUser.getAuthToken());
        assertNotNull(Divroll.getAuthToken());

        userProfile.retrieve();

        assertNotNull(userProfile.getEntityId());
        Window.alert("USER ID: " + userId);
        Window.alert("------------------------->" + userProfile.getAcl().getAclWrite().toString());
        assertTrue(userProfile.getAcl().getAclWrite().contains(userId));
        assertTrue(userProfile.getAcl().getPublicRead());
    }

    public void testCreateAndGetEntityWithACLMissingAuthTokenShouldFail() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollUser divrollAdmin = new DivrollUser();
                divrollAdmin.setAcl(DivrollACL.buildMasterKeyOnly());
                divrollAdmin.create("admin", "password");

                String adminUserId = divrollAdmin.getEntityId();

                DivrollEntity userProfile = new DivrollEntity("UserProfile");
                DivrollACL acl = new DivrollACL();
                acl.setAclWrite(Arrays.asList(adminUserId));
                acl.setAclRead(Arrays.asList(adminUserId));
                userProfile.setAcl(acl);
                userProfile.setProperty("nickname", "Johnny");
                userProfile.setProperty("age", 30);
                userProfile.create();

                assertNotNull(userProfile.getEntityId());
                assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
                assertTrue(userProfile.getAcl().getAclRead().contains("0-0"));

                userProfile.retrieve();
            }
        });


    }

    public void testUpdatePublicEntityMissingAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollEntity userProfile = new DivrollEntity("UserProfile");
        userProfile.setAcl(DivrollACL.buildPublicReadWrite());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        assertNotNull(userProfile.getEntityId());
        assertEquals("Johnny", userProfile.getProperty("nickname"));
        assertTrue(userProfile.getAcl().getPublicRead());
        assertTrue(userProfile.getAcl().getPublicWrite());

        userProfile.setProperty("age", 40);
        userProfile.update();

        Double age = (Double) userProfile.getProperty("age");
        assertEquals(40, age.intValue());
        assertTrue(userProfile.getAcl().getPublicRead());
        assertTrue(userProfile.getAcl().getPublicWrite());
    }

    public void testUpdateEntityWithACLMissingAuthTokenShouldFail() throws RequestException {
        expected(UnauthorizedRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollUser divrollUser = new DivrollUser();
                divrollUser.create("admin", "password");

                assertNotNull(divrollUser.getEntityId());

                String userId = divrollUser.getEntityId();

                DivrollACL divrollACL = DivrollACL.build();
                divrollACL.setAclRead(Arrays.asList(userId));
                divrollACL.setAclWrite(Arrays.asList(userId));

                DivrollEntity userProfile = new DivrollEntity("UserProfile");
                userProfile.setAcl(divrollACL);
                userProfile.setProperty("nickname", "Johnny");
                userProfile.setProperty("age", 30);
                userProfile.create();

                assertNotNull(userProfile.getEntityId());
                assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
                assertTrue(userProfile.getAcl().getAclRead().contains("0-0"));

                userProfile.setProperty("age", 40);
                userProfile.update();
            }
        });

    }

    public void testUpdateEntityWithACL() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.create("admin", "password");

        assertNotNull(divrollUser.getEntityId());

        String userId = divrollUser.getEntityId();

        DivrollACL divrollACL = DivrollACL.build();
        divrollACL.setAclRead(Arrays.asList(userId));
        divrollACL.setAclWrite(Arrays.asList(userId));

        DivrollEntity userProfile = new DivrollEntity("UserProfile");
        userProfile.setAcl(divrollACL);
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        assertNotNull(userProfile.getEntityId());
        assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
        assertTrue(userProfile.getAcl().getAclRead().contains("0-0"));

        divrollUser.login("admin", "password");

        userProfile.setProperty("age", 40);
        userProfile.update();
    }

    public void testUpdateEntityWithACLUsingMasterKey() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollUser divrollUser = new DivrollUser();
        divrollUser.create("admin", "password");

        assertNotNull(divrollUser.getEntityId());

        String userId = divrollUser.getEntityId();

        DivrollACL divrollACL = DivrollACL.build();
        divrollACL.setAclRead(Arrays.asList(userId));
        divrollACL.setAclWrite(Arrays.asList(userId));

        DivrollEntity userProfile = new DivrollEntity("UserProfile");
        userProfile.setAcl(divrollACL);
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        assertNotNull(userProfile.getEntityId());
        assertTrue(userProfile.getAcl().getAclWrite().contains("0-0"));
        assertTrue(userProfile.getAcl().getAclRead().contains("0-0"));

        userProfile.setProperty("age", 40);
        userProfile.update();
    }

    public void testUpdatePublicEntityUsingAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser admin = new DivrollUser();
        admin.create("admin", "password");

        assertNotNull(admin.getEntityId());

        DivrollEntity userProfile = new DivrollEntity("UserProfile");
        userProfile.setAcl(DivrollACL.buildPublicReadWrite());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        assertNotNull(userProfile.getEntityId());
        assertTrue(userProfile.getAcl().getPublicRead());
        assertTrue(userProfile.getAcl().getPublicWrite());

        admin.login("admin", "password");

        userProfile.setProperty("age", 40);
        userProfile.update();

        assertNotNull(userProfile.getEntityId());
        assertTrue(userProfile.getAcl().getPublicRead());
        assertTrue(userProfile.getAcl().getPublicWrite());
    }

    public void testUpdatePublicUserUsingMasterKey() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser admin = new DivrollUser();
        admin.create("admin", "password");

        assertNotNull(admin.getEntityId());

        DivrollEntity userProfile = new DivrollEntity("UserProfile");
        userProfile.setAcl(DivrollACL.buildPublicReadWrite());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        assertNotNull(userProfile.getEntityId());
        assertTrue(userProfile.getAcl().getPublicRead());
        assertTrue(userProfile.getAcl().getPublicWrite());

        userProfile.setProperty("age", 40);
        userProfile.update();

        assertNotNull(userProfile.getEntityId());
        assertTrue(userProfile.getAcl().getPublicRead());
        assertTrue(userProfile.getAcl().getPublicWrite());

    }

    public void testUpdatePublicEntityChangeACLUsingMasterKey() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollEntity userProfile = new DivrollEntity("UserProfile");
        userProfile.setAcl(DivrollACL.buildPublicReadWrite());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        assertNotNull(userProfile.getEntityId());
        assertTrue(userProfile.getAcl().getPublicRead());
        assertTrue(userProfile.getAcl().getPublicWrite());

        userProfile.setAcl(DivrollACL.buildMasterKeyOnly());
        userProfile.update();

        assertEquals("Johnny", userProfile.getProperty("nickname"));
        assertFalse(userProfile.getAcl().getPublicRead());
        assertFalse(userProfile.getAcl().getPublicWrite());

    }

    public void testUpdatePublicEntityChangeACLUsingAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollUser admin = new DivrollUser();
        admin.create("admin", "password");

        assertNotNull(admin.getEntityId());

        DivrollEntity userProfile = new DivrollEntity("UserProfile");
        userProfile.setAcl(DivrollACL.buildPublicReadWrite());
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.create();

        assertNotNull(userProfile.getEntityId());
        assertTrue(userProfile.getAcl().getPublicRead());
        assertTrue(userProfile.getAcl().getPublicWrite());

        DivrollACL divrollACL = DivrollACL.build();
        divrollACL.setAclRead(Arrays.asList(admin.getEntityId()));
        divrollACL.setAclWrite(Arrays.asList(admin.getEntityId()));
        userProfile.setAcl(divrollACL);

        admin.login("admin", "password");

        assertNotNull(admin.getAuthToken());

        userProfile.setProperty("age", 40);
        userProfile.update();

        assertNotNull(userProfile.getEntityId());
        assertNull(userProfile.getAcl().getPublicRead());
        assertNull(userProfile.getAcl().getPublicWrite());
    }

    public void testUpdateEntityUsingAuthTokenWithRole() throws RequestException {
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

        DivrollEntity userProfile = new DivrollEntity("UserProfile");
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);

        DivrollACL divrollACL = DivrollACL.build();
        divrollACL.setAclRead(Arrays.asList(adminUser.getEntityId()));
        divrollACL.setAclWrite(Arrays.asList(adminUser.getEntityId()));
        userProfile.setAcl(divrollACL);

        userProfile.create();

        userProfile.update();

    }

    public void testDeletePublicEntity() throws RequestException {
        expected(NotFoundRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());

                DivrollEntity userProfile = new DivrollEntity("UserProfile");
                userProfile.setProperty("nickname", "Johnny");
                userProfile.setProperty("age", 30);
                userProfile.setAcl(DivrollACL.buildPublicReadWrite());

                userProfile.create();

                assertNotNull(userProfile.getEntityId());

                userProfile.delete();
                userProfile.retrieve();
                assertNull(userProfile.getEntityId());
            }
        });
    }

    public void testDeletePublicEntityWithAuthToken() throws RequestException {
        expected(NotFoundRequestException.class, new TestHelper.Testable() {
            @Override
            public void test() throws Throwable {
                TestApplication application = TestData.getNewApplication();
                Divroll.initialize(application.getAppId(), application.getApiToken());


                DivrollUser adminUser = new DivrollUser();
                adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
                adminUser.create("admin", "password");

                adminUser.login("admin", "password");

                DivrollEntity userProfile = new DivrollEntity("UserProfile");
                userProfile.setProperty("nickname", "Johnny");
                userProfile.setProperty("age", 30);
                userProfile.setAcl(DivrollACL.buildPublicReadWrite());
                userProfile.create();

                assertNotNull(userProfile.getEntityId());

                adminUser.login("admin", "password");

                userProfile.delete();

                userProfile.retrieve();

                assertNull(userProfile.getEntityId());
            }
        });

    }

    public void testDeletePublicEntityWithMasterKey() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollEntity userProfile = new DivrollEntity("UserProfile");
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        userProfile.setAcl(DivrollACL.buildPublicReadWrite());
        userProfile.create();

        assertNotNull(userProfile.getEntityId());
        assertTrue(userProfile.delete());
    }

    public void testDeleteEntityWithACLWithAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole adminRole = new DivrollRole("Admin");
        adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        adminRole.create();

        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.getRoles().add(adminRole);
        adminUser.create("admin", "password");

        DivrollEntity userProfile = new DivrollEntity("UserProfile");
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        DivrollACL acl = new DivrollACL();
        acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
        userProfile.setAcl(acl);

        userProfile.create();

        assertNotNull(userProfile.getEntityId());
        assertNotNull(userProfile.getAcl().getAclWrite().contains(adminRole.getEntityId()));

        adminUser.login("admin", "password");
        assertNotNull(adminUser.getAuthToken());
        assertNotNull(Divroll.getAuthToken());

        userProfile.delete();
    }

    public void testDeleteEntityWithACLWithoutAuthToken() throws RequestException {
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

                DivrollEntity userProfile = new DivrollEntity("UserProfile");
                userProfile.setProperty("nickname", "Johnny");
                userProfile.setProperty("age", 30);
                DivrollACL acl = new DivrollACL();
                acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
                userProfile.setAcl(acl);

                userProfile.create();

                assertNotNull(userProfile.getEntityId());
                assertNotNull(userProfile.getAcl().getAclWrite().contains(adminRole.getEntityId()));
                userProfile.delete();
            }
        });


        //adminUser.login("admin", "password");
//        assertNotNull(adminUser.getAuthToken());
//        assertNotNull(Divroll.getAuthToken());

    }

    public void testSetEntityBlob() throws RequestException, UnsupportedEncodingException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole adminRole = new DivrollRole("Admin");
        adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        adminRole.create();

        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.getRoles().add(adminRole);
        adminUser.create("admin", "password");

        DivrollEntity userProfile = new DivrollEntity("UserProfile");
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        DivrollACL acl = new DivrollACL();
        acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
        userProfile.setAcl(acl);

        userProfile.create();

        assertNotNull(userProfile.getEntityId());
        assertNotNull(userProfile.getAcl().getAclWrite().contains(adminRole.getEntityId()));

        adminUser.login("admin", "password");
        assertNotNull(adminUser.getAuthToken());
        assertNotNull(Divroll.getAuthToken());

        try {
            userProfile.setBlobProperty("picture", "this is a picture".getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] blob = userProfile.getBlobProperty("picture");

        assertNotNull(blob);
        assertEquals("this is a picture", new String(blob, "utf-8"));

        userProfile.deleteBlobProperty("picture");

        userProfile.delete();
    }

    public void testCreateLink() throws RequestException, UnsupportedEncodingException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DivrollRole adminRole = new DivrollRole("Admin");
        adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
        adminRole.create();

        DivrollUser adminUser = new DivrollUser();
        adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
        adminUser.getRoles().add(adminRole);
        adminUser.create("admin", "password");

        DivrollEntity userProfile = new DivrollEntity("UserProfile");
        userProfile.setProperty("nickname", "Johnny");
        userProfile.setProperty("age", 30);
        DivrollACL acl = new DivrollACL();
        acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
        acl.setAclRead(Arrays.asList(adminRole.getEntityId()));
        userProfile.setAcl(acl);

        userProfile.create();

        assertNotNull(userProfile.getEntityId());
        assertNotNull(userProfile.getAcl().getAclWrite().contains(adminRole.getEntityId()));

        adminUser.login("admin", "password");
        assertNotNull(adminUser.getAuthToken());
        assertNotNull(Divroll.getAuthToken());

        try {
            userProfile.setBlobProperty("picture", "this is a picture".getBytes("utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] blob = userProfile.getBlobProperty("picture");

        assertNotNull(blob);
        assertEquals("this is a picture", new String(blob, "utf-8"));

        userProfile.deleteBlobProperty("picture");


        try {
            userProfile.addLink("user", adminUser.getEntityId());
            userProfile.retrieve();
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertNotNull(userProfile.getProperty("links"));
        assertTrue(((List)userProfile.getProperty("links")).contains("user"));

        List<DivrollEntity> entities = userProfile.links("user");
        assertNotNull(entities);
        assertFalse(entities.isEmpty());

        for(DivrollEntity entity : entities) {


        }


        userProfile.removeLink("user", adminUser.getEntityId());
        userProfile.retrieve();
//
        assertNotNull(userProfile.getProperty("links"));
        assertFalse(((List)userProfile.getProperty("links")).contains("user"));

        userProfile.delete();
    }


}
