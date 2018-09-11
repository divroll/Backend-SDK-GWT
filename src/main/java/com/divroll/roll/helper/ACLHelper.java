/*
 * Divroll, Platform for Hosting Static Sites
 * Copyright 2018, Divroll, and individual contributors
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
package com.divroll.roll.helper;

import com.divroll.roll.DivrollEntityId;
import com.divroll.roll.SafeJSONOArray;
import com.divroll.roll.SafeJSONObject;
import org.json.JSONArray;
import org.json.JSONObject;

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

}
