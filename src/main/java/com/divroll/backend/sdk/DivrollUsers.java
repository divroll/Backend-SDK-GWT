package com.divroll.backend.sdk;

import com.divroll.http.client.GetRequest;
import com.divroll.http.client.HttpClient;
import com.divroll.http.client.JsonNode;
import com.divroll.http.client.exceptions.BadRequestException;
import com.divroll.http.client.exceptions.ClientErrorRequestException;
import com.divroll.http.client.exceptions.ServerErrorRequestException;
import com.divroll.http.client.exceptions.UnauthorizedRequestException;
import com.google.gwt.http.client.RequestException;
import elemental.client.Browser;
import io.reactivex.Single;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import static com.divroll.backend.sdk.helper.ACLHelper.aclReadFrom;
import static com.divroll.backend.sdk.helper.ACLHelper.aclWriteFrom;

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

    public Single<DivrollUsers> query()  {
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

        return getRequest.asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new ServerErrorRequestException();
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(), response.getStatus());
            }  else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 200) {

                JsonNode body = response.getBody();

                JSONObject bodyObj = body.getObject();
                JSONObject users = bodyObj.getJSONObject("users");
                JSONArray results = users.getJSONArray("results");
                int skip = users.getNumber("skip").intValue();
                int limit = users.getNumber("limit").intValue();

                if(results == null) {
                    JSONObject singleResultObject = users.getJSONObject("results");
                    if(singleResultObject != null) {
                        results = new JSONArray();
                        results.put(singleResultObject);
                    }
                }

                if(results != null) {
                    for(int i=0;i<results.length();i++){
                        JSONObject userObj = results.getJSONObject(i);
                        String entityId = userObj.getString("entityId");
                        String username = userObj.getString("username");
                        Boolean publicRead = userObj.getBoolean("publicRead");
                        Boolean publicWrite = userObj.getBoolean("publicWrite");

                        List<String> aclWriteList = aclWriteFrom(userObj);
                        List<String> aclReadList =  aclReadFrom(userObj);

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

                        List<DivrollUser> divrollUsers = getUsers();
                        divrollUsers.add(user);
                        setUsers(divrollUsers);
                    }
                    setSkip(skip);
                    setLimit(limit);
                }
            } else {
                setSkip(skip);
                setLimit(limit);
                setUsers(new LinkedList<>());
            }
            return copy();
        });
    }

    @Override
    public DivrollUsers copy() {
        Browser.getWindow().getConsole().log(toString());
        return this;
    }

    @Override
    public String toString() {
        final String[] s = {"["};
        List<DivrollUser> users = getUsers();
        String skip = String.valueOf(getSkip());
        String limit = String.valueOf(getLimit());
        s[0] = s[0] + "className=" + getClass().getName() + "\n";
        s[0] = s[0] + "users=" + users != null ? users.toString() : null + "\n";
        s[0] = s[0] + "skip=" + skip + "\n";
        s[0] = s[0] + "limit=" + limit + "\n";
        s[0] = s[0] + "]" + "\n";
        return s[0];
    }
}
