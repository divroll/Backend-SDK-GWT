package com.divroll.domino.client;

import com.divroll.domino.client.helper.JSON;
import com.dotweblabs.shape.client.GetRequest;
import com.dotweblabs.shape.client.HttpRequestException;
import com.dotweblabs.shape.client.Shape;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DominoRoles extends DominoBase {

    private static final String rolesUrl = "/entities/roles";

    private List<DominoRole> roles;
    private int skip;
    private int limit;

    public List<DominoRole> getRoles() {
        if(roles == null) {
            roles = new LinkedList<DominoRole>();
        }
        return roles;
    }

    public void setRoles(List<DominoRole> roles) {
        this.roles = roles;
    }

    public int getSkip() {
        return skip;
    }

    public void setSkip(int skip) {
        this.skip = skip;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void query(DominoCallback callback) {
        try {
            GetRequest getRequest = Shape.get(Domino.getServerUrl()
                    + rolesUrl );

            if(Domino.getMasterKey() != null) {
                getRequest.header(HEADER_MASTER_KEY, Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                getRequest.header(HEADER_APP_ID, Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                getRequest.header(HEADER_API_KEY, Domino.getApiKey());
            }
            if(Domino.getAuthToken() != null) {
                getRequest.header(HEADER_AUTH_TOKEN, Domino.getAuthToken());
            }

            getRequest.asJson(new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable throwable) {
                    callback.failure((HttpRequestException) throwable);
                }
                @Override
                public void onSuccess(String body) {
                    getRoles().clear();
                    JSONObject resultbodyObj = JSONParser.parseStrict(body).isObject();
                    JSONObject roles = resultbodyObj.get("roles").isObject();
                    JSONArray results = roles.get("results").isArray();
                    for(int i=0;i<results.size();i++){
                        JSONObject bodyObj = JSONParser.parseStrict(body).isObject();
                        JSONObject jsonRole = bodyObj.get("role").isObject();
                        String entityId = jsonRole.get("entityId").isString().stringValue();
                        String name = jsonRole.get("name").isString().stringValue();
                        Boolean publicRead = jsonRole.get("publicRead").isBoolean().booleanValue();
                        Boolean publicWrite = jsonRole.get("publicWrite").isBoolean().booleanValue();

                        try {
                            publicRead = jsonRole.get("publicRead").isBoolean().booleanValue();
                        } catch (Exception e) {

                        }

                        try {
                            publicWrite = jsonRole.get("publicWrite").isBoolean().booleanValue();
                        } catch (Exception e) {

                        }

                        List<String> aclWriteList = null;
                        List<String> aclReadList = null;

                        try {
                            aclWriteList = JSON.toList(jsonRole.get("aclWrite").isArray());
                        } catch (Exception e) {

                        }

                        try {
                            aclWriteList = Arrays.asList(jsonRole.get("aclWrite").isString().stringValue());
                        } catch (Exception e) {

                        }

                        try {
                            aclReadList = JSON.toList(jsonRole.get("aclRead").isArray());
                        } catch (Exception e) {

                        }

                        try {
                            aclReadList = Arrays.asList(jsonRole.get("aclRead").isString().stringValue());
                        } catch (Exception e) {

                        }

                        DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                        acl.setPublicWrite(publicWrite);
                        acl.setPublicRead(publicRead);

                        DominoRole role = new DominoRole();
                        role.setEntityId(entityId);
                        role.setName(name);
                        role.setAcl(acl);

                        getRoles().add(role);

                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
