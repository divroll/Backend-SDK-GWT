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

import com.divroll.http.client.GetRequest;
import com.divroll.http.client.HttpClient;
import com.divroll.http.client.JsonNode;
import com.divroll.http.client.exceptions.BadRequestException;
import com.divroll.http.client.exceptions.ClientErrorRequestException;
import com.divroll.http.client.exceptions.ServerErrorRequestException;
import com.divroll.http.client.exceptions.UnauthorizedRequestException;
import elemental.client.Browser;
import io.reactivex.Single;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

import static com.divroll.backend.sdk.helper.ACLHelper.aclReadFrom;
import static com.divroll.backend.sdk.helper.ACLHelper.aclWriteFrom;

public class DivrollRoles extends DivrollBase
    implements Copyable<DivrollRoles> {

    private static final String rolesUrl = "/entities/roles";

    private List<DivrollRole> roles;
    private Integer skip;
    private Integer limit;

    public List<DivrollRole> getRoles() {
        if(roles == null) {
            roles = new LinkedList<DivrollRole>();
        }
        return roles;
    }

    public void setRoles(List<DivrollRole> roles) {
        this.roles = roles;
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

    public Single<DivrollRoles> query()  {
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
        if(Divroll.getNamespace() != null) {
            getRequest.header(HEADER_NAMESPACE, Divroll.getNamespace());
        }

        if(skip != null) {
            getRequest.queryString("skip", String.valueOf(getSkip()));
        }
        if(limit != null) {
            getRequest.queryString("limit", String.valueOf(getLimit()));
        }

        return getRequest.asJson().map(response -> {
            if(response.getStatus() >= 500) {
                throw new ServerErrorRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 401) {
                throw new UnauthorizedRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 400) {
                throw new BadRequestException(response.getStatusText(), response.getStatus());
            }  else if(response.getStatus() >= 400) {
                throw new ClientErrorRequestException(response.getStatusText(), response.getStatus());
            } else if(response.getStatus() == 200) {
                setRoles(new LinkedList<>());
                JsonNode body = response.getBody();
                JSONObject bodyObj = body.getObject();
                JSONObject roles = bodyObj.getJSONObject("roles");
                JSONArray results = roles.getJSONArray("results");

                if(results == null) {
                    JSONObject singleResultObject = roles.getJSONObject("results");
                    if(singleResultObject != null) {
                        results = new JSONArray();
                        results.put(singleResultObject);
                    }
                }

                for(int i=0;i<results.length();i++){
                    JSONObject roleObj = results.getJSONObject(i);
                    String entityId = roleObj.getString("entityId");
                    String name = roleObj.getString("name");
                    Boolean publicRead = roleObj.getBoolean("publicRead");
                    Boolean publicWrite = roleObj.getBoolean("publicWrite");
                    List<String> aclWriteList = aclWriteFrom(roleObj);
                    List<String> aclReadList =  aclReadFrom(roleObj);

                    DivrollACL acl = new DivrollACL(aclReadList, aclWriteList);
                    acl.setPublicWrite(publicWrite);
                    acl.setPublicRead(publicRead);

                    DivrollRole role = new DivrollRole();
                    role.setEntityId(entityId);
                    role.setName(name);
                    role.setAcl(acl);

                    String dateCreated = roleObj.getString("dateCreated");
                    String dateUpdated = roleObj.getString("dateUpdated");
                    role.setDateCreated(dateCreated);
                    role.setDateUpdated(dateUpdated);

                    List<DivrollRole> divrollRoles = getRoles();
                    divrollRoles.add(role);
                    setRoles(divrollRoles);
                }
            }
            return copy();
        });
    }

    @Override
    public DivrollRoles copy() {
        Browser.getWindow().getConsole().log(toString());
        return this;
    }

    @Override
    public String toString() {
        final String[] s = {"["};
        String skip = String.valueOf(getSkip());
        String limit = String.valueOf(getLimit());
        s[0] = s[0] + "className=" + getClass().getName() + "\n";
        s[0] = s[0] + "skip=" + skip + "\n";
        s[0] = s[0] + "limit=" + limit + "\n";
        getRoles().forEach(divrollRole -> { s[0] = s[0] + divrollRole.toString() + "\n";});
        s[0] = s[0] + "]" + "\n";
        return s[0];
    }
}
