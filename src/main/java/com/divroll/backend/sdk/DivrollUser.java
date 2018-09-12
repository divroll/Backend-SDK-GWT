package com.divroll.backend.sdk;

import com.divroll.backend.sdk.helper.JSON;
import com.google.gwt.http.client.RequestException;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Consumer;
import com.divroll.http.client.*;
import com.divroll.http.client.exceptions.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DivrollUser extends DivrollBase
    implements Copyable<DivrollUser> {

    private static final String usersUrl = "/entities/users";
    private static final String loginUrl = "/entities/users/login";

    private String entityId;
    private String username;
    private String password;
    private String authToken;
    private DivrollACL acl;
    private List<DivrollRole> roles;

    public Single<DivrollUser> create(String username, String password) throws RequestException {

        setUsername(username);
        setPassword(password);

        HttpRequestWithBody httpRequestWithBody = HttpClient.post(Divroll.getServerUrl() + usersUrl);
        if(Divroll.getMasterKey() != null) {
            httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
        }
        if(Divroll.getAppId() != null) {
            httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
        }
        if(Divroll.getApiKey() != null) {
            httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
        }
        if(Divroll.getAuthToken() != null) {
            httpRequestWithBody.header("X-Divroll-Auth-Key", Divroll.getAuthToken());
        }
        JSONObject userObj = new JSONObject();
        userObj.put("username", username);
        userObj.put("password", password);
        userObj.put("publicRead", (acl != null && acl.getPublicRead() != null)
                ? acl.getPublicRead() : JSONObject.NULL);
        userObj.put("publicWrite", (acl != null && acl.getPublicWrite() != null)
                ? acl.getPublicWrite() : JSONObject.NULL);
        JSONObject body = new JSONObject();

        JSONArray roles = new JSONArray();
        for(DivrollRole role : getRoles()) {
            JSONObject roleObj = new JSONObject();
            roleObj.put("entityId", role.getEntityId());
            roles.put(roleObj);
        }
        userObj.put("roles", roles);

        body.put("user", userObj);
        httpRequestWithBody.body(body);
        JSONArray aclRead = new JSONArray();
        JSONArray aclWrite = new JSONArray();
        if(acl != null) {
            for(String uuid : acl.getAclRead()) {
                JSONObject entityStub = new JSONObject();
                entityStub.put("entityId", uuid);
                aclRead.put(entityStub);
            }
            for(String uuid : acl.getAclWrite()) {
                JSONObject entityStub = new JSONObject();
                entityStub.put("entityId", uuid);
                aclWrite.put(entityStub);
            }
        }

        httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
        httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
        httpRequestWithBody.header("Content-Type", "application/json");

        return httpRequestWithBody.asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new ServerErrorRequestException();
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 201) {

                JsonNode responseBody = response.getBody();
                JSONObject bodyObj = responseBody.getObject();
                JSONObject responseUser = bodyObj.getJSONObject("user");
                String entityId = responseUser.getString("entityId");
                String webToken = responseUser.getString("webToken");
                setEntityId(entityId);
                setAuthToken(webToken);

                List<String> aclWriteList = null;
                List<String> aclReadList = null;

                Boolean publicRead = null;
                Boolean publicWrite = null;

                try {
                    publicRead = responseUser.get("publicRead") != null ? responseUser.getBoolean("publicRead") : null;
                } catch (Exception e) {}

                try {
                    publicWrite = responseUser.get("publicWrite") != null ? responseUser.getBoolean("publicWrite") : null;
                } catch (Exception e) {}

                try {
                    aclWriteList = JSON.aclJSONArrayToList(responseUser.getJSONArray("aclWrite"));
                } catch (Exception e) {}

                try {
                    aclReadList = JSON.aclJSONArrayToList(responseUser.getJSONArray("aclRead"));
                } catch (Exception e) { }

                try {
                    JSONObject jsonObject = responseUser.getJSONObject("aclWrite");
                    aclWriteList = Arrays.asList(jsonObject.getString("entityId"));
                } catch (Exception e) { }
                try {
                    JSONObject jsonObject = responseUser.getJSONObject("aclRead");
                    aclReadList = Arrays.asList(jsonObject.getString("entityId"));
                } catch (Exception e) { }

                List<DivrollRole> divrollRoles = null;
                try {
                    Object rolesObj = responseUser.get("roles");
                    if(rolesObj instanceof JSONArray) {
                        divrollRoles = new LinkedList<DivrollRole>();
                        JSONArray jsonArray = (JSONArray) rolesObj;
                        for(int i=0;i<jsonArray.length();i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String roleId = jsonObject.getString("entityId");
                            DivrollRole divrollRole = new DivrollRole();
                            divrollRole.setEntityId(roleId);
                            divrollRoles.add(divrollRole);
                        }
                    } else if(rolesObj instanceof JSONObject) {
                        divrollRoles = new LinkedList<DivrollRole>();
                        JSONObject jsonObject = (JSONObject) rolesObj;
                        String roleId = jsonObject.getString("entityId");
                        DivrollRole divrollRole = new DivrollRole();
                        divrollRole.setEntityId(roleId);
                        divrollRoles.add(divrollRole);
                    }
                } catch (Exception e) {
                    // do nothing
                }

                DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
                acl.setPublicWrite(publicWrite);
                acl.setPublicRead(publicRead);
                setAcl(acl);

                setRoles(divrollRoles);

                return copy();
            }
            return null;
        });


    }

    public Single<DivrollUser> retrieve() throws RequestException  {
        return Single.create(new SingleOnSubscribe<DivrollUser>() {
            @Override
            public void subscribe(SingleEmitter<DivrollUser> emitter) throws Exception {
                GetRequest getRequest = (GetRequest) HttpClient.get(Divroll.getServerUrl()
                        + usersUrl + "/" + getEntityId());

                if(Divroll.getMasterKey() != null) {
                    getRequest.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
                }
                if(Divroll.getAppId() != null) {
                    getRequest.header(HEADER_APP_ID, Divroll.getAppId());
                }
                if(Divroll.getApiKey() != null) {
                    getRequest.header(HEADER_API_KEY, Divroll.getApiKey());
                }
                if(Divroll.getAuthToken() != null) {
                    getRequest.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
                }

                Single<HttpResponse<JsonNode>> responseSingle = getRequest.asJson();
                responseSingle.subscribe(new Consumer<HttpResponse<JsonNode>>() {
                    @Override
                    public void accept(HttpResponse<JsonNode> response) throws Exception {
                        if(response.getStatus() >= 500) {
                            emitter.onError(new ServerErrorRequestException());
                        } else if(response.getStatus() == 401) {
                            emitter.onError(new UnauthorizedRequestException(response.getStatusText(), response.getStatus()));
                        } else if(response.getStatus() == 400) {
                            emitter.onError(new BadRequestException(response.getStatusText(), response.getStatus()));
                        }  else if(response.getStatus() >= 400) {
                            emitter.onError(new ClientErrorRequestException(response.getStatusText(), response.getStatus()));
                        } else if(response.getStatus() == 200) {
                            JsonNode body = response.getBody();
                            JSONObject bodyObj = body.getObject();
                            JSONObject userJsonObj = bodyObj.getJSONObject("user");
                            String entityId = userJsonObj.getString("entityId");
                            String username = userJsonObj.getString("username");

                            Boolean publicRead = null;
                            Boolean publicWrite = null;

                            try {
                                publicRead = userJsonObj.get("publicRead") != null ? userJsonObj.getBoolean("publicRead") : null;
                            } catch (Exception e) {

                            }

                            try {
                                publicWrite = userJsonObj.get("publicWrite") != null ? userJsonObj.getBoolean("publicWrite") : null;
                            } catch (Exception e) {

                            }

                            List<String> aclWriteList = null;
                            List<String> aclReadList = null;

                            try {
                                aclWriteList = JSON.aclJSONArrayToList(userJsonObj.getJSONArray("aclWrite"));
                            } catch (Exception e) {

                            }

                            try {
                                aclReadList = JSON.aclJSONArrayToList(userJsonObj.getJSONArray("aclRead"));
                            } catch (Exception e) {

                            }

                            try {
                                aclWriteList = Arrays.asList(userJsonObj.getString("aclWrite"));
                            } catch (Exception e) {

                            }

                            try {
                                aclReadList = Arrays.asList(userJsonObj.getString("aclRead"));
                            } catch (Exception e) {

                            }

                            List<DivrollRole> divrollRoles = null;
                            try {
                                Object roles = userJsonObj.get("roles");
                                if(roles instanceof JSONArray) {
                                    divrollRoles = new LinkedList<DivrollRole>();
                                    JSONArray jsonArray = (JSONArray) roles;
                                    for(int i=0;i<jsonArray.length();i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        String roleId = jsonObject.getString("entityId");
                                        DivrollRole divrollRole = new DivrollRole();
                                        divrollRole.setEntityId(roleId);
                                        divrollRoles.add(divrollRole);
                                    }
                                } else if(roles instanceof JSONObject) {
                                    divrollRoles = new LinkedList<DivrollRole>();
                                    JSONObject jsonObject = (JSONObject) roles;
                                    String roleId = jsonObject.getString("entityId");
                                    DivrollRole divrollRole = new DivrollRole();
                                    divrollRole.setEntityId(roleId);
                                    divrollRoles.add(divrollRole);
                                }
                            } catch (Exception e) {
                                // do nothing
                            }

                            DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
                            acl.setPublicWrite(publicWrite);
                            acl.setPublicRead(publicRead);

                            setEntityId(entityId);
                            setUsername(username);
                            setAcl(acl);
                            setRoles(divrollRoles);

                            emitter.onSuccess(copy());

                        }
                    }
                });



            }
        });

    }

    public Single<Boolean> update(String newUsername, String newPassword) throws RequestException {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(SingleEmitter<Boolean> emitter) throws Exception {
                String completeUrl = Divroll.getServerUrl() + usersUrl + "/" + getEntityId();

                HttpRequestWithBody httpRequestWithBody = HttpClient.put(completeUrl);
                if(Divroll.getMasterKey() != null) {
                    httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
                }
                if(Divroll.getAppId() != null) {
                    httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
                }
                if(Divroll.getApiKey() != null) {
                    httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
                }
                if(Divroll.getAuthToken() != null) {
                    httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
                }
                JSONObject userObj = new JSONObject();

                JSONArray aclRead = new JSONArray();
                JSONArray aclWrite = new JSONArray();
                if(getAcl() != null) {
                    for(String uuid : getAcl().getAclRead()) {
                        JSONObject entityStub = new JSONObject();
                        entityStub.put("entityId", uuid);
                        aclRead.put(entityStub);
                    }
                    for(String uuid : getAcl().getAclWrite()) {
                        JSONObject entityStub = new JSONObject();
                        entityStub.put("entityId", uuid);
                        aclWrite.put(entityStub);
                    }
                }

                userObj.put("aclRead", aclRead);
                userObj.put("aclWrite", aclWrite);

                if(username != null) {
                    userObj.put("username", newUsername);
                }
                if(username != null) {
                    userObj.put("password", newPassword);
                }
                userObj.put("publicRead", (acl != null && acl.getPublicRead() != null)
                        ? acl.getPublicRead() : JSONObject.NULL);
                userObj.put("publicWrite", (acl != null && acl.getPublicWrite() != null)
                        ? acl.getPublicWrite() : JSONObject.NULL);
                JSONObject body = new JSONObject();

                JSONArray roles = new JSONArray();
                for(DivrollRole role : getRoles()) {
                    JSONObject roleObj = new JSONObject();
                    roleObj.put("entityId", role.getEntityId());
                    roles.put(roleObj);
                }
                userObj.put("roles", roles);

                body.put("user", userObj);

                httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
                httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
                httpRequestWithBody.header("Content-Type", "application/json");

                Single<HttpResponse<JsonNode>> responseSingle =  httpRequestWithBody.body(body).asJson();
                responseSingle.subscribe(new Consumer<HttpResponse<JsonNode>>() {
                    @Override
                    public void accept(HttpResponse<JsonNode> response) throws Exception {
                        if(response.getStatus() >= 500) {
                            emitter.onError(new ServerErrorRequestException());
                        } else if(response.getStatus() == 400) {
                            emitter.onError(new BadRequestException(response.getStatusText(), response.getStatus()));
                        } else if(response.getStatus() == 401) {
                            emitter.onError(new UnauthorizedRequestException(response.getStatusText(), response.getStatus()));
                        } else if(response.getStatus() == 404) {
                           emitter.onError(new NotFoundRequestException(response.getStatusText(), response.getStatus()));
                        } else if(response.getStatus() >= 400) {
                            emitter.onError(new ClientErrorRequestException(response.getStatusText(), response.getStatus()));
                        } else if(response.getStatus() == 200) {

                            JsonNode responseBody = response.getBody();
                            JSONObject bodyObj = responseBody.getObject();
                            JSONObject responseUser = bodyObj.getJSONObject("user");
                            String entityId = responseUser.getString("entityId");
                            //String webToken = responseUser.getString("webToken");
                            String updatedUsername = null;

                            try {
                                updatedUsername = responseUser.getString("username");
                            } catch (Exception e) {
                                // do nothing
                            }

                            setEntityId(entityId);
                            //setAuthToken(webToken);

                            Boolean publicRead = null;
                            Boolean publicWrite = null;

                            try {
                                publicRead = responseUser.getBoolean("publicRead");
                            } catch (Exception e) {

                            }

                            try {
                                publicWrite = responseUser.getBoolean("publicWrite");
                            } catch (Exception e) {

                            }

                            List<String> aclWriteList = null;
                            List<String> aclReadList = null;

                            try {
                                aclWriteList = JSON.aclJSONArrayToList(responseUser.getJSONArray("aclWrite"));
                            } catch (Exception e) {

                            }

                            try {
                                aclReadList = JSON.aclJSONArrayToList(responseUser.getJSONArray("aclRead"));
                            } catch (Exception e) {

                            }

                            try {
                                JSONObject jsonObject = responseUser.getJSONObject("aclWrite");
                                aclWriteList = Arrays.asList(jsonObject.getString("entityId"));
                            } catch (Exception e) {

                            }
                            try {
                                JSONObject jsonObject = responseUser.getJSONObject("aclRead");
                                aclReadList = Arrays.asList(jsonObject.getString("entityId"));
                            } catch (Exception e) {

                            }

                            List<DivrollRole> divrollRoles = null;
                            try {
                                Object roleObjects = userObj.get("roles");
                                if(roleObjects instanceof JSONArray) {
                                    divrollRoles = new LinkedList<DivrollRole>();
                                    JSONArray jsonArray = (JSONArray) roles;
                                    for(int i=0;i<jsonArray.length();i++) {
                                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                                        String roleId = jsonObject.getString("entityId");
                                        DivrollRole divrollRole = new DivrollRole();
                                        divrollRole.setEntityId(roleId);
                                        divrollRoles.add(divrollRole);
                                    }
                                } else if(roleObjects instanceof JSONObject) {
                                    divrollRoles = new LinkedList<DivrollRole>();
                                    JSONObject jsonObject = (JSONObject) roleObjects;
                                    String roleId = jsonObject.getString("entityId");
                                    DivrollRole divrollRole = new DivrollRole();
                                    divrollRole.setEntityId(roleId);
                                    divrollRoles.add(divrollRole);
                                }
                            } catch (Exception e) {

                            }

                            DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
                            acl.setPublicRead(publicRead);
                            acl.setPublicWrite(publicWrite);
                            setEntityId(entityId);
                            if(newUsername != null) {
                                setUsername(updatedUsername);
                            }
                            setAcl(acl);
                            setRoles(divrollRoles);

                            emitter.onSuccess(true);
                        }
                    }
                });
            }
        });
    }

    public Single<Boolean> update() throws RequestException {
        return update(null, null);
    }

    public Single<Boolean> delete() throws RequestException {
        return Single.create(new SingleOnSubscribe<Boolean>() {
            @Override
            public void subscribe(SingleEmitter<Boolean> emitter) throws Exception {
                HttpRequestWithBody httpRequestWithBody = HttpClient.delete(Divroll.getServerUrl()
                        + usersUrl + "/" + getEntityId());
                if(Divroll.getMasterKey() != null) {
                    httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
                }
                if(Divroll.getAppId() != null) {
                    httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
                }
                if(Divroll.getApiKey() != null) {
                    httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
                }
                if(Divroll.getAuthToken() != null) {
                    httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
                }

                Single<HttpResponse<JsonNode>> responseSingle = httpRequestWithBody.asJson();
                responseSingle.subscribe(new Consumer<HttpResponse<JsonNode>>() {
                    @Override
                    public void accept(HttpResponse<JsonNode> response) throws Exception {
                        if(response.getStatus() >= 500) {
                            emitter.onError(new ServerErrorRequestException());
                        } else if(response.getStatus() == 401) {
                            emitter.onError(new UnauthorizedRequestException(response.getStatusText(), response.getStatus()));
                        } else if(response.getStatus() >= 400) {
                            emitter.onError(new ClientErrorRequestException(response.getStatusText(), response.getStatus()));
                        } else if(response.getStatus() == 204) {
                            setEntityId(null);
                            setAcl(null);
                            setUsername(null);
                            setRoles(null);
                            setAcl(null);
                            setPassword(null);
                            setAuthToken(null);
                            emitter.onSuccess(true);
                        }
                    }
                });


            }
        });

    }

    public Single<DivrollUser> login(String username, String password) throws RequestException  {
        return Single.create(new SingleOnSubscribe<DivrollUser>() {
            @Override
            public void subscribe(SingleEmitter<DivrollUser> emitter) throws Exception {
                setUsername(username);
                setPassword(password);
                GetRequest getRequest = (GetRequest) HttpClient.get(Divroll.getServerUrl() + loginUrl)
                        .queryString("username", getUsername())
                        .queryString("password", getPassword());
                if(Divroll.getMasterKey() != null) {
                    getRequest.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
                }
                if(Divroll.getAppId() != null) {
                    getRequest.header(HEADER_APP_ID, Divroll.getAppId());
                }
                if(Divroll.getApiKey() != null) {
                    getRequest.header(HEADER_API_KEY, Divroll.getApiKey());
                }
                Single<HttpResponse<JsonNode>> responseSingle = getRequest.asJson();
                responseSingle.subscribe(new Consumer<HttpResponse<JsonNode>>() {
                    @Override
                    public void accept(HttpResponse<JsonNode> response) throws Exception {
                        if(response.getStatus() >= 500) {
                            emitter.onError(new ServerErrorRequestException());
                        } else if(response.getStatus() == 404) {
                            emitter.onError(new NotFoundRequestException(response.getStatusText(), response.getStatus()));
                        } else if(response.getStatus() == 401) {
                            emitter.onError(new UnauthorizedRequestException(response.getStatusText(), response.getStatus()));
                        } else if(response.getStatus() == 200) {
                            JsonNode body = response.getBody();
                            JSONObject bodyObj = body.getObject();
                            JSONObject user = bodyObj.getJSONObject("user");
                            String entityId = user.getString("entityId");
                            String webToken = user.getString("webToken");
                            setEntityId(entityId);
                            setAuthToken(webToken);
                            Divroll.setAuthToken(webToken);
                            emitter.onSuccess(copy());
                        }
                    }
                });
            }
        });


    }

    public void logout() {
        Divroll.setAuthToken(null);
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

    public DivrollACL getAcl() {
        return acl;
    }

    public void setAcl(DivrollACL acl) {
        this.acl = acl;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public List<DivrollRole> getRoles() {
        if(roles == null) {
            roles = new LinkedList<DivrollRole>();
        }
        return roles;
    }

    public void setRoles(List<DivrollRole> roles) {
        this.roles = roles;
    }

    @Override
    public DivrollUser copy() {
        return this;
    }

}
