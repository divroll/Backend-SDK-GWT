package com.divroll.backend.sdk;

import com.google.gwt.http.client.RequestException;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Window;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DivrollUsersTest extends GWTTestCase {

    public static final int DELAY = 10000;

    @Override
    public String getModuleName() {
        return "com.divroll.backend.sdk";
    }

    public void testGetApplication() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken(), testApplication.getMasterKey());
            assertNotNull(testApplication.getApiToken());
            assertNotNull(testApplication.getAppId());
            assertNotNull(testApplication.getMasterKey());
            finishTest();
        });
        delayTestFinish(DELAY);
    }

    public void testGetUsers() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken(), testApplication.getMasterKey());

            DataFactory df = new DataFactory();
            for(int i=0;i<100;i++) {
                DivrollUser divrollUser = new DivrollUser();
                divrollUser.create(df.getEmailAddress(), "password");
            }

            DivrollUsers users = new DivrollUsers();
            users.query().subscribe(divrollUsers -> {
                assertEquals(100, divrollUsers.getUsers().size());
                finishTest();
            });

        });
        delayTestFinish(DELAY);

    }

    public void testGetUsersMasterKeyOnly() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());

            Window.alert("Created app " + testApplication.getAppId());

            DataFactory df = new DataFactory();
            List<DivrollUser> list = new LinkedList<>();
            for(int i=0;i<100;i++) {
                DivrollUser divrollUser = new DivrollUser();
                divrollUser.setAcl(DivrollACL.buildMasterKeyOnly());
                divrollUser.create(df.getEmailAddress(), "password").subscribe(createdUser -> {
                    list.add(createdUser);
                    if(list.size() == 100) {
                        DivrollUsers users = new DivrollUsers();
                        users.query().subscribe(divrollUsers -> {
                            assertEquals(0, divrollUsers.getUsers().size());
                            finishTest();
                        });
                    }
                });
            }
        });
        delayTestFinish(DELAY);
    }

    public void testGetUsersWithACLUsingAuthToken() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {

            Window.alert("Created app " + testApplication.getAppId());

            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());

            DataFactory df = new DataFactory();

            DivrollUser admin = new DivrollUser();
            admin.setAcl(DivrollACL.buildMasterKeyOnly());
            String adminUsername = df.getEmailAddress();
            admin.create(adminUsername, "password").subscribe(createdAdminUser -> {
                List<DivrollUser> list = new LinkedList<>();
                for(int i=0;i<10;i++) {
                    DivrollUser divrollUser = new DivrollUser();
                    DivrollACL acl = new DivrollACL();
                    acl.setPublicWrite(false);
                    acl.setPublicRead(false);
                    acl.setAclRead(Arrays.asList(createdAdminUser.getEntityId()));
                    acl.setAclWrite(Arrays.asList(createdAdminUser.getEntityId()));
                    divrollUser.setAcl(acl);
                    divrollUser.create(df.getEmailAddress(), "password").subscribe(createdUser -> {
                        list.add(createdUser);
                        Window.alert("List size=" + list.size());
                        if(list.size() == 10) {
                            DivrollUsers users = new DivrollUsers();
                            users.query().subscribe(queryUsers -> {
                                assertEquals(0, queryUsers.getUsers().size());

                                createdAdminUser.login(adminUsername, "password").subscribe(loggedInUser -> {

                                    assertNotNull(loggedInUser.getAuthToken());

                                    DivrollUsers divrollUsers = new DivrollUsers();
                                    divrollUsers.query().subscribe(divrollUsers1 -> {
                                        assertEquals(10, divrollUsers1.getUsers().size());
                                        List<DivrollUser> list1 = new LinkedList<>();
                                        for(int j=0;j<20;j++) {
                                            DivrollUser user = new DivrollUser();
                                            DivrollACL userAcl = new DivrollACL();
                                            userAcl.setPublicWrite(false);
                                            userAcl.setPublicRead(true);
                                            user.setAcl(userAcl);
                                            user.create(df.getEmailAddress(), "password").subscribe(createdUser1 -> {
                                                assertNotNull(createdUser1.getEntityId());
                                                Window.alert("List1 size=" + list1.size());
                                                if(list1.size() == 20) {
                                                    DivrollUsers divrollUsers2 = new DivrollUsers();
                                                    divrollUsers2.query().subscribe(queriedUsers -> {
                                                        assertEquals(30, queriedUsers.getUsers().size());
                                                        List<DivrollUser> list2 = new LinkedList<>();
                                                        for(int k=0;k<20;k++) {
                                                            DivrollUser user1 = new DivrollUser();
                                                            DivrollACL userAcl1 = new DivrollACL();
                                                            userAcl1.setPublicWrite(true);
                                                            userAcl1.setPublicRead(false);
                                                            user1.setAcl(userAcl1);
                                                            user1.create(df.getEmailAddress(), "password").subscribe(createdUser2 -> {
                                                                list2.add(createdUser2);
                                                                Window.alert("List2 size=" + list2.size());
                                                                if(list2.size() == 20) {
                                                                    DivrollUsers divrollUsers3 = new DivrollUsers();
                                                                    divrollUsers3.query().subscribe(queriedUsers1 -> {
                                                                        assertEquals(30, queriedUsers1.getUsers().size());
                                                                        finishTest();
                                                                    });
                                                                }
                                                            });
                                                        }
                                                    });
                                                }

                                            });
                                        }
                                    });
                                });
                            });
                        }
                    });
                }
            });
        });
        delayTestFinish(60000);

    }

}