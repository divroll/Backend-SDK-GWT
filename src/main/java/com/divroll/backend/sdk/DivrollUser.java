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

import com.divroll.backend.sdk.helper.DivrollEntityHelper;
import com.google.gwt.user.client.Cookies;
import elemental.client.Browser;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Consumer;
import com.divroll.http.client.*;
import com.divroll.http.client.exceptions.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static com.divroll.backend.sdk.helper.ACLHelper.aclReadFrom;
import static com.divroll.backend.sdk.helper.ACLHelper.aclWriteFrom;
import static com.divroll.backend.sdk.helper.RoleHelper.rolesFrom;

public class DivrollUser extends LinkableDivrollBase
    implements Copyable<DivrollUser> {

    private static final String usersUrl = "/entities/users";
    private static final String loginUrl = "/entities/users/login";
    private static final String QUERY_INCLUDE = "include";

    private String entityId;
    private String username;
    private String email;
    private String password;
    private String authToken;
    private DivrollACL acl;
    private List<DivrollRole> roles;

    private String dateCreated;
    private String dateUpdated;

    private List<String> includes;
    private List<DivrollLink> links;

//    private JSONObject linkedEntity;
//    private JSONArray linkedEntities;

    private List<String> blobNames;

    public Single<DivrollUser> create(String email, String username, String password,
                                      String linkName, DivrollEntity linkedEntity, String backlinkName) {
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
            httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
        }
        if(Divroll.getNamespace() != null) {
            httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }

        if(authToken != null && !authToken.isEmpty()) {
            httpRequestWithBody.queryString("authToken", authToken);
        }

        JSONObject body = new JSONObject();
        JSONObject userObj = new JSONObject();

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

        userObj.put("aclRead", aclRead);
        userObj.put("aclWrite", aclWrite);
        userObj.put("email", email);
        userObj.put("username", username);
        userObj.put("password", password);
        userObj.put("publicRead", (acl != null && acl.getPublicRead() != null)
                ? acl.getPublicRead() : JSONObject.NULL);
        userObj.put("publicWrite", (acl != null && acl.getPublicWrite() != null)
                ? acl.getPublicWrite() : JSONObject.NULL);

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

        httpRequestWithBody.queryString("linkName", linkName);
        httpRequestWithBody.queryString("backlinkName", backlinkName);
        httpRequestWithBody.queryString("entityType", linkedEntity.getEntityType());
        httpRequestWithBody.queryString("entity", DivrollEntityHelper.convert(linkedEntity).toString());

        Browser.getWindow().getConsole().log("CREATE BODY=" + body);
        httpRequestWithBody.body(body);

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
                String authToken = responseUser.getString("authToken");
                setEntityId(entityId);
                setAuthToken(authToken);

                Boolean publicRead = responseUser.get("publicRead") != null ? responseUser.getBoolean("publicRead") : null;
                Boolean publicWrite = responseUser.get("publicWrite") != null ? responseUser.getBoolean("publicWrite") : null;
                List<String> aclWriteList = aclWriteFrom(responseUser);
                List<String> aclReadList =  aclReadFrom(responseUser);

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

    public Single<DivrollUser> create(String username, String password)  {

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
            httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
        }
        if(Divroll.getNamespace() != null) {
            httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }
        JSONObject body = new JSONObject();
        JSONObject userObj = new JSONObject();

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

        userObj.put("aclRead", aclRead);
        userObj.put("aclWrite", aclWrite);
        userObj.put("username", username);
        userObj.put("password", password);
        userObj.put("publicRead", (acl != null && acl.getPublicRead() != null)
                ? acl.getPublicRead() : JSONObject.NULL);
        userObj.put("publicWrite", (acl != null && acl.getPublicWrite() != null)
                ? acl.getPublicWrite() : JSONObject.NULL);

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

        Browser.getWindow().getConsole().log("CREATE BODY=" + body);
        httpRequestWithBody.body(body);

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
                String authToken = responseUser.getString("authToken");
                setEntityId(entityId);
                setAuthToken(authToken);

                Boolean publicRead = responseUser.get("publicRead") != null ? responseUser.getBoolean("publicRead") : null;
                Boolean publicWrite = responseUser.get("publicWrite") != null ? responseUser.getBoolean("publicWrite") : null;
                List<String> aclWriteList = aclWriteFrom(responseUser);
                List<String> aclReadList =  aclReadFrom(responseUser);

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

    public Single<DivrollUser> create(String email, String username, String password)  {

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
            httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getAuthToken());
        }
        if(Divroll.getNamespace() != null) {
            httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }
        JSONObject body = new JSONObject();
        JSONObject userObj = new JSONObject();

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

        userObj.put("aclRead", aclRead);
        userObj.put("aclWrite", aclWrite);
        userObj.put("email", email);
        userObj.put("username", username);
        userObj.put("password", password);
        userObj.put("publicRead", (acl != null && acl.getPublicRead() != null)
                ? acl.getPublicRead() : JSONObject.NULL);
        userObj.put("publicWrite", (acl != null && acl.getPublicWrite() != null)
                ? acl.getPublicWrite() : JSONObject.NULL);

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

        Browser.getWindow().getConsole().log("CREATE BODY=" + body);
        httpRequestWithBody.body(body);

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
                String authToken = responseUser.getString("authToken");
                setEntityId(entityId);
                setAuthToken(authToken);

                Boolean publicRead = responseUser.get("publicRead") != null ? responseUser.getBoolean("publicRead") : null;
                Boolean publicWrite = responseUser.get("publicWrite") != null ? responseUser.getBoolean("publicWrite") : null;
                List<String> aclWriteList = aclWriteFrom(responseUser);
                List<String> aclReadList =  aclReadFrom(responseUser);

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

    public Single<DivrollUser> retrieve()   {
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
                if(Divroll.getNamespace() != null) {
                    getRequest.header(HEADER_NAMESPACE, Divroll.getNamespace());
                }

                if(includes != null && !includes.isEmpty()) {
                    JSONArray linkNameArray = new JSONArray();
                    for(String linkName : includes) {
                        linkNameArray.put(linkName);
                    }
                    getRequest.queryString(QUERY_INCLUDE, linkNameArray.toString());
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
                            String email = userJsonObj.getString("email");

                            Boolean publicRead = userJsonObj.get("publicRead") != null ? userJsonObj.getBoolean("publicRead") : null;
                            Boolean publicWrite = userJsonObj.get("publicWrite") != null ? userJsonObj.getBoolean("publicWrite") : null;
                            List<String> aclWriteList = aclWriteFrom(userJsonObj);
                            List<String> aclReadList =  aclReadFrom(userJsonObj);
                            List<DivrollRole> divrollRoles = rolesFrom(userJsonObj);

                            DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
                            acl.setPublicWrite(publicWrite);
                            acl.setPublicRead(publicRead);

                            setEntityId(entityId);
                            setUsername(username);
                            setEmail(email);
                            setAcl(acl);
                            setRoles(divrollRoles);

                            String dateCreated = userJsonObj.getString("dateCreated");
                            String dateUpdated = userJsonObj.getString("dateUpdated");
                            setDateCreated(dateCreated);
                            setDateUpdated(dateUpdated);

                            JSONArray links = userJsonObj.getJSONArray("links");
                            if(links != null) {
                                for(int i=0;i<links.length();i++) {
                                    JSONObject linksObj = links.getJSONObject(i);
                                    DivrollLink link = processLink(linksObj);
                                    getLinks().add(link);
                                }
                            } else {
                                JSONObject linksObj = userJsonObj.getJSONObject("links");
                                DivrollLink link = processLink(linksObj);
                                getLinks().add(link);
                            }

                            JSONArray blobNamesArray = userJsonObj.getJSONArray("blobNames");
                            if(blobNamesArray != null) {
                                for(int i=0;i<blobNamesArray.length();i++) {
                                    String blobName = blobNamesArray.getString(i);
                                    getBlobNames().add(blobName);
                                }
                            }

                            emitter.onSuccess(copy());

                        }
                    }
                });



            }
        });

    }

    public Single<Boolean> update(String newUsername, String newPassword) {
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
                            //String authToken = responseUser.getString("authToken");
                            String updatedUsername = null;

                            try {
                                updatedUsername = responseUser.getString("username");
                            } catch (Exception e) {
                                // do nothing
                            }

                            setEntityId(entityId);
                            //setAuthToken(authToken);

                            Boolean publicRead = responseUser.getBoolean("publicRead");;
                            Boolean publicWrite = responseUser.getBoolean("publicWrite");
                            List<String> aclWriteList = aclWriteFrom(responseUser);
                            List<String> aclReadList =  aclReadFrom(responseUser);
                            List<DivrollRole> divrollRoles = rolesFrom(userObj);

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

    public Single<Boolean> update()  {
        return update(null, null);
    }

    public Single<Boolean> delete()  {
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
                if(Divroll.getNamespace() != null) {
                    httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNamespace());
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

    public static Single<DivrollUser> currentUser() {
        if(Divroll.getCurrentUser() != null) {
            return Single.create(e -> {
               e.onSuccess(Divroll.getCurrentUser());
            });
        } else {
            DivrollUser divrollUser = new DivrollUser();
            return divrollUser.login();
        }
    }

    public Single<DivrollUser> login() {

        GetRequest getRequest = (GetRequest) HttpClient.get(Divroll.getServerUrl() + loginUrl);
        if(Divroll.getMasterKey() != null) {
            getRequest.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
        }
        if(Divroll.getAppId() != null) {
            getRequest.header(HEADER_APP_ID, Divroll.getAppId());
        }
        if(Divroll.getApiKey() != null) {
            getRequest.header(HEADER_API_KEY, Divroll.getApiKey());
        }
        if(Divroll.getNamespace() != null) {
            getRequest.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }
        String authToken = Divroll.getAuthToken() != null
                ? Divroll.getAuthToken() : Cookies.getCookie("authToken");
        if(authToken == null) {
            return Single.create(e -> {
                e.onError(new IllegalArgumentException("Missing auth token error"));
            });
        }
        boolean remember = false;
        if(authToken != null) {
            remember = true;
            getRequest.header(HEADER_AUTH_TOKEN, authToken);
        }

        boolean finalRemember = remember;
        return getRequest.asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new ServerErrorRequestException();
            } else if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 200) {
                JsonNode body = response.getBody();
                JSONObject bodyObj = body.getObject();
                JSONObject user = bodyObj.getJSONObject("user");
                String entityId = user.getString("entityId");
                //String authToken = user.getString("authToken");
                String username = user.getString("username");

                Boolean publicRead = user.getBoolean("publicRead");
                Boolean publicWrite = user.getBoolean("publicWrite");

                List<String> aclWriteList = aclWriteFrom(user);
                List<String> aclReadList =  aclReadFrom(user);

                JSONArray userRoles = null;
                try {
                    userRoles = user.getJSONArray("roles");
                } catch (Exception e) {

                }

                List<DivrollRole> divrollRoles = null;
                try {
                    if(userRoles != null) {
                        Object roleObjects = user.get("roles");
                        if(roleObjects instanceof JSONArray) {
                            divrollRoles = new LinkedList<DivrollRole>();
                            for(int j=0;j<userRoles.length();j++) {
                                JSONObject jsonObject = userRoles.getJSONObject(j);
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
                    }

                } catch (Exception e) {
                    // do nothing
                }

                setRoles(divrollRoles);

                DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
                acl.setPublicWrite(publicWrite);
                acl.setPublicRead(publicRead);

                setEntityId(entityId);
                setAuthToken(authToken);
                setUsername(username);
                setAcl(acl);

                Divroll.setCurrentUser(copy());
                Divroll.setAuthToken(authToken);
                if(finalRemember) {
                    Cookies.setCookie("authToken", authToken);
                }

            }
            return copy();
        });
    }

//    public Single<DivrollUser> login(boolean remember) {
//        return login(Divroll.getAuthToken(), remember);
//    }

    public Single<DivrollUser> login(String username, String password, boolean remember)   {
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
        if(Divroll.getNamespace() != null) {
            getRequest.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }
        return getRequest.asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new ServerErrorRequestException();
            } else if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 200) {
                JsonNode body = response.getBody();
                JSONObject bodyObj = body.getObject();
                JSONObject user = bodyObj.getJSONObject("user");
                String entityId = user.getString("entityId");
                String authToken = user.getString("authToken");
                setEntityId(entityId);
                setAuthToken(authToken);
                Divroll.setAuthToken(authToken);
                Divroll.setCurrentUser(copy());
                if(remember) {
                    Cookies.setCookie("authToken", authToken);
                }
            }
            return copy();
        });
    }

    public static void logout() {
        Cookies.removeCookie("authToken");
        Divroll.setCurrentUser(null);
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
    public String toString() {
        final String[] s = {""};
        String entityId = getEntityId();
        String username = getUsername();
        String password = getPassword();
        String authToken = getAuthToken();
        String acl = String.valueOf(getAcl());
        s[0] = s[0] + "className=" + getClass().getName() + "\n";
        s[0] = s[0] + "entityId=" + entityId + "\n";
        s[0] = s[0] + "email=" + email + "\n";
        s[0] = s[0] + "username=" + username + "\n";
        s[0] = s[0] + "password=" + password + "\n";
        s[0] = s[0] + "authToken=" + authToken + "\n";
        s[0] = s[0] + "acl=" + acl + "\n";
        getRoles().forEach(divrollRole -> { s[0] = s[0] + String.valueOf(divrollRole) + "\n";});
        return s[0];
    }

    @Override
    public DivrollUser copy() {
        Browser.getWindow().getConsole().log(toString());
        return this;
    }

    public List<String> getBlobNames() {
        if(blobNames == null) {
            blobNames = new LinkedList<>();
        }
        return blobNames;
    }

    private void setBlobNames(List<String> blobNames) {
        this.blobNames = blobNames;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(String dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public Single<Boolean> setLink(String linkName, String entityId) {
        if(entityId == null) {
            throw new IllegalArgumentException("Save the entity first before creating a link");
        }
        HttpRequestWithBody httpRequestWithBody = HttpClient.post(Divroll.getServerUrl()
                + usersUrl + "/" + getEntityId() + "/links/" + linkName + "/" + entityId);
        httpRequestWithBody.queryString("linkType", "set");
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
        if(Divroll.getNamespace() != null) {
            httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }
        if(Divroll.getNamespace() != null) {
            httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }
        JSONArray aclRead = new JSONArray();
        JSONArray aclWrite = new JSONArray();
        if(acl != null) {
            for(String uuid : this.acl.getAclRead()) {
                JSONObject entityStub = new JSONObject();
                entityStub.put("entityId", uuid);
                aclRead.put(entityStub);
            }
            for(String uuid : this.acl.getAclWrite()) {
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
            } else if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 201) {
                return true;
            }
            return false;
        });
    }

    public Single<Boolean> addLink(String linkName, String entityId)   {
        if(entityId == null) {
            throw new IllegalArgumentException("Save the entity first before creating a link");
        }
        HttpRequestWithBody httpRequestWithBody = HttpClient.post(Divroll.getServerUrl()
                + usersUrl + "/" + getEntityId() + "/links/" + linkName + "/" + entityId);
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
        if(Divroll.getNamespace() != null) {
            httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }
        if(Divroll.getNamespace() != null) {
            httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }
        JSONArray aclRead = new JSONArray();
        JSONArray aclWrite = new JSONArray();
        if(acl != null) {
            for(String uuid : this.acl.getAclRead()) {
                JSONObject entityStub = new JSONObject();
                entityStub.put("entityId", uuid);
                aclRead.put(entityStub);
            }
            for(String uuid : this.acl.getAclWrite()) {
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
            } else if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 201) {
                return true;
            }
            return false;
        });

    }

    public Single<Boolean> removeLink(String linkName, String entityId)   {
        if(entityId == null) {
            throw new IllegalArgumentException("Save the entity first before removing a link");
        }
        HttpRequestWithBody httpRequestWithBody = HttpClient.delete(Divroll.getServerUrl()
                + usersUrl + "/" + getEntityId() + "/links/" + linkName + "/" + entityId);
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
        if(Divroll.getNamespace() != null) {
            httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }
        JSONArray aclRead = new JSONArray();
        JSONArray aclWrite = new JSONArray();
        if(acl != null) {
            for(String uuid : this.acl.getAclRead()) {
                JSONObject entityStub = new JSONObject();
                entityStub.put("entityId", uuid);
                aclRead.put(entityStub);
            }
            for(String uuid : this.acl.getAclWrite()) {
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
                throw new ServerErrorRequestException(response.getStatusText(),response.getStatus());
            } else if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 200) {
                return true;
            }
            return false;
        });

    }

    public Single<Boolean> removeLinks(String linkName)   {
        if(entityId == null) {
            throw new IllegalArgumentException("Save the entity first before removing links");
        }
        HttpRequestWithBody httpRequestWithBody = HttpClient.delete(Divroll.getServerUrl()
                + usersUrl + "/" + getEntityId() + "/links/" + linkName);
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
        if(Divroll.getNamespace() != null) {
            httpRequestWithBody.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }

        JSONArray aclRead = new JSONArray();
        JSONArray aclWrite = new JSONArray();
        if(acl != null) {
            for(String uuid : this.acl.getAclRead()) {
                JSONObject entityStub = new JSONObject();
                entityStub.put("entityId", uuid);
                aclRead.put(entityStub);
            }
            for(String uuid : this.acl.getAclWrite()) {
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
            } else if(response.getStatus() == 404){
                throw new NotFoundRequestException(response.getStatusText(),response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(),response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(),response.getStatus());
            } else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException(response.getStatusText(),response.getStatus());
            } else if(response.getStatus() == 200) {
                return true;
            }
            return false;
        });
    }

    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    public List<DivrollLink> getLinks() {
        if(links == null) {
            links = new LinkedList<>();
        }
        return links;
    }

    public DivrollLink getFirstLink() {
        if(links != null) {
            return links.iterator().next();
        }
        return null;
    }

}
