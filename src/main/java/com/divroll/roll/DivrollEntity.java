package com.divroll.roll;

import com.divroll.roll.exception.DivrollException;
import com.divroll.roll.exception.UnsupportedPropertyValueException;
import com.divroll.roll.helper.JSON;
import com.google.common.io.ByteStreams;
import com.google.gwt.http.client.RequestException;
import org.gwtproject.http.client.*;
import org.gwtproject.http.client.exceptions.BadRequestException;
import org.gwtproject.http.client.exceptions.NotFoundRequestException;
import org.gwtproject.http.client.exceptions.UnauthorizedRequestException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class DivrollEntity extends DivrollBase {

    private String entityStoreBase = "/entities/";
    private String entityId;
    private DivrollACL acl;
    private JSONObject entityObj = new JSONObject();

    private DivrollEntity() {}

    public DivrollEntity(String entityStore) {
        entityStoreBase = entityStoreBase + entityStore;
    }

    public byte[] getBlobProperty(String blobKey) throws RequestException {
        GetRequest getRequest = (GetRequest) HttpClient.get(Divroll.getServerUrl()
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

        HttpResponse<InputStream> response = getRequest.asBinary();

        if(response.getStatus() >= 500) {
            throw new DivrollException("Internal Server error"); // TODO
        } else if(response.getStatus() == 404) {
            throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() == 401) {
            throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() == 400) {
            throw new BadRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() >= 400) {
            throw new DivrollException("Client error"); // TODO
        } else if(response.getStatus() == 200) {
//            InputStream is = response.getBody();
//            byte[] bytes = ByteStreams.toByteArray(is);
//            return bytes;
        }
        return null;
    }

    public void setBlobProperty(String blobKey, byte[] value) throws RequestException{
        if(entityId == null) {
            throw new DivrollException("Save the entity first before setting a Blob property");
        }
        HttpRequestWithBody httpRequestWithBody = HttpClient.post(Divroll.getServerUrl()
                + entityStoreBase + "/" + getEntityId() + "/blobs/" + blobKey);
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

        HttpResponse<InputStream> response =  httpRequestWithBody.body(value).asBinary();
        if(response.getStatus() >= 500) {
            throw new DivrollException("Internal Server error"); // TODO
        } else if(response.getStatus() == 404) {
            throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() == 401) {
            throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() == 400) {
            throw new BadRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() >= 400) {
            throw new DivrollException("Client error"); // TODO
        } else if(response.getStatus() == 201) {
            InputStream responseBody = response.getBody();
        }
    }

    public void deleteBlobProperty(String blobKey) throws RequestException {
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

        HttpResponse<InputStream> response = getRequest.asBinary();

        if(response.getStatus() >= 500) {
            throw new DivrollException("Internal Server error"); // TODO
        } else if(response.getStatus() == 404) {
            throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() == 401) {
            throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() == 400) {
            throw new BadRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() >= 400) {
            throw new DivrollException("Client error"); // TODO
        } else if(response.getStatus() == 200) {

        }
    }

    public void setProperty(String propertyName, Object propertyValue) throws UnsupportedPropertyValueException {
        if(propertyValue == null) {
            entityObj.put(propertyName, JSONObject.NULL);
        } else {
            DivrollPropertyValue divrollPropertyValue = new DivrollPropertyValue(propertyValue);
            entityObj.put(propertyName, divrollPropertyValue.getValue());
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

    public List<DivrollEntity> links(String linkName) throws RequestException {
        List<DivrollEntity> entities = new LinkedList<DivrollEntity>();
        if(entityId == null) {
            throw new DivrollException("Save the entity first before getting links");
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

        HttpResponse<JsonNode> response = getRequest.asJson();


        if(response.getStatus() >= 500) {
            throwException(response);
        } else if(response.getStatus() == 401) {
            throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() == 400) {
            throw new BadRequestException(response.getStatusText(), response.getStatus());
        }  else if(response.getStatus() >= 400) {
            throwException(response);
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
                        try {
                            List<String> value = JSON.aclJSONArrayToList(entityJSONObject.getJSONArray("aclRead"));
                            divrollEntity.getAcl().setAclRead(value);
                        } catch (Exception e) {

                        }
                        try {
                            List<String> value = Arrays.asList(entityJSONObject.getString("aclRead"));
                            divrollEntity.getAcl().setAclRead(value);
                        } catch (Exception e) {

                        }
                    } else if(propertyKey.equals("aclWrite")) {
                        try {
                            List<String> value = JSON.aclJSONArrayToList(entityJSONObject.getJSONArray("aclWrite"));
                            divrollEntity.getAcl().setAclWrite(value);
                        } catch (Exception e) {

                        }
                        try {
                            List<String> value = Arrays.asList(entityJSONObject.getString("aclWrite"));
                            divrollEntity.getAcl().setAclWrite(value);
                        } catch (Exception e) {

                        }
                    } else {
                        divrollEntity.setProperty(propertyKey, entityJSONObject.get(propertyKey));
                    }
                }
                entities.add(divrollEntity);
            }

        }
        return entities;
    }


    public List<DivrollEntity> getEntities(String linkName) throws RequestException  {
        List<DivrollEntity> entities = new LinkedList<DivrollEntity>();
        if(entityId == null) {
            throw new DivrollException("Save the entity first before getting links");
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

        HttpResponse<JsonNode> response = getRequest.asJson();

        if(response.getStatus() >= 500) {
            throwException(response);
        } else if(response.getStatus() == 404) {
            throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
        }  else if(response.getStatus() == 401) {
            throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() == 400) {
            throw new BadRequestException(response.getStatusText(), response.getStatus());
        }  else if(response.getStatus() >= 400) {
            throwException(response);
        } else if(response.getStatus() == 200) {
            JsonNode body = response.getBody();
            JSONObject bodyObj = body.getObject();
            JSONObject entityJsonObject = bodyObj.getJSONObject("entities");
            JSONObject resultJsonObject = entityJsonObject.getJSONObject("results");
            String entityId = entityJsonObject.getString("entityId");

            Boolean publicRead = null;
            Boolean publicWrite = null;

            try {
                publicWrite = entityJsonObject.getBoolean("publicWrite");
            } catch (Exception e) {

            }

            try {
                publicRead = entityJsonObject.getBoolean("publicRead");
            } catch (Exception e) {

            }

            List<String> aclWriteList = null;
            List<String> aclReadList = null;

            try {
                aclWriteList = JSON.aclJSONArrayToList(entityJsonObject.getJSONArray("aclWrite"));
            } catch (Exception e) {

            }

            try {
                aclReadList = JSON.aclJSONArrayToList(entityJsonObject.getJSONArray("aclRead"));
            } catch (Exception e) {

            }

            try {
                aclWriteList = Arrays.asList(entityJsonObject.getString("aclWrite"));
            } catch (Exception e) {

            }

            try {
                aclReadList = Arrays.asList(entityJsonObject.getString("aclRead"));
            } catch (Exception e) {

            }

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

        }
        return entities;
    }

    public void addLink(String linkName, String entityId) throws RequestException  {
        if(entityId == null) {
            throw new DivrollException("Save the entity first before creating a link");
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

        HttpResponse<JsonNode> response =  httpRequestWithBody.asJson();
        if(response.getStatus() >= 500) {
            throw new HttpRequestException(response.getStatusText(),response.getStatus()); // TODO
        } else if(response.getStatus() == 404) {
            throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() == 401) {
            throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() == 400) {
            throw new BadRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() >= 400) {
            throw new DivrollException("Client error"); // TODO
        } else if(response.getStatus() == 201) {
        }
    }

    public void removeLink(String linkName, String entityId) throws RequestException  {
        if(entityId == null) {
            throw new DivrollException("Save the entity first before removing a link");
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

        HttpResponse<JsonNode> response =  httpRequestWithBody.asJson();
        if(response.getStatus() >= 500) {
            throw new HttpRequestException(response.getStatusText(),response.getStatus()); // TODO
        } else if(response.getStatus() == 404) {
            throw new NotFoundRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() == 401) {
            throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() == 400) {
            throw new BadRequestException(response.getStatusText(), response.getStatus());
        } else if(response.getStatus() >= 400) {
            throw new DivrollException("Client error"); // TODO
        } else if(response.getStatus() == 201) {
        }
    }

    public void removeLinks(String linkName) throws RequestException  {
        if(entityId == null) {
            throw new DivrollException("Save the entity first before removing links");
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

        HttpResponse<JsonNode> response =  httpRequestWithBody.asJson();
        if(response.getStatus() >= 500) {
            //throw new DivrollException(response.getStatusText(),response.getStatus()); // TODO
        } else if(response.getStatus() == 404) {
            throw new NotFoundRequestException(response.getStatusText(),response.getStatus());
        } else if(response.getStatus() == 401) {
            throw new UnauthorizedRequestException(response.getStatusText(),response.getStatus());
        } else if(response.getStatus() == 400) {
            throw new BadRequestException(response.getStatusText(),response.getStatus());
        } else if(response.getStatus() >= 400) {
            throw new DivrollException("Client error"); // TODO
        } else if(response.getStatus() == 201) {
        }
    }

    public DivrollACL getAcl() {
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

    public void create() throws RequestException {
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



        HttpResponse<JsonNode> response =  httpRequestWithBody.body(body).asJson();



        if(response.getStatus() >= 500) {
            throw new HttpRequestException(response.getStatusText(),response.getStatus());
        } else if(response.getStatus() == 401) {
            throw new UnauthorizedRequestException(response.getStatusText(),response.getStatus());
        } else if(response.getStatus() >= 401) {
            throw new UnauthorizedRequestException(response.getStatusText(),response.getStatus());
        } else if(response.getStatus() == 201) {
            JsonNode responseBody = response.getBody();
            JSONObject bodyObj = responseBody.getObject();
            JSONObject entity = bodyObj.getJSONObject("entity");
            String entityId = entity.getString("entityId");
            setEntityId(entityId);
        }
    }

    public boolean update() throws RequestException {
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



        HttpResponse<JsonNode> response =  httpRequestWithBody.body(body).asJson();



        if(response.getStatus() >= 500) {
            //throw new DivrollException(response.getStatusText(),response.getStatus());
        } else if(response.getStatus() == 401) {
            throw new UnauthorizedRequestException(response.getStatusText(),response.getStatus());
        } else if(response.getStatus() >= 401) {
            throw new HttpRequestException(response.getStatusText(),response.getStatus());
        } else if(response.getStatus() == 201) {
            JsonNode responseBody = response.getBody();
            JSONObject bodyObj = responseBody.getObject();
            JSONObject entity = bodyObj.getJSONObject("entity");
            String entityId = entity.getString("entityId");
            setEntityId(entityId);
            return true;
        }
        return false;
    }
    public void retrieve() throws RequestException {
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

        HttpResponse<JsonNode> response = getRequest.asJson();

        if(response.getStatus() >= 500) {
            throwException(response);
        } else if(response.getStatus() == 404) {
            throw new NotFoundRequestException(response.getStatusText(),response.getStatus());
        }  else if(response.getStatus() == 401) {
            throw new UnauthorizedRequestException(response.getStatusText(),response.getStatus());
        } else if(response.getStatus() == 400) {
            throw new BadRequestException(response.getStatusText(),response.getStatus());
        }  else if(response.getStatus() >= 400) {
            throwException(response);
        } else if(response.getStatus() == 200) {
            JsonNode body = response.getBody();
            JSONObject bodyObj = body.getObject();
            JSONObject entityJsonObject = bodyObj.getJSONObject("entity");
            String entityId = entityJsonObject.getString("entityId");

            Boolean publicRead = null;
            Boolean publicWrite = null;

            try {
                publicWrite = entityJsonObject.getBoolean("publicWrite");
            } catch (Exception e) {

            }

            try {
                publicRead = entityJsonObject.getBoolean("publicRead");
            } catch (Exception e) {

            }

            List<String> aclWriteList = null;
            List<String> aclReadList = null;

            try {
                aclWriteList = JSON.aclJSONArrayToList(entityJsonObject.getJSONArray("aclWrite"));
            } catch (Exception e) {

            }

            try {
                aclReadList = JSON.aclJSONArrayToList(entityJsonObject.getJSONArray("aclRead"));
            } catch (Exception e) {

            }

            try {
                JSONObject jsonObject = entityJsonObject.getJSONObject("aclWrite");
                aclWriteList = Arrays.asList(jsonObject.getString("entityId"));
            } catch (Exception e) {

            }
            try {
                JSONObject jsonObject = entityJsonObject.getJSONObject("aclRead");
                aclReadList = Arrays.asList(jsonObject.getString("entityId"));
            } catch (Exception e) {

            }

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

        }
    }

    public boolean delete() throws RequestException{
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
        HttpResponse<JsonNode> response = httpRequestWithBody.asJson();
        if(response.getStatus() >= 500) {
            throwException(response);
        } else if(response.getStatus() == 401) {
            throw new UnauthorizedRequestException(response.getStatusText(),response.getStatus());
        } else if(response.getStatus() == 400) {
            throw new BadRequestException(response.getStatusText(),response.getStatus());
        } else if(response.getStatus() >= 400) {
            throwException(response);
        } else if(response.getStatus() == 204) {
            return true;
        }
        return false;
    }

}
