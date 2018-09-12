package com.divroll.factory.sdk.helper;

import com.divroll.factory.sdk.DivrollRole;

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
}
