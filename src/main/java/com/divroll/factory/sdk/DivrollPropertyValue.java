package com.divroll.factory.sdk;

import com.divroll.factory.sdk.exception.UnsupportedPropertyValueException;
import com.divroll.factory.sdk.helper.JSON;
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
