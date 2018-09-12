package com.divroll.backend.sdk.helper;

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNull;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONString;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

public class JSON {
    private JSON() {}
    public static List<String> aclJSONArrayToList(JSONArray jsonArray) {
        if(jsonArray == null)
            return null;
        List<String> list = new LinkedList<String>();
        for(int i=0;i<jsonArray.length();i++) {
            JSONObject aclObject = jsonArray.getJSONObject(i);
            if(aclObject != null) {
                list.add(aclObject.getString("entityId"));
            }
        }
        return list;
    }
    public static List<Object> toArray(JSONArray jsonArray) {
        List<Object> list = new LinkedList<Object>();
        for(int i=0;i<jsonArray.length();i++){

            try {
                if(jsonArray.get(i) == null) {
                    list.add(null);
                }
            } catch (Exception e) {

            }

            try {
                JSONObject value = jsonArray.getJSONObject(i);
                list.add(toMap(value));
            } catch (Exception e) {

            }

            try {
                JSONArray value = jsonArray.getJSONArray(i);
                list.add(toArray(value));
            } catch (Exception e) {

            }

            try {
                Double value = jsonArray.getDouble(i);
                list.add(value);
            } catch (Exception e) {

            }

            try {
                Boolean value = jsonArray.getBoolean(i);
                list.add(value);
            } catch (Exception e) {

            }
            try {
                String value = jsonArray.getString(i);
                list.add(value);
            } catch (Exception e) {

            }

        }
        return list;
    }
    public static Map<String, Object> toMap(JSONObject jsonObject) {
        Iterator<String> it = jsonObject.keySet().iterator();
        Map<String, Object> enittyMap = new LinkedHashMap<String, Object>();
        while (it.hasNext()) {
            String k = it.next();
            try {
                JSONObject jso = jsonObject.getJSONObject(k);
                enittyMap.put(k, toMap(jso));
            } catch (Exception e) {

            }
            try {
                JSONArray jsa = jsonObject.getJSONArray(k);
                enittyMap.put(k, toArray(jsa));
            } catch (Exception e) {

            }
            try {
                Boolean value = jsonObject.getBoolean(k);
                enittyMap.put(k, value);
            } catch (Exception e) {

            }
            try {
                Long value = jsonObject.getLong(k);
                enittyMap.put(k, value);
            } catch (Exception e) {

            }
            try {
                Double value = jsonObject.getDouble(k);
                enittyMap.put(k, value);
            } catch (Exception e) {

            }
            try {
                String value = jsonObject.getString(k);
                enittyMap.put(k, value);
            } catch (Exception e) {

            }
        }
        return enittyMap;
    }

    public static com.google.gwt.json.client.JSONObject mapToJSONObject(Map<String,Object> map) {
        com.google.gwt.json.client.JSONObject jsonObject = null;
        Iterator<String> it = map.keySet().iterator();
        while(it.hasNext()) {
            if(jsonObject == null) {
                jsonObject = new com.google.gwt.json.client.JSONObject();
            }
            String key = it.next();
            Object value = map.get(key);
            if(value == null) {
                jsonObject.put(key, JSONNull.getInstance());
            } else {
                if(value instanceof Boolean || value.getClass().getName().equals(boolean.class.getName())){
                    jsonObject.put(key, JSONBoolean.getInstance((Boolean) value));
                } else if(value instanceof String){
                    jsonObject.put(key, new JSONString((String) value));
                } else if(value instanceof Double || value.getClass().getName().equals(double.class.getName())){
                    jsonObject.put(key, new JSONNumber((Double) value));
                } else if(value instanceof Float || value.getClass().getName().equals(float.class.getName())){
                    jsonObject.put(key, new JSONNumber((Float) value));
                } else if(value instanceof Long || value.getClass().getName().equals(long.class.getName())){
                    jsonObject.put(key, new JSONNumber((Long) value));
                } else if(value instanceof Integer || value.getClass().getName().equals(double.class.getName())){
                    jsonObject.put(key, new JSONNumber((Integer) value));
                } else if(value instanceof JSONArray){
                    JSONArray jsonArray = (JSONArray) value;
                    jsonObject.put(key, (jsonArray.asJSONArray()));
                } else if(value instanceof JSONObject){
                    JSONObject jso = (JSONObject) value;
                    jso.put(key, jso.asJSONObject());
                } else if(value instanceof JSONNull) {
                    jsonObject.put(key, (JSONNull) value);
                } else if(value instanceof com.google.gwt.json.client.JSONObject){
                    com.google.gwt.json.client.JSONObject jso = (com.google.gwt.json.client.JSONObject) value;
                    jso.put(key, jso);
                } else if(value instanceof com.google.gwt.json.client.JSONArray) {
                    jsonObject.put(key, (com.google.gwt.json.client.JSONArray) value);
                } else {
                    throw new IllegalArgumentException("Object type " + value.getClass().getName() + " is not supported.");
                }
            }
        }
        return jsonObject;
    }

    public static com.google.gwt.json.client.JSONArray listToJSONArray(List<Object> list) {
        com.google.gwt.json.client.JSONArray jsonArray = null;
        for(Object value : list) {
            if(jsonArray == null) {
                jsonArray = new com.google.gwt.json.client.JSONArray();
            }
            if(value == null) {
                jsonArray.set(jsonArray.size(), JSONNull.getInstance());
            } else {
                if(value instanceof Boolean || value.getClass().getName().equals(boolean.class.getName())){
                    jsonArray.set(jsonArray.size(), JSONBoolean.getInstance((Boolean) value));
                } else if(value instanceof String){
                    jsonArray.set(jsonArray.size(), new JSONString((String) value));
                } else if(value instanceof Double || value.getClass().getName().equals(double.class.getName())){
                    jsonArray.set(jsonArray.size(), new JSONNumber((Double) value));
                } else if(value instanceof Float || value.getClass().getName().equals(float.class.getName())){
                    jsonArray.set(jsonArray.size(), new JSONNumber((Float) value));
                } else if(value instanceof Long || value.getClass().getName().equals(long.class.getName())){
                    jsonArray.set(jsonArray.size(), new JSONNumber((Long) value));
                } else if(value instanceof Integer || value.getClass().getName().equals(double.class.getName())){
                    jsonArray.set(jsonArray.size(), new JSONNumber((Integer) value));
                } else if(value instanceof JSONArray){
                    JSONArray jsa = (JSONArray) value;
                    jsonArray.set(jsonArray.size(), (jsa.asJSONArray()));
                } else if(value instanceof JSONObject){
                    JSONObject jsonObject = (JSONObject) value;
                    jsonArray.set(jsonArray.size(), jsonObject.asJSONObject());
                } else if(value instanceof JSONNull) {
                    jsonArray.set(jsonArray.size(), (JSONNull) value);
                } else if(value instanceof com.google.gwt.json.client.JSONObject){
                    com.google.gwt.json.client.JSONObject jso = (com.google.gwt.json.client.JSONObject) value;
                    jsonArray.set(jsonArray.size(), jso);
                } else if(value instanceof com.google.gwt.json.client.JSONArray) {
                    jsonArray.set(jsonArray.size(), (com.google.gwt.json.client.JSONArray) value);
                } else {
                    throw new IllegalArgumentException("Object type " + value.getClass().getName() + " is not supported.");
                }
            }
        }
        return jsonArray;
    }



}
