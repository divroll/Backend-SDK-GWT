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

public class DivrollRoles extends DivrollBase
    implements Copyable<DivrollRoles> {

    private static final String rolesUrl = "/entities/roles";

    private List<DivrollRole> roles;
    private int skip;
    private int limit;

    public List<DivrollRole> getRoles() {
        if(roles == null) {
            roles = new LinkedList<DivrollRole>();
        }
        return roles;
    }

    public void setRoles(List<DivrollRole> roles) {
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

    public Single<DivrollRoles> query() throws RequestException {
        return Single.create(new SingleOnSubscribe<DivrollRoles>() {
            @Override
            public void subscribe(SingleEmitter<DivrollRoles> emitter) throws Exception {
                GetRequest getRequest = (GetRequest) HttpClient.get(Divroll.getServerUrl()
                        + rolesUrl);

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
                            emitter.onError( new BadRequestException(response.getStatusText(), response.getStatus()));
                        }  else if(response.getStatus() >= 400) {
                            emitter.onError(new ClientErrorRequestException(response.getStatusText(), response.getStatus()));
                        } else if(response.getStatus() == 200) {

                            getRoles().clear();

                            JsonNode body = response.getBody();
                            JSONObject bodyObj = body.getObject();
                            JSONObject roles = bodyObj.getJSONObject("roles");
                            JSONArray results = roles.getJSONArray("results");
                            for(int i=0;i<results.length();i++){
                                JSONObject roleObj = results.getJSONObject(i);
                                String entityId = roleObj.getString("entityId");
                                String name = roleObj.getString("name");
                                Boolean publicRead = roleObj.getBoolean("publicRead");
                                Boolean publicWrite = roleObj.getBoolean("publicWrite");

                                List<String> aclWriteList = null;
                                List<String> aclReadList = null;

                                try {
                                    aclWriteList = JSON.aclJSONArrayToList(roleObj.getJSONArray("aclWrite"));
                                } catch (Exception e) {

                                }

                                try {
                                    aclReadList = JSON.aclJSONArrayToList(roleObj.getJSONArray("aclRead"));
                                } catch (Exception e) {

                                }

                                try {
                                    aclWriteList = Arrays.asList(roleObj.getString("aclWrite"));
                                } catch (Exception e) {

                                }

                                try {
                                    aclReadList = Arrays.asList(roleObj.getString("aclRead"));
                                } catch (Exception e) {

                                }

                                DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
                                acl.setPublicWrite(publicWrite);
                                acl.setPublicRead(publicRead);

                                DivrollRole role = new DivrollRole();
                                role.setEntityId(entityId);
                                role.setName(name);
                                role.setAcl(acl);

                                getRoles().add(role);

                                emitter.onSuccess(copy());

                            }
                        }
                    }
                });


            }
        });

    }

    @Override
    public DivrollRoles copy() {
        return this;
    }
}
