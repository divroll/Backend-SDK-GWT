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

import com.google.gwt.http.client.RequestException;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Window;
import elemental.client.Browser;
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
            List<DivrollUser> list = new LinkedList<>();
            for(int i=0;i<100;i++) {
                DivrollUser divrollUser = new DivrollUser();
                divrollUser.create(df.getEmailAddress(), "password").subscribe(createdUser -> {
                    list.add(divrollUser);
                    if(list.size() == 100) {
                        DivrollUsers users = new DivrollUsers();
                        users.query().subscribe(divrollUsers -> {
                            assertEquals(100, divrollUsers.getUsers().size());
                            finishTest();
                        });
                    }
                });
            }
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
                                Browser.getWindow().getConsole().log("Query Test:" + new DivrollUsers());
                                Browser.getWindow().getConsole().log("Query Result:" + queryUsers.toString());
                                assertEquals(0, queryUsers.getUsers().size());
                                createdAdminUser.login(adminUsername, "password", true).subscribe(loggedInUser -> {
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
                                                list1.add(createdUser1);
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
