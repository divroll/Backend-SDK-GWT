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

import com.divroll.backend.sdk.DivrollEntityId;
import com.divroll.backend.sdk.SafeJSONOArray;
import com.divroll.backend.sdk.SafeJSONObject;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:kerby@divroll.com">Kerby Martino</a>
 * @version 0-SNAPSHOT
 * @since 0-SNAPSHOT
 */
public class ACLHelper {

    public static List<DivrollEntityId> idsOnly(SafeJSONOArray safeJSONOArray) {
        List<DivrollEntityId> aclList = null;
        if (safeJSONOArray == null) {
            return null;
        }
        for (int i = 0; i < safeJSONOArray.length(); i++) {
            if (aclList == null) {
                aclList = new LinkedList<DivrollEntityId>();
            }
            SafeJSONObject safeJSONObject = safeJSONOArray.getJSONObject(i);
            String entityId = safeJSONObject.getString("entityId");
            if (entityId != null && !contains(entityId, aclList)) {
                aclList.add(new DivrollEntityId(entityId));
            }
        }
        return aclList;
    }

    public static boolean contains(String entityId, List<DivrollEntityId> entityStubs) {
        if (entityStubs != null) {
            for (DivrollEntityId entityStub : entityStubs) {
                if (entityStub.getEntityId() != null && entityStub.getEntityId().equals(entityId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean containsId(String entityId, List<String> aclList) {
        if (aclList != null) {
            for (String id : aclList) {
                if (id.equals(entityId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<String> aclWriteFrom(JSONObject entityJsonObject) {
        List<String> aclWriteList = null;
        try {
            aclWriteList = JSON.aclJSONArrayToList(entityJsonObject.getJSONArray("aclWrite"));
        } catch (Exception e) {

        }
        try {
            JSONObject jsonObject = entityJsonObject.getJSONObject("aclWrite");
            if(aclWriteList == null) {
                aclWriteList = Arrays.asList(jsonObject.getString("entityId"));
            }
        } catch (Exception e) {

        }
        return aclWriteList;
    }

    public static List<String> aclReadFrom(JSONObject entityJsonObject) {
        List<String> aclReadList = null;
        try {
            aclReadList = JSON.aclJSONArrayToList(entityJsonObject.getJSONArray("aclRead"));
        } catch (Exception e) {

        }
        try {
            JSONObject jsonObject = entityJsonObject.getJSONObject("aclRead");
            if(aclReadList == null) {
                aclReadList = Arrays.asList(jsonObject.getString("entityId"));
            }
        } catch (Exception e) {

        }
        return aclReadList;
    }

}
