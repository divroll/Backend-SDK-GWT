package com.divroll.domino.client;

import com.divroll.domino.client.helper.JSON;
import com.dotweblabs.shape.client.GetRequest;
import com.dotweblabs.shape.client.HttpRequestException;
import com.dotweblabs.shape.client.Shape;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class DominoEntities extends DominoBase {

    private static String entityStoreUrl = "/entities/";

    private List<DominoEntity> entities;
    private int skip;
    private int limit;
    private String entityStore;

    private DominoEntities() {}

    public DominoEntities(String entityStore) {
        this.entityStore = entityStore;
        entityStoreUrl = entityStoreUrl + entityStore;
    }

    public List<DominoEntity> getEntities() {
        if(entities == null) {
            entities = new LinkedList<DominoEntity>();
        }
        return entities;
    }

    public void setEntities(List<DominoEntity> entities) {
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

    public void query(DominoCallback callback) {
        try {
            GetRequest getRequest = (GetRequest) Shape.get(Domino.getServerUrl()
                    + entityStoreUrl);

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
                    getEntities().clear();
                    JSONObject bodyObj = JSONParser.parseStrict(body).isObject();
                    JSONObject entitiesJSONObject = bodyObj.get("entities").isObject();
                    JSONArray results = entitiesJSONObject.get("results").isArray();
                    for(int i=0;i<results.size();i++){
                        DominoEntity dominoEntity = new DominoEntity(entityStore);
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
                        getEntities().add(dominoEntity);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
