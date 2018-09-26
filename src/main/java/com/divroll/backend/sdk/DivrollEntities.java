package com.divroll.backend.sdk;

import com.divroll.backend.sdk.helper.JSON;
import com.divroll.http.client.*;
import com.google.gwt.http.client.RequestException;
import io.reactivex.Single;
import com.divroll.http.client.exceptions.BadRequestException;
import com.divroll.http.client.exceptions.UnauthorizedRequestException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DivrollEntities extends DivrollBase
    implements Copyable<DivrollEntities> {

    private static final String entityStoreUrl = "/entities/";

    private List<DivrollEntity> entities;
    private int skip;
    private int limit;
    private String entityStore;

    private DivrollEntities() {}

    public DivrollEntities(String entityStore) {
        this.entityStore = entityStore;
    }

    public List<DivrollEntity> getEntities() {
        if(entities == null) {
            entities = new LinkedList<DivrollEntity>();
        }
        return entities;
    }

    public void setEntities(List<DivrollEntity> entities) {
        this.entities = entities;
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

    public Single<Boolean> delete() throws RequestException {
        String completeUrl = Divroll.getServerUrl()
                + entityStoreUrl + entityStore;
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

    public Single<DivrollEntities> query() throws RequestException {
        String completeUrl = Divroll.getServerUrl()
                + entityStoreUrl + entityStore;;
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
                throw new HttpRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(), response.getStatus());
            }  else if(response.getStatus() >= 400) {
                throw new HttpRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 200) {

                getEntities().clear();

                JsonNode body = response.getBody();
                JSONObject bodyObj = body.getObject();
                JSONObject entitiesJSONObject = bodyObj.getJSONObject("entities");
                JSONArray results = entitiesJSONObject.getJSONArray("results");
                for(int i=0;i<results.length();i++){
                    DivrollEntity divrollEntity = new DivrollEntity(this.entityStore);
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
                    getEntities().add(divrollEntity);
                }
            }
            return copy();
        });
    }

    @Override
    public DivrollEntities copy() {
        return this;
    }
}
