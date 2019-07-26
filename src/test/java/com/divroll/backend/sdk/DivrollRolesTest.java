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
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DivrollRolesTest extends GWTTestCase {

    public static final int DELAY = 10000;

    @Override
    public String getModuleName() {
        return "com.divroll.backend.sdk";
    }

//    public void testListRolesUsingMasterKey() throws RequestException {
//        TestApplication application = TestData.getNewApplication();
//        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());
//
//        DivrollRole adminRole = new DivrollRole();
//        adminRole.setName("Admin");
//        adminRole.create();
//
//        DivrollRole userRole = new DivrollRole();
//        userRole.setName("User");
//        userRole.create();
//
//        DivrollRole managerRole = new DivrollRole();
//        managerRole.setName("Manager");
//        managerRole.create();
//
//        DivrollRoles divrollRoles = new DivrollRoles();
//        divrollRoles.query();
//
//        assertEquals(3, divrollRoles.getRoles().size());
//        assertEquals("Admin", divrollRoles.getRoles().get(0).getName());
//        assertEquals("User", divrollRoles.getRoles().get(1).getName());
//        assertEquals("Manager", divrollRoles.getRoles().get(2).getName());
//
//    }

    public void testGetPublicRoles() throws RequestException {
        TestData.getNewApplication().subscribe(testApplication -> {
            Divroll.initialize(testApplication.getAppId(), testApplication.getApiToken(), testApplication.getMasterKey());
            assertNotNull(testApplication.getApiToken());
            assertNotNull(testApplication.getAppId());
            assertNotNull(testApplication.getMasterKey());

            DataFactory df = new DataFactory();

            DivrollUser admin = new DivrollUser();
            admin.setAcl(DivrollACL.buildMasterKeyOnly());
            String adminUsername = df.getEmailAddress();
            admin.create(adminUsername, "password").subscribe(createdAdminUser -> {
                int size = 10;
                List<DivrollRole> list = new LinkedList<DivrollRole>();
                for(int i=0;i<size;i++) {
                    DivrollRole role = new DivrollRole();
                    role.setName(df.getRandomWord());
                    DivrollACL acl = new DivrollACL();
                    acl.setPublicWrite(false);
                    acl.setPublicRead(true);
                    acl.setAclWrite(Arrays.asList(admin.getEntityId()));
                    role.setAcl(acl);
                    role.create().subscribe(createdRole -> {
                        list.add(createdRole);
                        if(list.size() == 10) {
                            DivrollRoles roles = new DivrollRoles();
                            roles.query().subscribe(fetchedRoles -> {
                                assertEquals(10, fetchedRoles.getRoles().size());
                                finishTest();
                            });
                        }
                    });
                }
            });
        });
        delayTestFinish(DELAY);
    }

//    public void testGetRolesWithACLUsingAuthToken() throws RequestException {
//        TestApplication application = TestData.getNewApplication();
//        Divroll.initialize(application.getAppId(), application.getApiToken());
//
//        DataFactory df = new DataFactory();
//
//        DivrollUser admin = new DivrollUser();
//        admin.setAcl(DivrollACL.buildMasterKeyOnly());
//        String adminUsername = df.getEmailAddress();
//        admin.create(adminUsername, "password");
//
//        for(int i=0;i<10;i++) {
//            DivrollRole role = new DivrollRole();
//            role.setName(df.getRandomWord());
//            DivrollACL acl = new DivrollACL();
//            acl.setPublicWrite(false);
//            acl.setPublicRead(true);
//            acl.setAclWrite(Arrays.asList(admin.getEntityId()));
//            role.setAcl(acl);
//            role.create();
//        }
//
//        DivrollRoles roles = new DivrollRoles();
//        roles.query();
//        assertEquals(10, roles.getRoles().size());
//
//        for(int i=0;i<10;i++) {
//            DivrollRole role = new DivrollRole();
//            role.setName(df.getRandomWord());
//            DivrollACL acl = new DivrollACL();
//            acl.setPublicWrite(false);
//            acl.setPublicRead(false);
//            role.setAcl(acl);
//            role.create();
//        }
//
//
//        for(int i=0;i<10;i++) {
//            DivrollRole role = new DivrollRole();
//            role.setName(df.getRandomWord());
//            DivrollACL acl = new DivrollACL();
//            acl.setPublicWrite(false);
//            acl.setPublicRead(false);
//            acl.setAclRead(Arrays.asList(admin.getEntityId()));
//            role.setAcl(acl);
//            role.create();
//        }
//
//        admin.login(adminUsername, "password");
//
//        roles.query();
//        assertEquals(20, roles.getRoles().size());
//
//    }

}
