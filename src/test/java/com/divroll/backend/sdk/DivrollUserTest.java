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

import com.divroll.http.client.exceptions.BadRequestException;
import com.divroll.http.client.exceptions.UnauthorizedRequestException;
import com.divroll.backend.sdk.helper.ACLHelper;
import com.divroll.backend.sdk.helper.RoleHelper;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Window;
import io.reactivex.functions.Consumer;

import java.util.Arrays;

/**
 *
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class DivrollUserTest extends GWTTestCase {

    public static final int DELAY = 10000;

    @Override
    public String getModuleName() {
        return "com.divroll.backend.sdk";
    }
    public void test() {
        Window.alert("Sample Test");
    }

    public void testCreateUserPublic() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());
            DivrollUser user = new DivrollUser();
            user.setAcl(DivrollACL.buildPublicReadWrite());
            user.create("username", "password").subscribe(createdUser -> {
                assertNotNull(createdUser.getEntityId());
                assertEquals("username", createdUser.getUsername());
                assertNotNull(createdUser.getAuthToken());
                //assertTrue(user.getPassword() == null);
                createdUser.retrieve().subscribe(retrievedUser -> {
                    assertNotNull(retrievedUser.getEntityId());
                    assertEquals("username", retrievedUser.getUsername());
                    assertNotNull(retrievedUser.getAuthToken());
                    //assertTrue(divrollUser.getPassword() == null);
                    finishTest();
                });
            });
        });
        delayTestFinish(DELAY);
    }
    public void testCreateUserInvalidACLShouldThrowException() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());
            DivrollUser user = new DivrollUser();
            DivrollACL acl = DivrollACL.build();
            acl.setAclWrite(Arrays.asList("")); // invalid
            acl.setAclRead(Arrays.asList(""));  // invalid
            user.setAcl(acl);
            user.create("username", "password").subscribe(divrollUser -> {
                Window.alert(divrollUser.getEntityId());
                Window.alert(divrollUser.getAcl().getAclRead().toString());
                Window.alert(divrollUser.getAcl().getAclWrite().toString());
                finishTest();
            }, error -> {
                if(error instanceof BadRequestException) {
                    finishTest();
                } else {
                    fail();
                }
            });
        });
        delayTestFinish(DELAY);
    }

    public void testCreateUserMasterKey() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());
            DivrollUser user = new DivrollUser();
            user.setAcl(null);
            user.create("username", "password").subscribe(divrollUser -> {
                assertNotNull(divrollUser.getEntityId());
                assertEquals("username", divrollUser.getUsername());
                assertTrue(divrollUser.getAcl().getAclRead().isEmpty());
                assertTrue(divrollUser.getAcl().getAclWrite().isEmpty());
                finishTest();
            });
        });
        delayTestFinish(DELAY);
    }

    public void testCreateUserMasterKeyOnlyShouldThrowException() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());

            DivrollUser user = new DivrollUser();
            user.setAcl(null);
            user.create("username", "password").subscribe(new Consumer<DivrollUser>() {
                @Override
                public void accept(DivrollUser divrollUser) throws Exception {
                    assertNotNull(user.getEntityId());
                    assertEquals("username", user.getUsername());
                    assertTrue(user.getAcl().getAclRead().isEmpty());
                    assertTrue(user.getAcl().getAclWrite().isEmpty());

                    // This wil throw exception since the created Role has
                    // Master Key-only access
                    user.retrieve().subscribe(divrollUser1 -> fail(), error -> {
                        if(error instanceof UnauthorizedRequestException) {
                            finishTest();
                        } else {
                            fail();
                        }
                    });
                }
            });

        });
        delayTestFinish(DELAY);
    }

    public void testCreateUserMasterKeyOnly() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken(), testApplication.getMasterKey());
            DivrollUser user = new DivrollUser();
            user.setAcl(DivrollACL.buildMasterKeyOnly());
            user.create("username", "password").subscribe(divrollUser -> {

                assertNotNull(user.getEntityId());
                assertEquals("username", user.getUsername());
                assertTrue(user.getAcl().getAclRead().isEmpty());
                assertTrue(user.getAcl().getAclWrite().isEmpty());

                user.retrieve().subscribe(divrollUser1 -> {
                    assertNotNull(divrollUser1.getEntityId());
                    assertNotNull(divrollUser1.getUsername());
                    assertNotNull(divrollUser1.getAcl());
                    assertNotNull(divrollUser1.getAuthToken());
                    finishTest();
                });

            });
        });
        delayTestFinish(DELAY);
    }

    public void testCreateUserInvalidAppId() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize("WRONG", testApplication.getApiToken());
            DivrollUser user = new DivrollUser();
            user.setAcl(DivrollACL.buildMasterKeyOnly());
            user.create("username", "password").subscribe((divrollUser, throwable) -> {
                if(throwable instanceof UnauthorizedRequestException) {
                    finishTest();
                } else {
                    fail();
                }
            });
        });
        delayTestFinish(DELAY);
    }

    public void testCreateUserInvalidApiToken() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), "WRONG");
            DivrollUser user = new DivrollUser();
            user.setAcl(DivrollACL.buildMasterKeyOnly());
            user.create("username", "password").subscribe((divrollUser, throwable) -> {
                if(throwable instanceof UnauthorizedRequestException) {
                    finishTest();
                }
                fail();
            });
        });
        delayTestFinish(DELAY);
    }

    public void testCreateUserInvalidMasterKey() {
        // TODO
    }

    public void testGetUserInvalidAppId() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());
            DivrollUser user = new DivrollUser();
            user.setAcl(DivrollACL.buildMasterKeyOnly());
            user.create("username", "password").subscribe(divrollUser -> {
                assertNotNull(user.getEntityId());
                assertNotNull(user.getAcl());
                assertNotNull(user.getAuthToken());
                Divroll.initialize("WRONG", testApplication.getApiToken());
                user.retrieve().subscribe((divrollUser1, throwable) -> {
                    if(throwable instanceof UnauthorizedRequestException) {
                        finishTest();
                    } else {
                        fail();
                    }
                });
            });
        });
        delayTestFinish(DELAY);
    }

    public void testGetUserInvalidApiToken() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());
            DivrollUser user = new DivrollUser();
            user.create("username", "password").subscribe(divrollUser -> {
                assertNotNull(divrollUser.getEntityId());
                assertNotNull(divrollUser.getAcl());
                assertNotNull(divrollUser.getUsername());
                assertNotNull(divrollUser.getAuthToken());
                Divroll.initialize(testApplication.getAppId(), "WRONG");
                divrollUser.retrieve().subscribe((divrollUser1, throwable) -> {
                    if(throwable instanceof UnauthorizedRequestException) {
                        finishTest();
                    } else {
                        fail();
                    }
                });
            });
        });
        delayTestFinish(DELAY);
    }

    public void testGetUserInvalidMasterKey() throws RequestException {
//        TestData.getNewApplication().subscribe(testApplication -> {
//
//        });
//        delayTestFinish(DELAY);
    }

    public void testCreateAndGetUserWithACLUsingAuthToken() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());
            DivrollUser divrollUser = new DivrollUser();
            divrollUser.create("admin", "password").subscribe(divrollUser1 -> {
                assertNotNull(divrollUser1.getEntityId());
                String userId = divrollUser1.getEntityId();
                DivrollRole divrollRole = new DivrollRole();
                DivrollACL divrollACL = DivrollACL.build();
                divrollACL.setPublicRead(true);
                divrollACL.setAclWrite(Arrays.asList(userId));
                divrollRole.setAcl(divrollACL);
                divrollRole.setName("Admin");
                divrollRole.create().subscribe(divrollRole1 -> {
                    assertNotNull(divrollRole1.getEntityId());
                    assertTrue(divrollRole1.getAcl().getAclWrite().contains("0-0"));
                    assertTrue(divrollRole1.getAcl().getPublicRead());
                    divrollUser1.login("admin", "password", true).subscribe(divrollUser2 -> {
                        assertNotNull(divrollUser2.getAuthToken());
                        assertNotNull(Divroll.getAuthToken());
                        divrollRole1.retrieve().subscribe(divrollRole2 -> {
                            assertNotNull(divrollRole2.getEntityId());
                            assertTrue(divrollRole2.getAcl().getAclWrite().contains("0-0"));
                            assertTrue(divrollRole2.getAcl().getPublicRead());
                            finishTest();
                        });
                    });
                });
            });
        });
        delayTestFinish(DELAY);

    }

    public void testCreateAndGetUserWithACLMissingAuthTokenShouldFail() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());
            DivrollUser divrollAdmin = new DivrollUser();
            divrollAdmin.setAcl(DivrollACL.buildMasterKeyOnly());
            divrollAdmin.create("admin", "password").subscribe(loggedInUser -> {
                assertNotNull(loggedInUser.getEntityId());
                String adminUserId = loggedInUser.getEntityId();
                DivrollUser divrollUser = new DivrollUser();
                DivrollACL acl = new DivrollACL();
                acl.setAclWrite(Arrays.asList(adminUserId));
                acl.setAclRead(Arrays.asList(adminUserId));
                divrollUser.setAcl(acl);
                divrollUser.create("user", "password").subscribe(createdUser -> {
                    assertNotNull(createdUser.getEntityId());
                    assertTrue(createdUser.getAcl().getAclWrite().contains("0-0"));
                    assertTrue(createdUser.getAcl().getAclRead().contains("0-0"));
                    createdUser.retrieve().subscribe((divrollUser1, throwable) -> {
                        if(throwable instanceof UnauthorizedRequestException) {
                            finishTest();
                        } else {
                            fail();
                        }
                    });
                });
            });
        });
        delayTestFinish(DELAY);
    }

    public void testUpdatePublicUserMissingAuthToken() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());
            DivrollUser divrollUser = new DivrollUser();
            divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
            divrollUser.create("username", "password").subscribe(createdUser -> {
                assertNotNull(createdUser.getEntityId());
                assertEquals("username", createdUser.getUsername());
                assertTrue(createdUser.getAcl().getPublicRead());
                assertTrue(createdUser.getAcl().getPublicWrite());
                createdUser.update("new_username", "new_password").subscribe(isUpdated -> {
                    if(isUpdated) {
                        finishTest();
                    } else {
                        fail();
                    }
                });
            });
        });
        delayTestFinish(DELAY);
    }

    public void testUpdateUserWithACLMissingAuthTokenShouldFail() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());
            DivrollUser divrollUser = new DivrollUser();
            divrollUser.create("admin", "password").subscribe(createdAdminUser -> {
                assertNotNull(createdAdminUser.getEntityId());
                DivrollUser user = new DivrollUser();
                DivrollACL divrollACL = DivrollACL.build();
                divrollACL.setAclRead(Arrays.asList(createdAdminUser.getEntityId()));
                divrollACL.setAclWrite(Arrays.asList(createdAdminUser.getEntityId()));
                user.setAcl(divrollACL);
                user.create("username", "password").subscribe(createdUser -> {
                    assertNotNull(createdUser.getEntityId());
                    assertTrue(createdUser.getAcl().getAclWrite().contains("0-0"));
                    assertTrue(createdUser.getAcl().getAclRead().contains("0-0"));
                    createdUser.update("new_username", "new_password").subscribe((aBoolean, throwable) -> {
                        if(throwable instanceof BadRequestException) {
                            finishTest();
                        } else {
                            fail();
                        }
                    });
                });
            });
        });
        delayTestFinish(DELAY);
    }

    public void testUpdatePublicUserUsingAuthToken() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());
            DivrollUser admin = new DivrollUser();
            admin.create("admin", "password").subscribe(createdAdminUser -> {
                assertNotNull(createdAdminUser.getEntityId());
                DivrollUser user = new DivrollUser();
                DivrollACL divrollACL = DivrollACL.build();
                divrollACL.setAclRead(Arrays.asList(createdAdminUser.getEntityId()));
                divrollACL.setAclWrite(Arrays.asList(createdAdminUser.getEntityId()));
                user.setAcl(divrollACL);
                user.create("username", "password").subscribe(createdUser -> {
                    assertNotNull(createdUser.getEntityId());
                    assertTrue(createdUser.getAcl().getAclWrite().contains("0-0"));
                    assertTrue(createdUser.getAcl().getAclRead().contains("0-0"));
                    createdAdminUser.login("admin", "password", true).subscribe(loggedInUser -> {
                        assertNotNull(loggedInUser.getAuthToken());
                        createdUser.update("new_username", "new_password").subscribe(isUpdated -> {
                            if(isUpdated) {
                                finishTest();
                            } else {
                                fail();
                            }
                        });
                    });
                });
            });
        });
        delayTestFinish(DELAY);
    }

    public void testUpdatePublicUserUsingMasterKey() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken(), testApplication.getMasterKey());
            DivrollUser divrollUser = new DivrollUser();
            divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
            divrollUser.create("username", "password").subscribe(createdUser -> {
                assertNotNull(createdUser.getEntityId());
                assertEquals("username", createdUser.getUsername());
                assertTrue(createdUser.getAcl().getPublicRead());
                assertTrue(createdUser.getAcl().getPublicWrite());
                createdUser.update("new_username", "new_password").subscribe(isUpdated -> {
                    if(isUpdated) {
                        finishTest();
                    } else {
                        fail();
                    }
                });
            });
        });
        delayTestFinish(DELAY);
    }

    public void testUpdatePublicUserChangeACLUsingMasterKey() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken(), testApplication.getMasterKey());

            DivrollUser divrollUser = new DivrollUser();
            divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
            divrollUser.create("username", "password").subscribe(createdUser -> {
                assertEquals("username", createdUser.getUsername());
                assertTrue(createdUser.getAcl().getPublicRead());
                assertTrue(createdUser.getAcl().getPublicWrite());

                createdUser.setAcl(DivrollACL.buildMasterKeyOnly());
                createdUser.update().subscribe(isUpdated -> {
                    if(isUpdated) {
                        finishTest();
                    } else {
                        fail();
                    }
                });
            });
        });
        delayTestFinish(DELAY);
    }

    public void testUpdatePublicRoleChangeACLUsingAuthToken() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());
            DivrollUser admin = new DivrollUser();
            admin.create("admin", "password").subscribe(createdAdminUser -> {
                assertNotNull(createdAdminUser.getEntityId());

                DivrollUser divrollUser = new DivrollUser();
                divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
                divrollUser.create("username", "password").subscribe(createdUser -> {
                    assertEquals("username", createdUser.getUsername());
                    assertTrue(createdUser.getAcl().getPublicRead());
                    assertTrue(createdUser.getAcl().getPublicWrite());

                    DivrollACL divrollACL = DivrollACL.build();
                    divrollACL.setAclRead(Arrays.asList(createdAdminUser.getEntityId()));
                    divrollACL.setAclWrite(Arrays.asList(createdAdminUser.getEntityId()));
                    createdUser.setAcl(divrollACL);

                    createdAdminUser.login("admin", "password", true).subscribe(loggedInUser -> {
                        assertNotNull(loggedInUser.getAuthToken());
                        createdUser.update().subscribe(isUpdated -> {
                            if(isUpdated) {
                                finishTest();
                            } else {
                                fail();
                            }
                        });
                    });
                });
            });
        });
        delayTestFinish(DELAY);

    }

    public void testCreateUserWithRole() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());
            DivrollRole role = new DivrollRole("Admin");
            role.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
            role.create().subscribe(createdAdminRole -> {
                assertNotNull(createdAdminRole.getEntityId());
                DivrollUser adminUser = new DivrollUser();
                adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
                adminUser.getRoles().add(createdAdminRole);
                adminUser.create("admin", "password").subscribe(createdAdminUser -> {
                    assertNotNull(createdAdminUser.getEntityId());
                    assertTrue(RoleHelper.contains(createdAdminRole.getEntityId(),createdAdminUser.getRoles()));
                    finishTest();
                });
            });
        });
        delayTestFinish(DELAY);
    }

    public void testCreateUserWithRoles() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());
            DivrollRole role = new DivrollRole("Admin");
            role.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
            role.create().subscribe(createdAdminRole -> {
                DivrollRole managerRole = new DivrollRole("Manager");
                managerRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
                managerRole.create().subscribe(createdManagerRole -> {
                    DivrollUser adminUser = new DivrollUser();
                    adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
                    adminUser.getRoles().add(createdAdminRole);
                    adminUser.getRoles().add(createdManagerRole);
                    adminUser.create("admin", "password").subscribe(createdAdminUser -> {
                        assertNotNull(createdAdminUser.getEntityId());
                        finishTest();
                    });
                });
            });
        });
        delayTestFinish(DELAY);
    }

    public void testCreateUserWithRolesThenUpdateRoleWithoutAuthTokenShouldFail() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());

            DivrollRole adminRole = new DivrollRole("Admin");
            adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
            adminRole.create().subscribe(createdAdminRole -> {
                DivrollRole managerRole = new DivrollRole("Manager");
                managerRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
                managerRole.create().subscribe(createdManagerRole -> {
                    DivrollUser adminUser = new DivrollUser();
                    adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
                    adminUser.getRoles().add(createdAdminRole);
                    adminUser.getRoles().add(createdManagerRole);
                    adminUser.create("admin", "password").subscribe(createdAdminUser -> {
                        assertNotNull(createdAdminUser.getEntityId());
                        assertEquals(2, createdAdminUser.getRoles().size());
                        createdAdminUser.setRoles(Arrays.asList(createdManagerRole)); // change to Manager role only
                        createdAdminUser.update().subscribe((aBoolean, throwable) -> {
                            if(throwable instanceof BadRequestException) {
                                finishTest();
                            } else {
                                fail();
                            }
                        });
                    });
                });
            });
        });
        delayTestFinish(DELAY);
    }

    public void testCreateUserWithRolesThenUpdateRole() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());
            DivrollRole role = new DivrollRole("Admin");
            role.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
            role.create().subscribe(createdAdminRole -> {
                DivrollRole managerRole = new DivrollRole("Manager");
                managerRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
                managerRole.create().subscribe(createdManagerRole -> {
                    DivrollUser adminUser = new DivrollUser();
                    adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
                    adminUser.getRoles().add(createdAdminRole);
                    adminUser.getRoles().add(createdManagerRole);
                    adminUser.create("admin", "password").subscribe(createdAdminUser -> {
                        assertEquals(2, createdAdminUser.getRoles().size());
                        createdAdminUser.login("admin", "password", true).subscribe(loggedInUser -> {
                            assertNotNull(loggedInUser.getEntityId());
                            loggedInUser.setRoles(Arrays.asList(createdManagerRole));
                            loggedInUser.update().subscribe(isUpdated -> {
                                //assertEquals(1, adminUser.getRoles().size());
                                if(isUpdated) {
                                    finishTest();
                                } else {
                                    fail();
                                }
                            });
                        });
                    });
                });
            });
        });
        delayTestFinish(DELAY);
    }

    public void testUpdateUserWithAuthTokenWithRole() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());

            DivrollRole role = new DivrollRole("Admin");
            role.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
            role.create().subscribe(createdAdminRole -> {
                DivrollUser adminUser = new DivrollUser();
                adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
                adminUser.getRoles().add(createdAdminRole);
                adminUser.create("admin", "password").subscribe(createdAdminUser -> {

                    assertNotNull(createdAdminUser.getEntityId());
                    assertFalse(createdAdminUser.getRoles().isEmpty());

                    DivrollUser divrollUser = new DivrollUser();
                    DivrollACL userACL = new DivrollACL();
                    userACL.setPublicRead(true);
                    userACL.setPublicWrite(false);
                    userACL.setAclWrite(Arrays.asList(createdAdminRole.getEntityId()));
                    divrollUser.setAcl(userACL);
                    divrollUser.create("user", "password").subscribe(createdUser -> {
                        assertNotNull(createdUser.getEntityId());

                        createdAdminUser.login("admin", "password", true).subscribe(loggedInUser -> {
                            assertNotNull(createdAdminUser.getAuthToken());
                            assertNotNull(Divroll.getAuthToken());
                            createdUser.update("new_username", "new_password").subscribe(isUpdated -> {
                                if(isUpdated) {
                                    finishTest();
                                } else {
                                    fail();
                                }
                            });

                        });


                    });


                });


            });
        });
        delayTestFinish(DELAY);
    }

    public void testDeletePublicUser() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());
            DivrollUser divrollUser = new DivrollUser();
            divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
            divrollUser.create("username", "password").subscribe(createdUser -> {
                assertNotNull(createdUser.getEntityId());
                createdUser.delete().subscribe(isDeleted -> {
                    createdUser.retrieve().subscribe((divrollUser1, throwable) -> {
                        if(throwable instanceof BadRequestException) {
                            finishTest();
                        } else {
                            fail();
                        }
                    });
                });
            });
        });
        delayTestFinish(DELAY);
    }

    public void testDeletePublicUserWithAuthToken() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());

            DivrollUser adminUser = new DivrollUser();
            adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
            adminUser.create("admin", "password").subscribe(createdAdminUser -> {
                createdAdminUser.login("admin", "password", true).subscribe(loggedInUser -> {
                    DivrollUser divrollUser = new DivrollUser();
                    divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
                    divrollUser.create("user", "password").subscribe(createdUser -> {
                        assertNotNull(divrollUser.getEntityId());
                        createdUser.delete().subscribe(isDeleted -> {
                            if(isDeleted) {
                                finishTest();
                            } else {
                                fail();
                            }
                        });
                    });
                });
            });
        });
        delayTestFinish(DELAY);
    }

    public void testDeletePublicUserWithMasterKey() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken(), testApplication.getMasterKey());

            DivrollRole role = new DivrollRole("Admin");
            role.setAcl(DivrollACL.buildPublicReadWrite());
            role.create().subscribe(createdAdminRole -> {
                assertNotNull(createdAdminRole.getEntityId());

                DivrollUser divrollUser = new DivrollUser();
                divrollUser.setAcl(DivrollACL.buildPublicReadWrite());
                divrollUser.create("username", "password").subscribe(createdUser -> {
                    assertNotNull(createdUser.getEntityId());
                    createdUser.delete().subscribe(isDeleted -> {
                        if(isDeleted) {
                            finishTest();
                        } else {
                            fail();
                        }
                    });
                });
            });
        });
        delayTestFinish(DELAY);


    }

    public void testDeleteUserWithACLWithAuthToken() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());

            DivrollRole adminRole = new DivrollRole("Admin");
            adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
            adminRole.create().subscribe(createdAdminRole -> {
                DivrollUser adminUser = new DivrollUser();
                adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
                adminUser.getRoles().add(createdAdminRole);
                adminUser.create("admin", "password").subscribe(createdAdminUser -> {

                    assertNotNull(createdAdminUser.getRoles());
                    assertFalse(createdAdminUser.getRoles().isEmpty());

                    DivrollUser divrollUser = new DivrollUser();
                    DivrollACL acl = new DivrollACL();
                    acl.setAclWrite(Arrays.asList(createdAdminRole.getEntityId()));
                    divrollUser.setAcl(acl);
                    divrollUser.create("username", "password").subscribe(createdUser -> {
                        assertNotNull(createdUser.getEntityId());
                        assertNotNull(ACLHelper.containsId(adminRole.getEntityId(), createdUser.getAcl().getAclWrite()));
                        createdAdminUser.login("admin", "password", true).subscribe(loggedInUser -> {
                            assertNotNull(loggedInUser.getAuthToken());
                            assertNotNull(Divroll.getAuthToken());
                            divrollUser.delete().subscribe(isDeleted -> {
                                if(isDeleted) {
                                    finishTest();
                                } else {
                                    fail();
                                }
                            });
                        });

                    });


                });


            });


        });
        delayTestFinish(DELAY);

    }

    public void testDeleteUserWithACLWithoutAuthToken() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken());

            DivrollRole adminRole = new DivrollRole("Admin");
            adminRole.setAcl(DivrollACL.buildPublicReadMasterKeyWrite());
            adminRole.create().subscribe(divrollRole -> {
                DivrollUser adminUser = new DivrollUser();
                adminUser.setAcl(DivrollACL.buildMasterKeyOnly());
                adminUser.getRoles().add(adminRole);
                adminUser.create("admin", "password").subscribe(divrollUser -> {
                    assertNotNull(adminUser.getRoles());
                    assertFalse(adminUser.getRoles().isEmpty());
                    DivrollACL acl = new DivrollACL();
                    acl.setAclWrite(Arrays.asList(adminRole.getEntityId()));
                    divrollUser.setAcl(acl);
                    divrollUser.create("username", "password").subscribe(divrollUser1 -> {

                        assertNotNull(divrollUser1.getEntityId());
                        assertNotNull(divrollUser1.getAcl().getAclWrite().contains(adminRole.getEntityId()));

                        divrollUser1.delete().subscribe((divrollUser2, throwable) -> {
                            if(throwable instanceof UnauthorizedRequestException) {
                                finishTest();
                            } else {
                                fail();
                            }
                        });
                    });


                });


            });


        });
        delayTestFinish(DELAY);
    }

}
