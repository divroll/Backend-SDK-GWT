package com.divroll.domino.client;

import com.divroll.domino.client.exception.BadRequestException;
import com.divroll.domino.client.exception.DominoException;
import com.divroll.domino.client.exception.UnauthorizedException;
import com.divroll.domino.client.helper.JSON;
import com.dotweblabs.shape.client.GetRequest;
import com.dotweblabs.shape.client.HttpRequestException;
import com.dotweblabs.shape.client.HttpRequestWithBody;
import com.dotweblabs.shape.client.Shape;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Arrays;
import java.util.List;

public class DominoRole extends DominoBase {

    private static final String rolesUrl = "/entities/roles";

    private String entityId;
    private String name;
    private DominoACL acl;

    public DominoRole() {}

    public DominoRole(String name) {
        setName(name);
    }

    public void create(DominoCallback callback) {
        HttpRequestWithBody httpRequestWithBody = Shape.post(Domino.getServerUrl() + rolesUrl);

        if(Domino.getMasterKey() != null) {
            httpRequestWithBody.header(HEADER_MASTER_KEY, Domino.getMasterKey());
        }
        if(Domino.getAppId() != null) {
            httpRequestWithBody.header(HEADER_APP_ID, Domino.getAppId());
        }
        if(Domino.getApiKey() != null) {
            httpRequestWithBody.header(HEADER_API_KEY, Domino.getApiKey());
        }

        JSONObject roleObj = new JSONObject();
        roleObj.put("name", new JSONString(name));
        roleObj.put("aclRead", getAcl() != null ? JSON.toJSONArray(getAcl().getAclRead()) : null);
        roleObj.put("aclWrite", getAcl() != null ? JSON.toJSONArray(getAcl().getAclWrite()) : null);
        roleObj.put("publicRead", (acl != null && acl.getPublicRead() != null)
                ? JSONBoolean.getInstance(acl.getPublicRead()) : JSONNull.getInstance());
        roleObj.put("publicWrite", (acl != null && acl.getPublicWrite() != null)
                ? JSONBoolean.getInstance(acl.getPublicWrite()) : JSONNull.getInstance());
        JSONObject body = new JSONObject();
        body.put("role", roleObj);

        JSONArray aclRead = new JSONArray();
        JSONArray aclWrite = new JSONArray();
        if(acl != null) {
            int idx = 0;
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

        System.out.println("REQUEST: " + body.toString());

        httpRequestWithBody.body(body).asJson(new AsyncCallback<String>() {
            @Override
            public void onFailure(Throwable throwable) {
                HttpRequestException exception = (HttpRequestException) throwable;
                GWT.log("Error: "  + exception.getMessage());
                GWT.log("Status: "  + exception.getCode());
                callback.failure((HttpRequestException) throwable);
            }
            @Override
            public void onSuccess(String body) {

                if(checkStatus(body) != null
                        && checkStatus(body).getCode() >= 400) {
                    callback.failure(new HttpRequestException(checkStatus(body).getMessage(), checkStatus(body).getCode()));
                    return;
                }

                JSONObject bodyObj = JSONParser.parseStrict(body).isObject();

                JSONObject role = bodyObj.get("role").isObject();
                String entityId = role.get("entityId").isString().stringValue();
                String name = role.get("name").isString().stringValue();

                Boolean publicRead = null;
                Boolean publicWrite = null;

                try {
                    publicRead = role.get("publicRead").isBoolean().booleanValue();
                } catch (Exception e) {

                }

                try {
                    publicWrite = role.get("publicWrite").isBoolean().booleanValue();
                } catch (Exception e) {

                }

                List<String> aclWriteList = null;
                List<String> aclReadList = null;

                try {
                    aclWriteList = JSON.toList(role.get("aclWrite").isArray());
                } catch (Exception e) {

                }

                try {
                    aclWriteList = Arrays.asList(role.get("aclWrite").isString().stringValue());
                } catch (Exception e) {

                }

                try {
                    aclReadList = JSON.toList(role.get("aclRead").isArray());
                } catch (Exception e) {

                }

                try {
                    aclReadList = Arrays.asList(role.get("aclRead").isString().stringValue());
                } catch (Exception e) {

                }

                DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                acl.setPublicRead(publicRead);
                acl.setPublicWrite(publicWrite);
                setEntityId(entityId);
                setName(name);
                setAcl(acl);
                callback.success();
            }
        });
    }
    public void update(DominoCallback callback) {
        try {
            HttpRequestWithBody httpRequestWithBody = Shape.put(Domino.getServerUrl() + rolesUrl + "/" + getEntityId());

            if(Domino.getMasterKey() != null) {
                httpRequestWithBody.header(HEADER_MASTER_KEY, Domino.getMasterKey());
            }
            if(Domino.getAppId() != null) {
                httpRequestWithBody.header(HEADER_APP_ID, Domino.getAppId());
            }
            if(Domino.getApiKey() != null) {
                httpRequestWithBody.header(HEADER_API_KEY, Domino.getApiKey());
            }

            JSONObject roleObj = new JSONObject();
            roleObj.put("name", new JSONString(name));

            roleObj.put("publicRead", (acl != null && acl.getPublicRead() != null)
                    ? JSONBoolean.getInstance(acl.getPublicRead()) : JSONNull.getInstance());
            roleObj.put("publicWrite", (acl != null && acl.getPublicWrite() != null)
                    ? JSONBoolean.getInstance(acl.getPublicWrite()) : JSONNull.getInstance());
            JSONObject body = new JSONObject();
            body.put("role", roleObj);

            JSONArray aclRead = new JSONArray();
            JSONArray aclWrite = new JSONArray();
            if(acl != null) {
                int idx = 0;
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
                    HttpRequestException exception = (HttpRequestException) throwable;
                    GWT.log("Error: "  + exception.getMessage());
                    GWT.log("Status: "  + exception.getCode());
                    callback.failure((HttpRequestException) throwable);
                }

                @Override
                public void onSuccess(String body) {

                    if(checkStatus(body) != null
                            && checkStatus(body).getCode() >= 400) {
                        callback.failure(new HttpRequestException(checkStatus(body).getMessage(), checkStatus(body).getCode()));
                        return;
                    }


                    JSONObject bodyObj = JSONParser.parseStrict(body).isObject();

                    JSONObject role = bodyObj.get("role").isObject();
                    String entityId = role.get("entityId").isString().stringValue();
                    String name = role.get("name").isString().stringValue();
                    Boolean publicRead = role.get("publicRead").isBoolean().booleanValue();
                    Boolean publicWrite = role.get("publicWrite").isBoolean().booleanValue();

                    try {
                        publicRead = role.get("publicRead").isBoolean().booleanValue();
                    } catch (Exception e) {

                    }

                    try {
                        publicWrite = role.get("publicWrite").isBoolean().booleanValue();
                    } catch (Exception e) {

                    }

                    List<String> aclWriteList = null;
                    List<String> aclReadList = null;

                    try {
                        aclWriteList = JSON.toList(role.get("aclWrite").isArray());
                    } catch (Exception e) {

                    }

                    try {
                        aclWriteList = Arrays.asList(role.get("aclWrite").isString().stringValue());
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = JSON.toList(role.get("aclRead").isArray());
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = Arrays.asList(role.get("aclRead").isString().stringValue());
                    } catch (Exception e) {

                    }

                    DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                    acl.setPublicRead(publicRead);
                    acl.setPublicWrite(publicWrite);
                    setEntityId(entityId);
                    setName(name);
                    setAcl(acl);

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean delete(DominoCallback callback) {
        try {
            HttpRequestWithBody httpRequestWithBody = Shape.delete(Domino.getServerUrl()
                    + rolesUrl + "/" + getEntityId());

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
                httpRequestWithBody.header(HEADER_AUTH_TOKEN, Domino.getApiKey());
            }

            httpRequestWithBody.asJson(new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable throwable) {
                    callback.failure((HttpRequestException) throwable);
                }

                @Override
                public void onSuccess(String body) {

                    if(checkStatus(body) != null
                            && checkStatus(body).getCode() >= 400) {
                        callback.failure(new HttpRequestException(checkStatus(body).getMessage(), checkStatus(body).getCode()));
                        return;
                    }

                    setEntityId(null);
                    setAcl(null);
                    setName(name);
                    callback.success();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void retrieve(DominoCallback callback) throws DominoException {
        try {
            GetRequest getRequest = Shape.get(Domino.getServerUrl()
                    + rolesUrl + "/" + getEntityId());

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

                    if(checkStatus(body) != null
                            && checkStatus(body).getCode() >= 400) {
                        callback.failure(new HttpRequestException(checkStatus(body).getMessage(), checkStatus(body).getCode()));
                        return;
                    }

                    JSONObject bodyObj = JSONParser.parseStrict(body).isObject();

                    JSONObject role = bodyObj.get("role").isObject();
                    String entityId = role.get("entityId").isString().stringValue();
                    String name = role.get("name").isString().stringValue();
                    Boolean publicRead = role.get("publicRead").isBoolean().booleanValue();
                    Boolean publicWrite = role.get("publicWrite").isBoolean().booleanValue();

                    try {
                        publicRead = role.get("publicRead").isBoolean().booleanValue();
                    } catch (Exception e) {

                    }

                    try {
                        publicWrite = role.get("publicWrite").isBoolean().booleanValue();
                    } catch (Exception e) {

                    }

                    List<String> aclWriteList = null;
                    List<String> aclReadList = null;

                    try {
                        aclWriteList = JSON.toList(role.get("aclWrite").isArray());
                    } catch (Exception e) {

                    }

                    try {
                        aclWriteList = Arrays.asList(role.get("aclWrite").isString().stringValue());
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = JSON.toList(role.get("aclRead").isArray());
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = Arrays.asList(role.get("aclRead").isString().stringValue());
                    } catch (Exception e) {

                    }

                    DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                    acl.setPublicRead(publicRead);
                    acl.setPublicWrite(publicWrite);
                    setEntityId(entityId);
                    setName(name);
                    setAcl(acl);
                    callback.success();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public DominoACL getAcl() {
        return acl;
    }

    public void setAcl(DominoACL acl) {
        this.acl = acl;
    }

}
