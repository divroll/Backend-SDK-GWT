package com.divroll.domino.client.helper;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import java.util.*;

public class JSON {
    private JSON() {}
    public static JSONArray toJSONArray(List<String> list) {
        if(list == null) {
            return null;
        }
        JSONArray jsonArray = new JSONArray();
        int idx = 0;
        for(String s : list) {
            jsonArray.set(idx, new JSONString(s));
            idx++;
        }
        return jsonArray;
    }
    public static List<String> toList(JSONArray jsonArray) {
        if(jsonArray == null)
            return null;
        List<String> list = new LinkedList<String>();
        for(int i=0;i<jsonArray.size();i++) {
            list.add(jsonArray.get(i).isString().stringValue());
        }
        return list;
    }
    public static List<Object> toArray(JSONArray jsonArray) {
        List<Object> list = new LinkedList<Object>();
        for(int i=0;i<jsonArray.size();i++){

            try {
                if(jsonArray.get(i) == null) {
                    list.add(null);
                }
            } catch (Exception e) {

            }

            try {
                JSONObject value = jsonArray.get(i).isObject();
                list.add(toMap(value));
            } catch (Exception e) {

            }

            try {
                JSONArray value = jsonArray.get(i).isArray();
                list.add(toArray(value));
            } catch (Exception e) {

            }

            try {
                Double value = jsonArray.get(i).isNumber().doubleValue();
                list.add(value);
            } catch (Exception e) {

            }

            try {
                Boolean value = jsonArray.get(i).isBoolean().booleanValue();
                list.add(value);
            } catch (Exception e) {

            }
            try {
                String value = jsonArray.get(i).isString().stringValue();
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
                JSONObject jso = jsonObject.get(k).isObject();
                enittyMap.put(k, toMap(jso));
            } catch (Exception e) {

            }
            try {
                JSONArray jsa = jsonObject.get(k).isArray();
                enittyMap.put(k, toArray(jsa));
            } catch (Exception e) {

            }
            try {
                Boolean value = jsonObject.get(k).isBoolean().booleanValue();
                enittyMap.put(k, value);
            } catch (Exception e) {

            }
            try {
                Double value = jsonObject.get(k).isNumber().doubleValue();
                enittyMap.put(k, value);
            } catch (Exception e) {

            }
            try {
                String value = jsonObject.get(k).isString().stringValue();
                enittyMap.put(k, value);
            } catch (Exception e) {

            }
        }
        return enittyMap;
    }
}
