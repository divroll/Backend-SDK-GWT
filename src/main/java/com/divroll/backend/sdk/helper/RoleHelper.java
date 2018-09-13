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
