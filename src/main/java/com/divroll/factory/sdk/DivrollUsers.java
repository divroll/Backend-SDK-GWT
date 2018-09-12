package com.divroll.factory.sdk;

import com.divroll.factory.sdk.helper.JSON;
import com.google.gwt.http.client.RequestException;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.functions.Consumer;
import com.divroll.http.client.GetRequest;
import com.divroll.http.client.HttpClient;
import com.divroll.http.client.HttpResponse;
import com.divroll.http.client.JsonNode;
import com.divroll.http.client.exceptions.BadRequestException;
import com.divroll.http.client.exceptions.ClientErrorRequestException;
import com.divroll.http.client.exceptions.ServerErrorRequestException;
import com.divroll.http.client.exceptions.UnauthorizedRequestException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class DivrollUsers extends DivrollBase
    implements Copyable<DivrollUsers> {

    private static final String usersUrl = "/entities/users";

    private List<DivrollUser> users;
    private int skip = 0;
    private int limit = 100;

    public List<DivrollUser> getUsers() {
        if(users == null) {
            users = new LinkedList<DivrollUser>();
        }
        return users;
    }

    public void setUsers(List<DivrollUser> users) {
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

    public Single<DivrollUsers> query() throws RequestException {
        return Single.create(new SingleOnSubscribe<DivrollUsers>() {
            @Override
            public void subscribe(SingleEmitter<DivrollUsers> emitter) throws Exception {
                String completeUrl = Divroll.getServerUrl() + usersUrl;

                GetRequest getRequest = (GetRequest) HttpClient.get(completeUrl);

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

                            getUsers().clear();

                            JsonNode body = response.getBody();

                            JSONObject bodyObj = body.getObject();
                            JSONObject roles = bodyObj.getJSONObject("users");
                            JSONArray results = new JSONArray();

                            try {
                                results = roles.getJSONArray("results");
                            } catch (Exception e) {

                            }

                            try {
                                JSONObject jsonObject = roles.getJSONObject("results");
                                results.put(jsonObject);
                            } catch (Exception e) {

                            }

                            for(int i=0;i<results.length() - 1;i++){
                                JSONObject userObj = results.getJSONObject(i);
                                String entityId = userObj.getString("entityId");
                                String username = userObj.getString("username");
                                Boolean publicRead = userObj.getBoolean("publicRead");
                                Boolean publicWrite = userObj.getBoolean("publicWrite");

                                List<String> aclWriteList = null;
                                List<String> aclReadList = null;

                                try {
                                    aclWriteList = JSON.aclJSONArrayToList(userObj.getJSONArray("aclWrite"));
                                } catch (Exception e) {

                                }

                                try {
                                    aclReadList = JSON.aclJSONArrayToList(userObj.getJSONArray("aclRead"));
                                } catch (Exception e) {

                                }

                                try {
                                    aclWriteList = Arrays.asList(userObj.getString("aclWrite"));
                                } catch (Exception e) {

                                }

                                try {
                                    aclReadList = Arrays.asList(userObj.getString("aclRead"));
                                } catch (Exception e) {

                                }

                                JSONArray userRoles = null;
                                try {
                                    userRoles = userObj.getJSONArray("roles");
                                } catch (Exception e) {

                                }

                                List<DivrollRole> divrollRoles = null;
                                try {
                                    if(userRoles != null) {
                                        Object roleObjects = userObj.get("roles");
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

                                DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
                                acl.setPublicWrite(publicWrite);
                                acl.setPublicRead(publicRead);

                                DivrollUser user = new DivrollUser();
                                user.setEntityId(entityId);
                                user.setAcl(acl);
                                user.setRoles(divrollRoles);
                                user.setUsername(username);

                                getUsers().add(user);

                                emitter.onSuccess(copy());

                            }
                        }
                    }
                });


            }
        });
    }

    @Override
    public DivrollUsers copy() {
        return this;
    }
}
