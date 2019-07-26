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

import com.divroll.backend.sdk.helper.DivrollEntityHelper;
import io.reactivex.Single;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public abstract class LinkableDivrollBase extends DivrollBase {
    protected DivrollLink processLink(JSONObject linksObj) {
        if(linksObj != null) {
            String linkName = linksObj.getString("linkName");
            JSONArray entities = linksObj.getJSONArray("entities");
            List<DivrollEntityStub> divrollEntities = new LinkedList<>();
            if(entities != null) {
                for (int i=0; i<entities.length(); i++) {
                    JSONObject entity = entities.getJSONObject(i);
                    DivrollEntityStub divrollEntity = DivrollEntityHelper.convertStub(entity);
                    divrollEntities.add(divrollEntity);
                }
            } else {
                JSONObject entity = linksObj.getJSONObject("entities");
                DivrollEntityStub divrollEntity = DivrollEntityHelper.convertStub(entity);
                divrollEntities.add(divrollEntity);
            }
            return new DivrollLink(linkName, divrollEntities);
        }
        return null;
    }

    protected Single<List<DivrollEntity>> getStubBodies(List<DivrollEntity> entityStubs) {
        return Single.create(emitter -> {
            entityStubs.forEach(stub -> {

            });
        });
    }
}
