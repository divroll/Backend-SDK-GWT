package com.divroll.factory.sdk;

import com.divroll.factory.sdk.exception.DivrollException;
import com.divroll.factory.sdk.helper.JSON;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.json.client.JSONNull;
import io.reactivex.Single;
import com.divroll.http.client.GetRequest;
import com.divroll.http.client.HttpClient;
import com.divroll.http.client.HttpRequestWithBody;
import com.divroll.http.client.JsonNode;
import com.divroll.http.client.exceptions.BadRequestException;
import com.divroll.http.client.exceptions.ClientErrorRequestException;
import com.divroll.http.client.exceptions.ServerErrorRequestException;
import com.divroll.http.client.exceptions.UnauthorizedRequestException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class DivrollRole extends DivrollBase
    implements Copyable<DivrollRole> {

    private static final String rolesUrl = "/entities/roles";

    private String entityId;
    private String name;
    private DivrollACL acl;

    public DivrollRole() {}

    public DivrollRole(String name) {
        setName(name);
    }

    public Single<DivrollRole> create() throws RequestException {

        HttpRequestWithBody httpRequestWithBody = HttpClient.post(Divroll.getServerUrl() + rolesUrl);
        if(Divroll.getMasterKey() != null) {
            httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
        }
        if(Divroll.getAppId() != null) {
            httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
        }
        if(Divroll.getApiKey() != null) {
            httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
        }

        JSONObject roleObj = new JSONObject();
        roleObj.put("name", name);
        roleObj.put("publicRead", (acl != null && acl.getPublicRead() != null)
                ? acl.getPublicRead() : JSONObject.NULL);
        roleObj.put("publicWrite", (acl != null && acl.getPublicWrite() != null)
                ? acl.getPublicWrite() : JSONObject.NULL);
        JSONObject body = new JSONObject();
        body.put("role", roleObj);

        JSONArray aclRead = new JSONArray();
        JSONArray aclWrite = new JSONArray();
        if(acl != null) {
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
        roleObj.put("aclRead", getAcl() != null ? aclRead : JSONNull.getInstance());
        roleObj.put("aclWrite", getAcl() != null ? aclWrite : JSONNull.getInstance());

        httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
        httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
        httpRequestWithBody.header("Content-Type", "application/json");

        return httpRequestWithBody.body(body).asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new ServerErrorRequestException();
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 404) {
                throw new DivrollException(response.getStatusText());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(),response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new DivrollException(response.getStatusText());
            } else if(response.getStatus() == 201) {
                JsonNode responseBody = response.getBody();
                JSONObject bodyObj = responseBody.getObject();

                JSONObject role = bodyObj.getJSONObject("role");
                String entityId = role.getString("entityId");
                String name = role.getString("name");

                Boolean publicRead = null;
                Boolean publicWrite = null;

                try {
                    publicRead = role.getBoolean("publicRead");
                } catch (Exception e) {

                }

                try {
                    publicWrite = role.getBoolean("publicWrite");
                } catch (Exception e) {

                }

                List<String> aclWriteList = null;
                List<String> aclReadList = null;

                try {
                    aclWriteList = JSON.aclJSONArrayToList(role.getJSONArray("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = JSON.aclJSONArrayToList(role.getJSONArray("aclRead"));
                } catch (Exception e) {

                }

                try {
                    JSONObject jsonObject = role.getJSONObject("aclWrite");
                    aclWriteList = Arrays.asList(jsonObject.getString("entityId"));
                } catch (Exception e) {

                }
                try {
                    JSONObject jsonObject = role.getJSONObject("aclRead");
                    aclReadList = Arrays.asList(jsonObject.getString("entityId"));
                } catch (Exception e) {

                }

                DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
                acl.setPublicRead(publicRead);
                acl.setPublicWrite(publicWrite);
                setEntityId(entityId);
                setName(name);
                setAcl(acl);
            }
            return copy();
        });

    }
    public Single<Boolean> update() throws RequestException {
        HttpRequestWithBody httpRequestWithBody = HttpClient.put(Divroll.getServerUrl() + rolesUrl + "/" + getEntityId());
        if(Divroll.getMasterKey() != null) {
            httpRequestWithBody.header(HEADER_MASTER_KEY, Divroll.getMasterKey());
        }
        if(Divroll.getAppId() != null) {
            httpRequestWithBody.header(HEADER_APP_ID, Divroll.getAppId());
        }
        if(Divroll.getApiKey() != null) {
            httpRequestWithBody.header(HEADER_API_KEY, Divroll.getApiKey());
        }

        JSONObject roleObj = new JSONObject();

        JSONArray aclRead = new JSONArray();
        JSONArray aclWrite = new JSONArray();
        if(acl != null) {
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
        roleObj.put("aclRead", aclRead);
        roleObj.put("aclWrite", aclWrite);
        roleObj.put("name", name);
        roleObj.put("publicRead", (acl != null && acl.getPublicRead() != null)
                ? acl.getPublicRead() : JSONObject.NULL);
        roleObj.put("publicWrite", (acl != null && acl.getPublicWrite() != null)
                ? acl.getPublicWrite() : JSONObject.NULL);
        JSONObject body = new JSONObject();
        body.put("role", roleObj);


        httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
        httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
        httpRequestWithBody.header("Content-Type", "application/json");

       return httpRequestWithBody.body(body).asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new ServerErrorRequestException();
            } else if(response.getStatus() == 200) {
                return true;
            }
            return false;
       });

    }

    public Single<Boolean> delete() throws RequestException {
        HttpRequestWithBody httpRequestWithBody = HttpClient.delete(Divroll.getServerUrl()
                + rolesUrl + "/" + getEntityId());
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
            httpRequestWithBody.header(HEADER_AUTH_TOKEN, Divroll.getApiKey());
        }
        return httpRequestWithBody.asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new ServerErrorRequestException();
            } else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 200) {
                return true;
            }
            return false;
        });
    }

    public Single<DivrollRole> retrieve() throws RequestException  {
        GetRequest getRequest = (GetRequest) HttpClient.get(Divroll.getServerUrl()
                + rolesUrl + "/" + getEntityId());

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
                throwException(response);
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(), response.getStatus());
            }  else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 200) {
                JsonNode body = response.getBody();
                JSONObject bodyObj = body.getObject();
                JSONObject role = bodyObj.getJSONObject("role");
                String entityId = role.getString("entityId");
                String name = role.getString("name");
                Boolean publicRead = role.getBoolean("publicRead");
                Boolean publicWrite = role.getBoolean("publicWrite");
                setEntityId(entityId);
                setName(name);

                List<String> aclWriteList = null;
                List<String> aclReadList = null;

                try {
                    aclWriteList = JSON.aclJSONArrayToList(role.getJSONArray("aclWrite"));
                } catch (Exception e) {

                }

                try {
                    aclReadList = JSON.aclJSONArrayToList(role.getJSONArray("aclRead"));
                } catch (Exception e) {

                }

                try {
                    JSONObject jsonObject = role.getJSONObject("aclWrite");
                    aclWriteList = Arrays.asList(jsonObject.getString("entityId"));
                } catch (Exception e) {

                }
                try {
                    JSONObject jsonObject = role.getJSONObject("aclRead");
                    aclReadList = Arrays.asList(jsonObject.getString("entityId"));
                } catch (Exception e) {

                }

                DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
                acl.setPublicWrite(publicWrite);
                acl.setPublicRead(publicRead);
                setEntityId(entityId);
                setName(name);
                setAcl(acl);
            }
            return copy();
        });
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DivrollACL getAcl() {
        return acl;
    }

    public void setAcl(DivrollACL acl) {
        this.acl = acl;
    }

    @Override
    public DivrollRole copy() {
        return this;
    }
}
