package com.divroll.domino.client;

import com.divroll.domino.client.helper.JSON;
import com.dotweblabs.shape.client.GetRequest;
import com.dotweblabs.shape.client.HttpRequestException;
import com.dotweblabs.shape.client.HttpRequestWithBody;
import com.dotweblabs.shape.client.Shape;
import com.google.gwt.json.client.*;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DominoUser extends DominoBase {

    private static final String usersUrl = "/entities/users";
    private static final String loginUrl = "/entities/users/login";

    private String entityId;
    private String username;
    private String password;
    private String authToken;
    private DominoACL acl;
    private List<DominoRole> roles;

    public void create(String username, String password, DominoCallback callback) {
        try {

            setUsername(username);
            setPassword(password);

            HttpRequestWithBody httpRequestWithBody = Shape.post(Domino.getServerUrl() + usersUrl);
            if(Domino.getMasterKey() != null) {
                httpRequestWithBody.header(HEADER_MASTER_KEY, Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                httpRequestWithBody.header(HEADER_APP_ID, Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                httpRequestWithBody.header(HEADER_API_KEY, Domino.getApiKey());
            }
            if(Domino.getAuthToken() != null) {
                httpRequestWithBody.header("X-Domino-Auth-Key", Domino.getAuthToken());
            }
            JSONObject userObj = new JSONObject();
            userObj.put("username", new JSONString(username));
            userObj.put("password", new JSONString(password));
            userObj.put("publicRead", (this.acl != null && this.acl.getPublicRead() != null)
                    ? JSONBoolean.getInstance(this.acl.getPublicRead()) : JSONNull.getInstance());
            userObj.put("publicWrite", (this.acl != null && this.acl.getPublicWrite() != null)
                    ? JSONBoolean.getInstance(this.acl.getPublicWrite()) : JSONNull.getInstance());
            JSONObject body = new JSONObject();

            JSONArray roles = new JSONArray();
            int idx = 0;
            for(DominoRole role : getRoles()) {
                JSONObject roleObj = new JSONObject();
                roleObj.put("entityId", new JSONString(role.getEntityId()));
                roles.set(idx, roleObj);
                idx++;
            }
            userObj.put("roles", roles);

            body.put("user", userObj);
            httpRequestWithBody.body(body);
            JSONArray aclRead = new JSONArray();
            JSONArray aclWrite = new JSONArray();
            if(this.acl != null) {
                idx = 0;
                for(String uuid : this.acl.getAclRead()) {
                    aclRead.set(idx, new JSONString(uuid));
                    idx++;
                }
                idx = 0;
                for(String uuid : this.acl.getAclWrite()) {
                    aclWrite.set(idx, new JSONString(uuid));
                    idx++;
                }
            }

            httpRequestWithBody.header("X-Domino-ACL-Read", aclRead.toString());
            httpRequestWithBody.header("X-Domino-ACL-Write", aclWrite.toString());
            httpRequestWithBody.header("Content-Type", "application/json");

            httpRequestWithBody.asJson(new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable throwable) {
                    callback.failure((HttpRequestException) throwable);
                }
                @Override
                public void onSuccess(String body) {
                    JSONObject bodyObj = JSONParser.parseStrict(body).isObject();
                    JSONObject responseUser = bodyObj.get("role").isObject();
                    String entityId = responseUser.get("entityId").isString().stringValue();
                    String webToken = responseUser.get("webToken").isString().stringValue();
                    setEntityId(entityId);
                    setAuthToken(webToken);

                    List<String> aclWriteList = null;
                    List<String> aclReadList = null;

                    Boolean publicRead = null;
                    Boolean publicWrite = null;

                    try {
                        publicRead = responseUser.get("publicRead").isBoolean().booleanValue();
                    } catch (Exception e) {

                    }

                    try {
                        publicWrite = responseUser.get("publicWrite").isBoolean().booleanValue();
                    } catch (Exception e) {

                    }

                    try {
                        aclWriteList = JSON.toList(responseUser.get("aclWrite").isArray());
                    } catch (Exception e) {

                    }

                    try {
                        aclWriteList = Arrays.asList(responseUser.get("aclWrite").isString().stringValue());
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = JSON.toList(responseUser.get("aclRead").isArray());
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = Arrays.asList(responseUser.get("aclRead").isString().stringValue());
                    } catch (Exception e) {

                    }

                    List<DominoRole> dominoRoles = null;
                    try {
                        Object rolesObj = responseUser.get("roles");
                        if(roles instanceof JSONArray) {
                            dominoRoles = new LinkedList<DominoRole>();
                            JSONArray jsonArray = (JSONArray) roles;
                            for(int i=0;i<jsonArray.size();i++) {
                                JSONObject jsonObject = jsonArray.get(i).isObject();
                                String roleId = jsonObject.get("entityId").isString().stringValue();
                                DominoRole dominoRole = new DominoRole();
                                dominoRole.setEntityId(roleId);
                                dominoRoles.add(dominoRole);
                            }
                        } else if(rolesObj instanceof JSONObject) {
                            dominoRoles = new LinkedList<DominoRole>();
                            JSONObject jsonObject = (JSONObject) rolesObj;
                            String roleId = jsonObject.get("entityId").isString().stringValue();
                            DominoRole dominoRole = new DominoRole();
                            dominoRole.setEntityId(roleId);
                            dominoRoles.add(dominoRole);
                        }
                    } catch (Exception e) {
                        // do nothing
                    }

                    DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                    acl.setPublicWrite(publicWrite);
                    acl.setPublicRead(publicRead);
                    setAcl(acl);
                    setRoles(dominoRoles);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void retrieve(DominoCallback callback) {
        try {
            GetRequest getRequest = (GetRequest) Shape.get(Domino.getServerUrl()
                    + usersUrl + "/" + getEntityId());

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
                    JSONObject bodyObj = JSONParser.parseStrict(body).isObject();
                    JSONObject userJsonObj = bodyObj.get("user").isObject();
                    String entityId = userJsonObj.get("entityId").isString().stringValue();
                    String username = userJsonObj.get("username").isString().stringValue();

                    List<String> aclWriteList = null;
                    List<String> aclReadList = null;

                    Boolean publicRead = null;
                    Boolean publicWrite = null;

                    try {
                        publicRead = userJsonObj.get("publicRead").isBoolean().booleanValue();
                    } catch (Exception e) {

                    }

                    try {
                        publicWrite = userJsonObj.get("publicWrite").isBoolean().booleanValue();
                    } catch (Exception e) {

                    }

                    try {
                        aclWriteList = JSON.toList(userJsonObj.get("aclWrite").isArray());
                    } catch (Exception e) {

                    }

                    try {
                        aclWriteList = Arrays.asList(userJsonObj.get("aclWrite").isString().stringValue());
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = JSON.toList(userJsonObj.get("aclRead").isArray());
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = Arrays.asList(userJsonObj.get("aclRead").isString().stringValue());
                    } catch (Exception e) {

                    }

                    List<DominoRole> dominoRoles = null;
                    try {
                        Object roles = userJsonObj.get("roles");
                        if(roles instanceof JSONArray) {
                            dominoRoles = new LinkedList<DominoRole>();
                            JSONArray jsonArray = (JSONArray) roles;
                            for(int i=0;i<jsonArray.size();i++) {
                                JSONObject jsonObject = jsonArray.get(i).isObject();
                                String roleId = jsonObject.get("entityId").isString().stringValue();
                                DominoRole dominoRole = new DominoRole();
                                dominoRole.setEntityId(roleId);
                                dominoRoles.add(dominoRole);
                            }
                        } else if(roles instanceof JSONObject) {
                            dominoRoles = new LinkedList<DominoRole>();
                            JSONObject jsonObject = (JSONObject) roles;
                            String roleId = jsonObject.get("entityId").isString().stringValue();
                            DominoRole dominoRole = new DominoRole();
                            dominoRole.setEntityId(roleId);
                            dominoRoles.add(dominoRole);
                        }
                    } catch (Exception e) {
                        // do nothing
                    }

                    DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                    acl.setPublicWrite(publicWrite);
                    acl.setPublicRead(publicRead);

                    setEntityId(entityId);
                    setUsername(username);
                    setAcl(acl);
                    setRoles(dominoRoles);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(String newUsername, String newPassword, DominoCallback callback) {
        try {

            String completeUrl = Domino.getServerUrl() + usersUrl + "/" + getEntityId();
            HttpRequestWithBody httpRequestWithBody = Shape.put(completeUrl);
            if(Domino.getMasterKey() != null) {
                httpRequestWithBody.header(HEADER_MASTER_KEY, Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                httpRequestWithBody.header(HEADER_APP_ID, Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                httpRequestWithBody.header(HEADER_API_KEY, Domino.getApiKey());
            }
            if(Domino.getAuthToken() != null) {
                httpRequestWithBody.header(HEADER_AUTH_TOKEN, Domino.getAuthToken());
            }
            JSONObject userObj = new JSONObject();
            if(username != null) {
                userObj.put("username", new JSONString(newUsername));
            }
            if(username != null) {
                userObj.put("password", new JSONString(newPassword));
            }
            userObj.put("publicRead", (acl != null && acl.getPublicRead() != null)
                    ? JSONBoolean.getInstance(acl.getPublicRead()) : JSONNull.getInstance());
            userObj.put("publicWrite", (acl != null && acl.getPublicWrite() != null)
                    ? JSONBoolean.getInstance(acl.getPublicWrite()) : JSONNull.getInstance());
            JSONObject body = new JSONObject();

            JSONArray roles = new JSONArray();
            int idx = 0;
            for(DominoRole role : getRoles()) {
                JSONObject roleObj = new JSONObject();
                roleObj.put("entityId", new JSONString(role.getEntityId()));
                roles.set(idx, roleObj);
                idx++;
            }
            userObj.put("roles", roles);

            body.put("user", userObj);

            JSONArray aclRead = new JSONArray();
            JSONArray aclWrite = new JSONArray();
            idx = 0;
            if(acl != null) {
                for(String uuid : acl.getAclRead()) {
                    aclRead.set(idx, new JSONString(uuid));
                    idx++;
                }
                idx = 0;
                for(String uuid : acl.getAclWrite()) {
                    aclWrite.set(idx, new JSONString(uuid));
                    idx++;
                }
            }

            httpRequestWithBody.header("X-Domino-ACL-Read", aclRead.toString());
            httpRequestWithBody.header("X-Domino-ACL-Write", aclWrite.toString());
            httpRequestWithBody.header("Content-Type", "application/json");

            httpRequestWithBody.body(body).asJson(new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable throwable) {
                    callback.failure((HttpRequestException) throwable);
                }
                @Override
                public void onSuccess(String body) {
                    JSONObject bodyObj = JSONParser.parseStrict(body).isObject();
                    JSONObject responseUser = bodyObj.get("user").isObject();
                    String entityId = responseUser.get("entityId").isString().stringValue();
                    //String webToken = responseUser.getString("webToken");
                    String updatedUsername = null;

                    try {
                        updatedUsername = responseUser.get("username").isString().stringValue();
                    } catch (Exception e) {
                        // do nothing
                    }

                    setEntityId(entityId);
                    //setAuthToken(webToken);

                    List<String> aclWriteList = null;
                    List<String> aclReadList = null;

                    Boolean publicRead = null;
                    Boolean publicWrite = null;

                    try {
                        publicRead = responseUser.get("publicRead").isBoolean().booleanValue();
                    } catch (Exception e) {

                    }

                    try {
                        publicWrite = responseUser.get("publicWrite").isBoolean().booleanValue();
                    } catch (Exception e) {

                    }

                    try {
                        aclWriteList = JSON.toList(responseUser.get("aclWrite").isArray());
                    } catch (Exception e) {

                    }

                    try {
                        aclWriteList = Arrays.asList(responseUser.get("aclWrite").isString().stringValue());
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = JSON.toList(responseUser.get("aclRead").isArray());
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = Arrays.asList(responseUser.get("aclRead").isString().stringValue());
                    } catch (Exception e) {

                    }

                    List<DominoRole> dominoRoles = null;
                    try {
                        Object roleObjects = userObj.get("roles");
                        if(roleObjects instanceof JSONArray) {
                            dominoRoles = new LinkedList<DominoRole>();
                            JSONArray jsonArray = (JSONArray) roles;
                            for(int i=0;i<jsonArray.size();i++) {
                                JSONObject jsonObject = jsonArray.get(i).isObject();
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
                    } catch (Exception e) {

                    }

                    DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                    acl.setPublicRead(publicRead);
                    acl.setPublicWrite(publicWrite);
                    setEntityId(entityId);
                    if(newUsername != null) {
                        setUsername(updatedUsername);
                    }
                    setAcl(acl);
                    setRoles(dominoRoles);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update(DominoCallback callback) {
        update(null, null, callback);
    }

    public boolean delete(DominoCallback callback) {
        try {
            HttpRequestWithBody httpRequestWithBody = Shape.delete(Domino.getServerUrl()
                    + usersUrl + "/" + getEntityId());
            if(Domino.getMasterKey() != null) {
                httpRequestWithBody.header(HEADER_MASTER_KEY, Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                httpRequestWithBody.header(HEADER_APP_ID, Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                httpRequestWithBody.header(HEADER_API_KEY, Domino.getApiKey());
            }
            if(Domino.getAuthToken() != null) {
                httpRequestWithBody.header(HEADER_AUTH_TOKEN, Domino.getAuthToken());
            }
            httpRequestWithBody.asJson(new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable throwable) {
                    callback.failure((HttpRequestException) throwable);
                }
                @Override
                public void onSuccess(String s) {
                    setEntityId(null);
                    setAcl(null);
                    setUsername(null);
                    setRoles(null);
                    setAcl(null);
                    setPassword(null);
                    setAuthToken(null);
                    callback.success();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void login(String username, String password, DominoCallback callback) {
        setUsername(username);
        setPassword(password);
        try {
            GetRequest getRequest = (GetRequest) Shape.get(Domino.getServerUrl() + loginUrl)
                    .queryString("username", getUsername())
                    .queryString("password", getPassword());
            if(Domino.getMasterKey() != null) {
                getRequest.header(HEADER_MASTER_KEY, Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                getRequest.header(HEADER_APP_ID, Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                getRequest.header(HEADER_API_KEY, Domino.getApiKey());
            }
            getRequest.asJson(new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable throwable) {

                }
                @Override
                public void onSuccess(String body) {
                    JSONObject bodyObj = JSONParser.parseStrict(body).isObject();
                    JSONObject user = bodyObj.get("user").isObject();
                    String entityId = user.get("entityId").isString().stringValue();
                    String webToken = user.get("webToken").isString().stringValue();
                    setEntityId(entityId);
                    setAuthToken(webToken);
                    Domino.setAuthToken(webToken);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        Domino.setAuthToken(null);
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public DominoACL getAcl() {
        return acl;
    }

    public void setAcl(DominoACL acl) {
        this.acl = acl;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public List<DominoRole> getRoles() {
        if(roles == null) {
            roles = new LinkedList<DominoRole>();
        }
        return roles;
    }

    public void setRoles(List<DominoRole> roles) {
        this.roles = roles;
    }
}
