package com.divroll.domino.client;

import com.divroll.domino.client.exception.BadRequestException;
import com.divroll.domino.client.exception.DominoException;
import com.divroll.domino.client.exception.NotFoundRequestException;
import com.divroll.domino.client.exception.UnauthorizedException;
import com.divroll.domino.client.helper.Base64Utils;
import com.divroll.domino.client.helper.JSON;
import com.dotweblabs.shape.client.GetRequest;
import com.dotweblabs.shape.client.HttpRequestException;
import com.dotweblabs.shape.client.HttpRequestWithBody;
import com.dotweblabs.shape.client.Shape;
import com.google.gwt.json.client.*;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.*;

public class DominoEntity extends DominoBase {

    private String entityStoreBase = "/entities/";
    private String entityId;
    private DominoACL acl;
    private JSONObject entityObj = new JSONObject();

    private DominoEntity() {}

    public DominoEntity(String entityStore) {
        entityStoreBase = entityStoreBase + entityStore;
    }

    public void getBlobProperty(String blobKey, DominoCallbackWithResponse<byte[]> callback) {
        try {
            GetRequest getRequest = (GetRequest) Shape.get(Domino.getServerUrl()
                    + entityStoreBase + "/" + getEntityId() + "/blobs/" + blobKey);
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
                    byte[] bytes = Base64Utils.fromBase64(body);
                    callback.success(bytes);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setBlobProperty(String blobKey, byte[] value, DominoCallback callback) {
        if(entityId == null) {
            throw new DominoException("Save the entity first before setting a Blob property");
        }
        try {
            HttpRequestWithBody httpRequestWithBody = Shape.post(Domino.getServerUrl()
                    + entityStoreBase + "/" + getEntityId() + "/blobs/" + blobKey + "?encoding=base64");
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

            String base64 = Base64Utils.toBase64(value);

            httpRequestWithBody.body(base64).asJson(new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable throwable) {
                    callback.failure((HttpRequestException) throwable);
                }
                @Override
                public void onSuccess(String body) {
                    callback.success();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteBlobProperty(String blobKey, DominoCallback callback) {
        try {
            HttpRequestWithBody getRequest = (HttpRequestWithBody) Shape.delete(Domino.getServerUrl()
                    + entityStoreBase + "/" + getEntityId() + "/blobs/" + blobKey);
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
                    callback.success();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setProperty(String propertyName, Object propertyValue) {
        if(propertyValue == null) {
            entityObj.put(propertyName, JSONNull.getInstance());
        } else {
            DominoPropertyValue dominoPropertyValue = new DominoPropertyValue(propertyValue);
            entityObj.put(propertyName, dominoPropertyValue.getValue());
        }
    }

    public Object getProperty(String propertyName) {
        Object value = entityObj.get(propertyName);
        if(value instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) value;
            Map<String,Object> entityMap = JSON.toMap(jsonObject);
            return entityMap;
        } else if(value instanceof  JSONArray) {
            JSONArray jsonArray = (JSONArray) value;
            List<Object> list = JSON.toArray(jsonArray);
            return list;
        }
        return value;
    }

    public List<DominoEntity> links(String linkName, DominoCallback callback) {
        List<DominoEntity> entities = new LinkedList<DominoEntity>();
        if(entityId == null) {
            throw new DominoException("Save the entity first before getting links");
        }
        try {
            String completeUrl = Domino.getServerUrl() + entityStoreBase + "/" + getEntityId() + "/links/" + linkName;

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
                    JSONObject bodyObj = JSONParser.parseStrict(body).isObject();
                    JSONObject entitiesJSONObject = bodyObj.get("entities").isObject();
                    JSONArray results = entitiesJSONObject.get("results").isArray();
                    for(int i=0;i<results.size();i++){
                        DominoEntity dominoEntity = new DominoEntity();
                        JSONObject entityJSONObject = results.get(i).isObject();
                        Iterator<String> it = entityJSONObject.keySet().iterator();
                        while(it.hasNext()) {
                            String propertyKey = it.next();
                            if( propertyKey.equals("entityId")) {
                                dominoEntity.setEntityId(entityJSONObject.get(propertyKey).isString().stringValue());
                            }
                            else if (propertyKey.equals("publicRead")) {
                                try {
                                    Boolean value = entityJSONObject.get("publicRead").isBoolean().booleanValue();
                                    dominoEntity.getAcl().setPublicRead(value);
                                } catch (Exception e) {

                                }
                            } else if(propertyKey.equals("publicWrite")) {
                                try {
                                    Boolean value = entityJSONObject.get("publicWrite").isBoolean().booleanValue();
                                    dominoEntity.getAcl().setPublicWrite(value);
                                } catch (Exception e) {

                                }
                            } else if(propertyKey.equals("aclRead")) {
                                try {
                                    List<String> value = JSON.toList(entityJSONObject.get("aclRead").isArray());
                                    dominoEntity.getAcl().setAclRead(value);
                                } catch (Exception e) {

                                }
                                try {
                                    List<String> value = Arrays.asList(entityJSONObject.get("aclRead").isString().stringValue());
                                    dominoEntity.getAcl().setAclRead(value);
                                } catch (Exception e) {

                                }
                            } else if(propertyKey.equals("aclWrite")) {
                                try {
                                    List<String> value = JSON.toList(entityJSONObject.get("aclWrite").isArray());
                                    dominoEntity.getAcl().setAclWrite(value);
                                } catch (Exception e) {

                                }
                                try {
                                    List<String> value = Arrays.asList(entityJSONObject.get("aclWrite").isString().stringValue());
                                    dominoEntity.getAcl().setAclWrite(value);
                                } catch (Exception e) {

                                }
                            } else {
                                dominoEntity.setProperty(propertyKey, entityJSONObject.get(propertyKey));
                            }
                        }
                        entities.add(dominoEntity);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entities;
    }


    public List<DominoEntity> getEntities(String linkName, DominoCallback callback) {
        List<DominoEntity> entities = new LinkedList<DominoEntity>();
        if(entityId == null) {
            throw new DominoException("Save the entity first before getting links");
        }
        try {
            DominoEntity dominoEntity = new DominoEntity();
            String completeUrl = Domino.getServerUrl() + entityStoreBase + "/" + getEntityId() + "/links/" + linkName;
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
                    JSONObject bodyObj = JSONParser.parseStrict(body).isObject();
                    JSONObject entityJsonObject = bodyObj.get("entities").isObject();
                    JSONObject resultJsonObject = entityJsonObject.get("results").isObject();
                    String entityId = entityJsonObject.get("entityId").isString().stringValue();

                    Boolean publicRead = null;
                    Boolean publicWrite = null;

                    try {
                        publicWrite = entityJsonObject.get("publicWrite").isBoolean().booleanValue();
                    } catch (Exception e) {

                    }

                    try {
                        publicRead = entityJsonObject.get("publicRead").isBoolean().booleanValue();
                    } catch (Exception e) {

                    }

                    List<String> aclWriteList = null;
                    List<String> aclReadList = null;

                    try {
                        aclWriteList = JSON.toList(entityJsonObject.get("aclWrite").isArray());
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = JSON.toList(entityJsonObject.get("aclRead").isArray());
                    } catch (Exception e) {

                    }

                    try {
                        aclWriteList = Arrays.asList(entityJsonObject.get("aclWrite").isString().stringValue());
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = Arrays.asList(entityJsonObject.get("aclRead").isString().stringValue());
                    } catch (Exception e) {

                    }

                    Iterator<String> it = entityJsonObject.keySet().iterator();
                    while(it.hasNext()) {
                        String propertyKey = it.next();
                        if( propertyKey.equals("entityId")) {
                            setEntityId(entityJsonObject.get(propertyKey).isString().stringValue());
                        } else if (propertyKey.equals("publicRead")
                                || propertyKey.equals("publicWrite")
                                || propertyKey.equals("aclRead")
                                || propertyKey.equals("aclWrite")) {
                            // skip
                        } else {
                            Object obj = entityJsonObject.get(propertyKey);
                            dominoEntity.setProperty(propertyKey, obj);
                        }
                    }

                    DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                    acl.setPublicWrite(publicWrite);
                    acl.setPublicRead(publicRead);
                    dominoEntity.setEntityId(entityId);
                    dominoEntity.setAcl(acl);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
        return entities;
    }

    public void addLink(String linkName, String entityId, DominoCallback callback) {
        if(entityId == null) {
            throw new DominoException("Save the entity first before creating a link");
        }
        try {
            HttpRequestWithBody httpRequestWithBody = Shape.post(Domino.getServerUrl()
                    + entityStoreBase + "/" + getEntityId() + "/links/" + linkName + "/" + entityId);
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

             httpRequestWithBody.asJson(new AsyncCallback<String>() {
                 @Override
                 public void onFailure(Throwable throwable) {
                     callback.failure((HttpRequestException) throwable);
                 }
                 @Override
                 public void onSuccess(String s) {

                 }
             });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeLink(String linkName, String entityId, DominoCallback callback) {
        if(entityId == null) {
            throw new DominoException("Save the entity first before removing a link");
        }
        try {
            HttpRequestWithBody httpRequestWithBody = Shape.delete(Domino.getServerUrl()
                    + entityStoreBase + "/" + getEntityId() + "/links/" + linkName + "/" + entityId);
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

            httpRequestWithBody.asJson(new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable throwable) {
                    callback.failure((HttpRequestException) throwable);
                }
                @Override
                public void onSuccess(String body) {
                    callback.success();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeLinks(String linkName, DominoCallback callback) {
        if(entityId == null) {
            throw new DominoException("Save the entity first before removing links");
        }
        try {
            HttpRequestWithBody httpRequestWithBody = Shape.delete(Domino.getServerUrl()
                    + entityStoreBase + "/" + getEntityId() + "/links/" + linkName);
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

            httpRequestWithBody.asJson(new AsyncCallback<String>() {
                @Override
                public void onFailure(Throwable throwable) {
                    callback.failure((HttpRequestException) throwable);
                }
                @Override
                public void onSuccess(String body) {
                    callback.success();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DominoACL getAcl() {
        return acl;
    }

    public void setAcl(DominoACL acl) {
        this.acl = acl;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public void create(DominoCallback callback) {
        try {
            HttpRequestWithBody httpRequestWithBody = Shape.post(Domino.getServerUrl() + entityStoreBase);
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
            JSONArray aclReadArray = new JSONArray();
            JSONArray aclWriteArray = new JSONArray();

            entityObj.put("aclRead", aclReadArray);
            entityObj.put("aclWrite", aclWriteArray);
            entityObj.put("publicRead", (this.acl != null && this.acl.getPublicRead() != null)
                    ? JSONBoolean.getInstance(this.acl.getPublicRead()) : JSONNull.getInstance());
            entityObj.put("publicWrite", (this.acl != null && this.acl.getPublicWrite() != null)
                    ? JSONBoolean.getInstance(this.acl.getPublicWrite()) : JSONNull.getInstance());

            JSONObject body = new JSONObject();
            body.put("entity", entityObj);

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
                    callback.failure((HttpRequestException) throwable);
                }
                @Override
                public void onSuccess(String body) {
                    JSONObject bodyObj = JSONParser.parseStrict(body).isObject();
                    JSONObject entity = bodyObj.get("entity").isObject();
                    String entityId = entity.get("entityId").isString().stringValue();
                    setEntityId(entityId);
                    callback.success();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean update(DominoCallback callback) {
        try {
            String completeUrl = Domino.getServerUrl() + entityStoreBase + "/" + getEntityId();
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
            JSONArray aclReadArray = new JSONArray();
            JSONArray aclWriteArray = new JSONArray();

            entityObj.put("aclRead", aclReadArray);
            entityObj.put("aclWrite", aclWriteArray);
            entityObj.put("publicRead", (this.acl != null && this.acl.getPublicRead() != null)
                    ? JSONBoolean.getInstance(this.acl.getPublicRead()) : JSONNull.getInstance());
            entityObj.put("publicWrite", (this.acl != null && this.acl.getPublicWrite() != null)
                    ? JSONBoolean.getInstance(this.acl.getPublicWrite()) : JSONNull.getInstance());

            JSONObject body = new JSONObject();
            body.put("entity", entityObj);

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
                    callback.failure((HttpRequestException) throwable);
                }
                @Override
                public void onSuccess(String body) {
                    JSONObject bodyObj = JSONParser.parseStrict(body).isObject();
                    JSONObject entity = bodyObj.get("entity").isObject();
                    String entityId = entity.get("entityId").isString().stringValue();
                    setEntityId(entityId);
                    callback.success();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public void retrieve(DominoCallback callback) {
        try {
            String completeUrl = Domino.getServerUrl() + entityStoreBase + "/" + getEntityId();
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
                    JSONObject bodyObj = JSONParser.parseStrict(body).isObject();
                    JSONObject entityJsonObject = bodyObj.get("entity").isObject();
                    String entityId = entityJsonObject.get("entityId").isString().stringValue();

                    Boolean publicRead = null;
                    Boolean publicWrite = null;

                    try {
                        publicWrite = entityJsonObject.get("publicWrite").isBoolean().booleanValue();
                    } catch (Exception e) {

                    }

                    try {
                        publicRead = entityJsonObject.get("publicRead").isBoolean().booleanValue();
                    } catch (Exception e) {

                    }

                    List<String> aclWriteList = null;
                    List<String> aclReadList = null;

                    try {
                        aclWriteList = JSON.toList(entityJsonObject.get("aclWrite").isArray());
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = JSON.toList(entityJsonObject.get("aclRead").isArray());
                    } catch (Exception e) {

                    }

                    try {
                        aclWriteList = Arrays.asList(entityJsonObject.get("aclWrite").isString().stringValue());
                    } catch (Exception e) {

                    }

                    try {
                        aclReadList = Arrays.asList(entityJsonObject.get("aclRead").isString().stringValue());
                    } catch (Exception e) {

                    }

                    Iterator<String> it = entityJsonObject.keySet().iterator();
                    while(it.hasNext()) {
                        String propertyKey = it.next();
                        if( propertyKey.equals("entityId")) {
                            setEntityId(entityJsonObject.get(propertyKey).isString().stringValue());
                        } else if (propertyKey.equals("publicRead")
                                || propertyKey.equals("publicWrite")
                                || propertyKey.equals("aclRead")
                                || propertyKey.equals("aclWrite")) {
                            // skip
                        } else {
                            JSONValue obj = entityJsonObject.get(propertyKey);
                            entityObj.put(propertyKey, obj);
                        }
                    }

                    DominoACL acl = new DominoACL(aclReadList, aclWriteList);
                    acl.setPublicWrite(publicWrite);
                    acl.setPublicRead(publicRead);
                    setEntityId(entityId);
                    setAcl(acl);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean delete(DominoCallback callback) {
        try {
            String completeUrl = Domino.getServerUrl() + entityStoreBase + "/" + getEntityId();
            HttpRequestWithBody httpRequestWithBody = Shape.delete(completeUrl);
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
                    callback.success();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
