package com.divroll.backend.sdk;

import com.divroll.backend.sdk.exception.DivrollException;
import com.divroll.backend.sdk.exception.UnsupportedPropertyValueException;
import com.divroll.backend.sdk.helper.Base64Utils;
import com.divroll.backend.sdk.helper.JSON;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.json.client.JSONValue;
import io.reactivex.Single;
import com.divroll.http.client.*;
import com.divroll.http.client.exceptions.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

import static com.divroll.backend.sdk.helper.ACLHelper.aclReadFrom;
import static com.divroll.backend.sdk.helper.ACLHelper.aclWriteFrom;

public class DivrollEntity extends DivrollBase
    implements Copyable<DivrollEntity> {

    private String entityStoreBase = "/entities/";
    private String entityId;
    private DivrollACL acl;
    private List<String> linkNames;
    private List<String> blobNames;
    private String dateCreated;
    private String dateUpdated;

    private JSONObject entityObj = new JSONObject();

    private String entityType;

    private DivrollEntity() {}

    public DivrollEntity(String entityStore) {
        entityStoreBase = entityStoreBase + entityStore;
        entityType = entityStore;
    }



    public Single<byte[]> getBlobProperty(String blobKey) {
        GetRequest getRequest = (GetRequest) HttpClient.get(Divroll.getServerUrl()
                + entityStoreBase + "/" + getEntityId() + "/blobs/" + blobKey)
                .queryString("encoding", "base64");

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
        return getRequest.asString().map(response -> {
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
            } else if(response.getStatus() == 200) {
                if(response.getBody() != null) {
                    byte[] bytes = Base64Utils.fromBase64(response.getBody());
                    return bytes;
                }
            }
            return null;
        });


    }

    public Single<Boolean> setBlobProperty(String blobKey, String base64) {
        if(entityId == null) {
            return Single.create(e -> {
                e.onError(new RuntimeException("Save the entity first before setting a Blob property"));
            });
        }
        HttpRequestWithBody httpRequestWithBody = HttpClient.post(Divroll.getServerUrl()
                + entityStoreBase + "/" + getEntityId() + "/blobs/" + blobKey)
                .queryString("encoding", "base64");

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

        return httpRequestWithBody.body(base64).asString().map(response -> {
            if(response.getStatus() >= 500) {
                throw new ServerErrorRequestException();
            } else if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException();
            } else if(response.getStatus() == 201) {
                //InputStream responseBody = response.getBody();
                return true;
            }
            return false;
        });


    }


    public Single<Boolean> setBlobProperty(String blobKey, byte[] value) {
        if(entityId == null) {
            throw new IllegalArgumentException("Save the entity first before setting a Blob property");
        }
        HttpRequestWithBody httpRequestWithBody = HttpClient.post(Divroll.getServerUrl()
                + entityStoreBase + "/" + getEntityId() + "/blobs/" + blobKey)
                .queryString("encoding", "base64");

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

        String base64 = Base64Utils.toBase64(value);
        return httpRequestWithBody.body(base64).asString().map(response -> {
            if(response.getStatus() >= 500) {
                throw new ServerErrorRequestException();
            } else if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException();
            } else if(response.getStatus() == 201) {
                //InputStream responseBody = response.getBody();
                return true;
            }
            return false;
        });


    }

    public Single<Boolean> deleteBlobProperty(String blobKey) {
        HttpRequestWithBody getRequest = (HttpRequestWithBody) HttpClient.delete(Divroll.getServerUrl()
                + entityStoreBase + "/" + getEntityId() + "/blobs/" + blobKey);
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
        return getRequest.asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new ServerErrorRequestException();
            } else if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException();
            } else if(response.getStatus() == 200) {

            }
            return false;
        });
    }

    public void setProperty(String propertyName, Object propertyValue) throws UnsupportedPropertyValueException {
        if(propertyValue == null) {
            entityObj.put(propertyName, JSONObject.NULL);
        } else {
            DivrollPropertyValue divrollPropertyValue = new DivrollPropertyValue(propertyValue);
            entityObj.put(propertyName, divrollPropertyValue.getValue());
        }
    }

    public Integer getIntegerProperty(String propertyName) {
        Object value = entityObj.get(propertyName);
        if(value != null) {
            if(value instanceof JSONValue) {
                try {
                    JSONValue jsonValue = (JSONValue) value;
                    Double propertyValue = jsonValue.isNumber().doubleValue();
                    return propertyValue.intValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(value instanceof Double) {
                Double propertyValue = (Double) value;
                return propertyValue.intValue();
            }
        } else {
            return null;
        }
        return (Integer) value;
    }

    public Double getDoubleProperty(String propertyName) {
        Object value = entityObj.get(propertyName);
        if(value != null) {
            if(value instanceof JSONValue) {
                try {
                    JSONValue jsonValue = (JSONValue) value;
                    Double propertyValue = jsonValue.isNumber().doubleValue();
                    return propertyValue.doubleValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(value instanceof Double) {
                Double propertyValue = (Double) value;
                return propertyValue;
            }
        } else {
            return null;
        }
        return (Double) value;
    }

    public Long getLongProperty(String propertyName) {
        Object value = entityObj.get(propertyName);
        if(value != null) {
            if(value instanceof JSONValue) {
                try {
                    JSONValue jsonValue = (JSONValue) value;
                    Double propertyValue = jsonValue.isNumber().doubleValue();
                    return propertyValue.longValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if(value instanceof Double) {
                Double propertyValue = (Double) value;
                return propertyValue.longValue();
            }
        } else {
            return null;
        }
        return (Long) value;
    }

    public String getStringProperty(String propertyName) {
        Object value = entityObj.get(propertyName);
        if(value != null) {
            if(value instanceof JSONValue) {
                try {
                    JSONValue jsonValue = (JSONValue) value;
                    String propertyValue = jsonValue.isString().stringValue();
                    return propertyValue;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            return null;
        }
        return (String) value;
    }

    public JSONObject getProperties() {
        return entityObj;
    }

    public Object getProperty(String propertyName) {
        Object value = entityObj.get(propertyName);
        if(value instanceof JSONValue) {
            JSONValue jsonValue = (JSONValue) value;
            if(jsonValue.isNull() != null) {
                return null;
            } else if(jsonValue.isObject() != null) {
                JSONObject jsonObject = new JSONObject(jsonValue.isObject());
                Map<String,Object> entityMap = JSON.toMap(jsonObject);
                return entityMap;
            } else if(jsonValue.isArray() != null) {
                JSONArray jsonArray = new JSONArray(jsonValue.isArray());
                List<Object> list = JSON.toArray(jsonArray);
                return list;
            } else if(jsonValue.isBoolean() != null) {
                return jsonValue.isBoolean().booleanValue();
            } else if(jsonValue.isString() != null) {
                return jsonValue.isString().stringValue();
            } else if(jsonValue.isNumber() != null) {
                return jsonValue.isNumber().doubleValue();
            }
        } else if(value instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) value;
            Map<String,Object> entityMap = JSON.toMap(jsonObject);
            return entityMap;
        } else if(value instanceof  JSONArray) {
            JSONArray jsonArray = (JSONArray) value;
            List<Object> list = JSON.toArray(jsonArray);
            return list;
        } else if(value instanceof com.google.gwt.json.client.JSONObject) {
            JSONObject jsonObject = new JSONObject((com.google.gwt.json.client.JSONObject) value);
            Map<String,Object> entityMap = JSON.toMap(jsonObject);
            return entityMap;
        } else if(value instanceof com.google.gwt.json.client.JSONArray) {
            JSONArray jsonArray = new JSONArray((com.google.gwt.json.client.JSONArray) value);
            List<Object> list = JSON.toArray(jsonArray);
            return list;
        }

        return value;
    }

    public Single<List<DivrollEntity>> links(String linkName)  {
        List<DivrollEntity> entities = new LinkedList<DivrollEntity>();
        if(entityId == null) {
            throw new IllegalArgumentException("Save the entity first before getting links");
        }
        String completeUrl = Divroll.getServerUrl() + entityStoreBase + "/" + getEntityId() + "/links/" + linkName;

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
        if(Divroll.getNamespace() != null) {
            getRequest.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }
        return getRequest.asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new ServerErrorRequestException();
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(), response.getStatus());
            }  else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException();
            } else if(response.getStatus() == 200) {
                JsonNode body = response.getBody();
                JSONObject bodyObj = body.getObject();
                JSONObject entitiesJSONObject = bodyObj.getJSONObject("entities");
                JSONArray results = entitiesJSONObject.getJSONArray("results");
                for(int i=0;i<results.length();i++){
                    DivrollEntity divrollEntity = new DivrollEntity();
                    JSONObject entityJSONObject = results.getJSONObject(i);
                    Iterator<String> it = entityJSONObject.keySet().iterator();
                    while(it.hasNext()) {
                        String propertyKey = it.next();
                        if( propertyKey.equals("entityId")) {
                            divrollEntity.setEntityId(entityJSONObject.getString(propertyKey));
                        }
                        else if (propertyKey.equals("publicRead")) {
                            try {
                                Boolean value = entityJSONObject.getBoolean("publicRead");
                                divrollEntity.getAcl().setPublicRead(value);
                            } catch (Exception e) {

                            }
                        } else if(propertyKey.equals("publicWrite")) {
                            try {
                                Boolean value = entityJSONObject.getBoolean("publicWrite");
                                divrollEntity.getAcl().setPublicWrite(value);
                            } catch (Exception e) {

                            }
                        } else if(propertyKey.equals("aclRead")) {
                            List<String> value = JSON.aclJSONArrayToList(entityJSONObject.getJSONArray("aclRead"));
                            divrollEntity.getAcl().setAclRead(value);
                            if(value == null) {
                                divrollEntity.getAcl().setAclRead(Arrays.asList(entityJSONObject.getString("aclRead")));
                            }
                        } else if(propertyKey.equals("aclWrite")) {
                            List<String> value = JSON.aclJSONArrayToList(entityJSONObject.getJSONArray("aclWrite"));
                            divrollEntity.getAcl().setAclWrite(value);
                            if(value == null) {
                                divrollEntity.getAcl().setAclWrite(Arrays.asList(entityJSONObject.getString("aclWrite")));
                            }
                        } else {
                            divrollEntity.setProperty(propertyKey, entityJSONObject.get(propertyKey));
                        }
                    }
                    entities.add(divrollEntity);
                }
            }
            return entities;
        });
    }

    public Single<List<DivrollEntity>> getEntities(String linkName)   {
        List<DivrollEntity> entities = new LinkedList<DivrollEntity>();
        if(entityId == null) {
            throw new IllegalArgumentException("Save the entity first before getting links");
        }
        DivrollEntity divrollEntity = new DivrollEntity();
        String completeUrl = Divroll.getServerUrl() + entityStoreBase + "/" + getEntityId() + "/links/" + linkName;
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
        if(Divroll.getNamespace() != null) {
            getRequest.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }
        return getRequest.asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new ServerErrorRequestException();
            } else if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
            }  else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(), response.getStatus());
            }  else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException();
            } else if(response.getStatus() == 200) {
                JsonNode body = response.getBody();
                JSONObject bodyObj = body.getObject();
                JSONObject entityJsonObject = bodyObj.getJSONObject("entities");
                JSONObject resultJsonObject = entityJsonObject.getJSONObject("results");
                String entityId = entityJsonObject.getString("entityId");

                Boolean publicRead = entityJsonObject.getBoolean("publicRead");
                Boolean publicWrite = entityJsonObject.getBoolean("publicWrite");
                List<String> aclWriteList = aclWriteFrom(entityJsonObject);
                List<String> aclReadList =  aclReadFrom(entityJsonObject);

                Iterator<String> it = entityJsonObject.keySet().iterator();
                while(it.hasNext()) {
                    String propertyKey = it.next();
                    if( propertyKey.equals("entityId")) {
                        setEntityId(entityJsonObject.getString(propertyKey));
                    } else if (propertyKey.equals("publicRead")
                            || propertyKey.equals("publicWrite")
                            || propertyKey.equals("dateCreated")
                            || propertyKey.equals("dateUpdated")
                            || propertyKey.equals("aclRead")
                            || propertyKey.equals("aclWrite")) {
                        // skip
                    } else {
                        Object obj = entityJsonObject.get(propertyKey);
                        divrollEntity.setProperty(propertyKey, obj);
                    }
                }

                DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
                acl.setPublicWrite(publicWrite);
                acl.setPublicRead(publicRead);
                divrollEntity.setEntityId(entityId);
                divrollEntity.setAcl(acl);

                String dateCreated = entityJsonObject.getString("dateCreated");
                String dateUpdated = entityJsonObject.getString("dateUpdated");
                divrollEntity.setDateCreated(dateCreated);
                divrollEntity.setDateUpdated(dateUpdated);


            }
            return entities;
        });


    }

    public Single<Boolean> setLink(String linkName, String entityId) {
        if(entityId == null) {
            throw new IllegalArgumentException("Save the entity first before creating a link");
        }
        HttpRequestWithBody httpRequestWithBody = HttpClient.post(Divroll.getServerUrl()
                + entityStoreBase + "/" + getEntityId() + "/links/" + linkName + "/" + entityId);
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
                + entityStoreBase + "/" + getEntityId() + "/links/" + linkName + "/" + entityId);
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
                + entityStoreBase + "/" + getEntityId() + "/links/" + linkName + "/" + entityId);
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
                + entityStoreBase + "/" + getEntityId() + "/links/" + linkName);
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

    public DivrollACL getAcl() {
        if(acl == null) {
            acl = new DivrollACL();
        }
        return acl;
    }

    public void setAcl(DivrollACL acl) {
        this.acl = acl;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public Single<DivrollEntity> create()  {
        HttpRequestWithBody httpRequestWithBody = HttpClient.post(Divroll.getServerUrl() + entityStoreBase);
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

        entityObj.put("aclRead", aclRead);
        entityObj.put("aclWrite", aclWrite);
        entityObj.put("publicRead", (this.acl != null && this.acl.getPublicRead() != null)
                ? this.acl.getPublicRead() : JSONObject.NULL);
        entityObj.put("publicWrite", (this.acl != null && this.acl.getPublicWrite() != null)
                ? this.acl.getPublicWrite() : JSONObject.NULL);
        JSONObject body = new JSONObject();
        body.put("entity", entityObj);


        httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
        httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
        httpRequestWithBody.header("Content-Type", "application/json");

        return httpRequestWithBody.body(body).asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new HttpRequestException(response.getStatusText(),response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(),response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(),response.getStatus());
            } else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException(response.getStatusText(),response.getStatus());
            } else if(response.getStatus() == 201) {
                JsonNode responseBody = response.getBody();
                JSONObject bodyObj = responseBody.getObject();
                JSONObject entity = bodyObj.getJSONObject("entity");
                String entityId = entity.getString("entityId");
                String dateCreated = entity.getString("dateCreated");
                String dateUpdated = entity.getString("dateUpdated");
                setDateCreated(dateCreated);
                setDateUpdated(dateUpdated);
                setEntityId(entityId);
            }
            return copy();
        });

    }

    public Single<Boolean> update() {
        String completeUrl = Divroll.getServerUrl() + entityStoreBase + "/" + getEntityId();
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

        entityObj.put("aclRead", aclRead);
        entityObj.put("aclWrite", aclWrite);
        entityObj.put("publicRead", (this.acl != null && this.acl.getPublicRead() != null)
                ? this.acl.getPublicRead() : JSONObject.NULL);
        entityObj.put("publicWrite", (this.acl != null && this.acl.getPublicWrite() != null)
                ? this.acl.getPublicWrite() : JSONObject.NULL);

        JSONObject body = new JSONObject();
        body.put("entity", entityObj);


        httpRequestWithBody.header("X-Divroll-ACL-Read", aclRead.toString());
        httpRequestWithBody.header("X-Divroll-ACL-Write", aclWrite.toString());
        httpRequestWithBody.header("Content-Type", "application/json");


        return httpRequestWithBody.body(body).asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new ServerErrorRequestException();
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(),response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(),response.getStatus());
            } else if(response.getStatus() == 200) {
                return true;
            }
            return false;
        });


    }

    public Single<DivrollEntity> retrieve() {
        String completeUrl = Divroll.getServerUrl() + entityStoreBase + "/" + getEntityId();
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
        if(Divroll.getNamespace() != null) {
            getRequest.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }
        return getRequest.asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new HttpRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 404) {
                throw new NotFoundRequestException(response.getStatusText(),response.getStatus());
            }  else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(),response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(),response.getStatus());
            }  else if(response.getStatus() >= 400) {
                throw new HttpRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 200) {
                JsonNode body = response.getBody();
                JSONObject bodyObj = body.getObject();
                JSONObject entityJsonObject = bodyObj.getJSONObject("entity");
                String entityId = entityJsonObject.getString("entityId");

                Boolean publicRead = entityJsonObject.getBoolean("publicRead");
                Boolean publicWrite = entityJsonObject.getBoolean("publicWrite");
                List<String> aclWriteList = aclWriteFrom(entityJsonObject);
                List<String> aclReadList =  aclReadFrom(entityJsonObject);

                Iterator<String> it = entityJsonObject.keySet().iterator();
                while(it.hasNext()) {
                    String propertyKey = it.next();
                    if( propertyKey.equals("entityId")) {
                        setEntityId(entityJsonObject.getString(propertyKey));
                    } else if (propertyKey.equals("publicRead")
                            || propertyKey.equals("publicWrite")
                            || propertyKey.equals("aclRead")
                            || propertyKey.equals("aclWrite")) {
                        // skip
                    } else if(propertyKey.equals("linkNames")) {
                        JSONArray jsonArray = entityJsonObject.getJSONArray(propertyKey);
                        for(int i=0;i<jsonArray.length();i++) {
                            String linkName = jsonArray.getString(i);
                            getLinkNames().add(linkName);
                        }
                    } else if(propertyKey.equals("blobNames")) {
                        JSONArray jsonArray = entityJsonObject.getJSONArray(propertyKey);
                        for(int i=0;i<jsonArray.length();i++) {
                            String blobName = jsonArray.getString(i);
                            getBlobNames().add(blobName);
                        }
                    } else {
                        Object obj = entityJsonObject.get(propertyKey);
                        entityObj.put(propertyKey, obj);
                    }
                }

                DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
                acl.setPublicWrite(publicWrite);
                acl.setPublicRead(publicRead);
                setEntityId(entityId);
                setAcl(acl);

                String dateCreated = entityJsonObject.getString("dateCreated");
                String dateUpdated = entityJsonObject.getString("dateUpdated");
                setDateCreated(dateCreated);
                setDateUpdated(dateUpdated);

            }
            return copy();
        });

    }

    public Single<Boolean> delete() {
        String completeUrl = Divroll.getServerUrl() + entityStoreBase + "/" + getEntityId();
        HttpRequestWithBody httpRequestWithBody = HttpClient.delete(completeUrl);
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
        return httpRequestWithBody.asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new ServerErrorRequestException();
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(),response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(),response.getStatus());
            } else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 204) {
                return true;
            }
            return false;
        });
    }

    public Single<Boolean> deleteProperty(String propertyName)  {
        String completeUrl = Divroll.getServerUrl() + entityStoreBase + "/" + getEntityId() + "/properties/" + propertyName;
        HttpRequestWithBody httpRequestWithBody = HttpClient.delete(completeUrl);
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
        return httpRequestWithBody.asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new HttpRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(), response.getStatus());
            }  else if(response.getStatus() >= 400) {
                throw new HttpRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 200) {
                return true;
            }
            return false;
        });
    }

    public Set<String> getPropertyNames() {
        return entityObj.asJSONObject().keySet();
    }

    @Override
    public DivrollEntity copy() {
        return this;
    }

    public List<String> getLinkNames() {
        if(linkNames == null) {
            linkNames = new LinkedList<>();
        }
        return linkNames;
    }

    private void setLinkNames(List<String> linkNames) {
        this.linkNames = linkNames;
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

    public String getEntityType() {
        return entityType;
    }
}
