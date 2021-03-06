/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018 to present, Divroll, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.divroll.backend.sdk;

import com.divroll.backend.sdk.filter.QueryFilter;
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

public class DivrollEntities extends LinkableDivrollBase
    implements Copyable<DivrollEntities> {

    private static final String entityStoreUrl = "/entities/";

    private List<DivrollEntity> entities;
    private Integer skip;
    private Integer limit;
    private Boolean count;
    private String sort;
    private Long result;
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

    public Integer getSkip() {
        return skip;
    }

    public void setSkip(Integer skip) {
        this.skip = skip;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Single<Boolean> delete()  {
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

    public Single<DivrollEntities> query(QueryFilter filter)  {
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
        if(Divroll.getNamespace() != null) {
            getRequest.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }

        if(filter != null) {
            getRequest.queryString("queries", filter.toString());
        }

        if(skip != null) {
            getRequest.queryString("skip", String.valueOf(getSkip()));
        }
        if(limit != null) {
            getRequest.queryString("limit", String.valueOf(getLimit()));
        }

        if(count != null) {
            getRequest.queryString("count", String.valueOf(getCount()));
        }

        if(sort != null) {
            getRequest.queryString("sort", String.valueOf(sort));
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
                result = entitiesJSONObject.getLong("count");
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

                    JSONArray links = entityJSONObject.getJSONArray("links");
                    if(links != null) {
                        for(int j=0;j<links.length();j++) {
                            JSONObject linksObj = links.getJSONObject(j);
                            DivrollLink link = processLink(linksObj);
                            divrollEntity.getLinks().add(link);
                        }
                    } else {
                        JSONObject linksObj = entityJSONObject.getJSONObject("links");
                        DivrollLink link = processLink(linksObj);
                        divrollEntity.getLinks().add(link);
                    }

                    getEntities().add(divrollEntity);
                }
            }
            return copy();
        });
    }


    public Single<DivrollEntities> query()  {
        return query(null);
    }

    @Override
    public DivrollEntities copy() {
        return this;
    }

    public Boolean getCount() {
        return count;
    }

    public void setCount(Boolean count) {
        this.count = count;
    }

    public Long getResult() {
        return result;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }
}
