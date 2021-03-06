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

import com.divroll.backend.sdk.exception.UnsupportedPropertyValueException;
import com.divroll.backend.sdk.helper.JSON;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class DivrollPropertyValue {
    private Object value = null;
    public DivrollPropertyValue(Object value) throws UnsupportedPropertyValueException {
//         //java.lang.String
//             //java.lang.String
        if(!value.getClass().getName().equalsIgnoreCase(String.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(Boolean.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(boolean.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(Integer.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(int.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(Long.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(long.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(Short.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(short.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(Float.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(float.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(Double.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(double.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(Map.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(List.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(JSONArray.class.getName())
                && !value.getClass().getName().equalsIgnoreCase(JSONObject.class.getName())
                && !(value instanceof Map)
                && !(value instanceof List)) {
            throw new UnsupportedPropertyValueException(value.getClass().getName());
        }
        if(value instanceof Map) {
            Object test = ((Map) value).keySet().iterator().next();
            if(!(test instanceof String)) {
                throw new IllegalArgumentException("Map contains non-String key");
            }
            this.value = JSON.mapToJSONObject((Map<String, Object>) value);
        } else if (value instanceof List) {
            this.value = JSON.listToJSONArray((List<Object>) value);
        } else {
            this.value = value;
        }
    }
    public Object getValue() {
        return value;
    }
}
