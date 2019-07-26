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
package com.divroll.backend.sdk.helper;

import com.divroll.backend.sdk.DivrollRole;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class RoleHelper {
    public static boolean contains(String roleId, List<DivrollRole> roleList) {
        if(roleList == null) {
            return false;
        }
        for(DivrollRole role : roleList) {
            if(role.getEntityId().equals(roleId)) {
                return true;
            }
        }
        return false;
    }

    public static List<DivrollRole> rolesFrom(JSONObject entityObj) {
        List<DivrollRole> divrollRoles = null;
        try {
            Object roles = entityObj.get("roles");
            if(roles instanceof JSONArray) {
                divrollRoles = new LinkedList<DivrollRole>();
                JSONArray jsonArray = (JSONArray) roles;
                for(int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String roleId = jsonObject.getString("entityId");
                    DivrollRole divrollRole = new DivrollRole();
                    divrollRole.setEntityId(roleId);
                    divrollRoles.add(divrollRole);
                }
            } else if(roles instanceof JSONObject) {
                divrollRoles = new LinkedList<DivrollRole>();
                JSONObject jsonObject = (JSONObject) roles;
                String roleId = jsonObject.getString("entityId");
                DivrollRole divrollRole = new DivrollRole();
                divrollRole.setEntityId(roleId);
                divrollRoles.add(divrollRole);
            }
        } catch (Exception e) {
            // do nothing
        }
        return divrollRoles;
    }
}
