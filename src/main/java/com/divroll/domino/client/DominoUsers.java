package com.divroll.domino.client;

import com.divroll.domino.client.exception.BadRequestException;
import com.divroll.domino.client.exception.UnauthorizedException;
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

public class DominoUsers extends DominoBase {

    private static final String usersUrl = "/entities/users";

    private List<DominoUser> users;
    private int skip = 0;
    private int limit = 100;

    public List<DominoUser> getUsers() {
        if(users == null) {
            users = new LinkedList<DominoUser>();
        }
        return users;
    }

    public void setUsers(List<DominoUser> users) {
        this.users = users;
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
            String completeUrl = Domino.getServerUrl() + usersUrl;
            System.out.println(completeUrl);
            GetRequest getRequest = (GetRequest) Shape.get(completeUrl);

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
                    getUsers().clear();
                    JSONObject bodyObj = JSONParser.parseStrict(body).isObject();
                    JSONObject roles = bodyObj.get("users").isObject();
                    JSONArray results = new JSONArray();

                    try {
                        results = roles.get("results").isArray();
                    } catch (Exception e) {

                    }

                    try {
                        JSONObject jsonObject = roles.get("results").isObject();
                        results.set(0, jsonObject);
                    } catch (Exception e) {

                    }

                    for(int i=0;i<results.size();i++){
                        JSONObject userObj = results.get(i).isObject();
                        String entityId = userObj.get("entityId").isString().stringValue();
                        String username = userObj.get("username").isString().stringValue();

                        List<String> aclWriteList = null;
                        List<String> aclReadList = null;

                        Boolean publicRead = null;
                        Boolean publicWrite = null;

                        try {
                            publicRead = userObj.get("publicRead").isBoolean().booleanValue();
                        } catch (Exception e) {

                        }

                        try {
                            publicWrite = userObj.get("publicWrite").isBoolean().booleanValue();
                        } catch (Exception e) {

                        }

                        try {
                            aclWriteList = JSON.toList(userObj.get("aclWrite").isArray());
                        } catch (Exception e) {

                        }

                        try {
                            aclWriteList = Arrays.asList(userObj.get("aclWrite").isString().stringValue());
                        } catch (Exception e) {

                        }

                        try {
                            aclReadList = JSON.toList(userObj.get("aclRead").isArray());
                        } catch (Exception e) {

                        }

                        try {
                            aclReadList = Arrays.asList(userObj.get("aclRead").isString().stringValue());
                        } catch (Exception e) {

                        }

                        JSONArray userRoles = null;
                        try {
                            userRoles = userObj.get("roles").isArray();
                        } catch (Exception e) {

                        }

                        List<DominoRole> dominoRoles = null;
                        try {
                            if(userRoles != null) {
                                Object roleObjects = userObj.get("roles");
                                if(roleObjects instanceof JSONArray) {
                                    dominoRoles = new LinkedList<DominoRole>();
                                    for(int j=0;j<userRoles.size();j++) {
                                        JSONObject jsonObject = userRoles.get(j).isObject();
                                        String roleId = jsonObject.get("entityId").isString().stringValue();
                                        DominoRole dominoRole = new DominoRole();
                                        dominoRole.setEntityId(roleId);
                                        dominoRoles.add(dominoRole);
                                    }
                                } else if(roleObjects instanceof JSONObject) {
                                    dominoRoles = new LinkedList<DominoRole>();
                                    JSONObject jsonObject = (JSONObject) roleObjects;
                                    String roleId = jsonObject.get("entityId").isString().stringValue();
                                    DominoRole dominoRole = new DominoRole();
                                    dominoRole.setEntityId(roleId);
                                    dominoRoles.add(dominoRole);
                                }
                            }

                        } catch (Exception e) {
                            // do nothing
                        }

                        DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                        acl.setPublicWrite(publicWrite);
                        acl.setPublicRead(publicRead);

                        DominoUser user = new DominoUser();
                        user.setEntityId(entityId);
                        user.setAcl(acl);
                        user.setRoles(dominoRoles);
                        user.setUsername(username);

                        getUsers().add(user);

                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
