package com.divroll.roll;

import com.google.gwt.http.client.RequestException;
import com.google.gwt.junit.client.GWTTestCase;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.Arrays;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDivrollRoles extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "com.divroll.sdk";
    }

    public void testListRolesUsingMasterKey() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DivrollRole adminRole = new DivrollRole();
        adminRole.setName("Admin");
        adminRole.create();

        DivrollRole userRole = new DivrollRole();
        userRole.setName("User");
        userRole.create();

        DivrollRole managerRole = new DivrollRole();
        managerRole.setName("Manager");
        managerRole.create();

        DivrollRoles divrollRoles = new DivrollRoles();
        divrollRoles.query();

        assertEquals(3, divrollRoles.getRoles().size());
        assertEquals("Admin", divrollRoles.getRoles().get(0).getName());
        assertEquals("User", divrollRoles.getRoles().get(1).getName());
        assertEquals("Manager", divrollRoles.getRoles().get(2).getName());

    }

    public void testGetPublicRoles() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DataFactory df = new DataFactory();

        DivrollUser admin = new DivrollUser();
        admin.setAcl(DivrollACL.buildMasterKeyOnly());
        String adminUsername = df.getEmailAddress();
        admin.create(adminUsername, "password");

        int size = 10;
        for(int i=0;i<size;i++) {
            DivrollRole role = new DivrollRole();
            role.setName(df.getRandomWord());
            DivrollACL acl = new DivrollACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(true);
            acl.setAclWrite(Arrays.asList(admin.getEntityId()));
            role.setAcl(acl);
            role.create();
        }

        DivrollRoles roles = new DivrollRoles();
        roles.query();
        assertEquals(10, roles.getRoles().size());
    }

    public void testGetRolesWithACLUsingAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DataFactory df = new DataFactory();

        DivrollUser admin = new DivrollUser();
        admin.setAcl(DivrollACL.buildMasterKeyOnly());
        String adminUsername = df.getEmailAddress();
        admin.create(adminUsername, "password");

        for(int i=0;i<10;i++) {
            DivrollRole role = new DivrollRole();
            role.setName(df.getRandomWord());
            DivrollACL acl = new DivrollACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(true);
            acl.setAclWrite(Arrays.asList(admin.getEntityId()));
            role.setAcl(acl);
            role.create();
        }

        DivrollRoles roles = new DivrollRoles();
        roles.query();
        assertEquals(10, roles.getRoles().size());

        for(int i=0;i<10;i++) {
            DivrollRole role = new DivrollRole();
            role.setName(df.getRandomWord());
            DivrollACL acl = new DivrollACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(false);
            role.setAcl(acl);
            role.create();
        }


        for(int i=0;i<10;i++) {
            DivrollRole role = new DivrollRole();
            role.setName(df.getRandomWord());
            DivrollACL acl = new DivrollACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(false);
            acl.setAclRead(Arrays.asList(admin.getEntityId()));
            role.setAcl(acl);
            role.create();
        }

        admin.login(adminUsername, "password");

        roles.query();
        assertEquals(20, roles.getRoles().size());

    }

}
