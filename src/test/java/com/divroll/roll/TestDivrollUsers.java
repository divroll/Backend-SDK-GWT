package com.divroll.roll;

import com.google.gwt.http.client.RequestException;
import com.google.gwt.junit.client.GWTTestCase;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import java.util.Arrays;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestDivrollUsers extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "com.divroll.sdk";
    }

    public void testGetApplication() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());
        assertNotNull(application.getApiToken());
        assertNotNull(application.getAppId());
        assertNotNull(application.getMasterKey());
    }

    public void testGetUsers() throws RequestException {

        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken(), application.getMasterKey());

        DataFactory df = new DataFactory();
        for(int i=0;i<100;i++) {
            DivrollUser divrollUser = new DivrollUser();
            divrollUser.create(df.getEmailAddress(), "password");
        }

        DivrollUsers users = new DivrollUsers();
        users.query();

        assertEquals(100, users.getUsers().size());
    }

    public void testGetUsersMasterKeyOnly() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DataFactory df = new DataFactory();
        for(int i=0;i<100;i++) {
            DivrollUser divrollUser = new DivrollUser();
            divrollUser.setAcl(DivrollACL.buildMasterKeyOnly());
            divrollUser.create(df.getEmailAddress(), "password");
        }

        DivrollUsers users = new DivrollUsers();
        users.query();

        assertEquals(0, users.getUsers().size());
    }

    public void testGetUsersWithACLUsingAuthToken() throws RequestException {
        TestApplication application = TestData.getNewApplication();
        Divroll.initialize(application.getAppId(), application.getApiToken());

        DataFactory df = new DataFactory();

        DivrollUser admin = new DivrollUser();
        admin.setAcl(DivrollACL.buildMasterKeyOnly());
        String adminUsername = df.getEmailAddress();
        admin.create(adminUsername, "password");

        int size = 10;
        for(int i=0;i<size;i++) {
            DivrollUser divrollUser = new DivrollUser();
            DivrollACL acl = new DivrollACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(false);
            acl.setAclRead(Arrays.asList(admin.getEntityId()));
            acl.setAclWrite(Arrays.asList(admin.getEntityId()));
            divrollUser.setAcl(acl);
            divrollUser.create(df.getEmailAddress(), "password");
        }

        DivrollUsers users = new DivrollUsers();
        users.query();

        assertEquals(0, users.getUsers().size());

        admin.login(adminUsername, "password");

        users = new DivrollUsers();
        users.query();

        assertEquals(size, users.getUsers().size());

        size = 20;
        for(int i=0;i<size;i++) {
            DivrollUser divrollUser = new DivrollUser();
            DivrollACL acl = new DivrollACL();
            acl.setPublicWrite(false);
            acl.setPublicRead(true);
            divrollUser.setAcl(acl);
            divrollUser.create(df.getEmailAddress(), "password");
        }

        users = new DivrollUsers();
        users.query();

        assertEquals(30, users.getUsers().size());

        size = 20;
        for(int i=0;i<size;i++) {
            DivrollUser divrollUser = new DivrollUser();
            DivrollACL acl = new DivrollACL();
            acl.setPublicWrite(true);
            acl.setPublicRead(false);
            divrollUser.setAcl(acl);
            divrollUser.create(df.getEmailAddress(), "password");
        }

        users = new DivrollUsers();
        users.query();

        assertEquals(30, users.getUsers().size());
    }

}
