package com.divroll.backend.sdk.filter;

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class EqualQueryFilter implements QueryFilter {
    private JSONObject filter = new JSONObject();
    private EqualQueryFilter() {}
    public EqualQueryFilter(String propertyName, String propertyValue) {
        JSONObject opFind = new JSONObject();
        opFind.put(propertyName, new JSONString(propertyValue));
        filter.put("$find", opFind);
    }
    public EqualQueryFilter(String propertyName, Double propertyValue) {
        JSONObject opFind = new JSONObject();
        opFind.put(propertyName, new JSONNumber(propertyValue));
        filter.put("$find", opFind);
    }
    public EqualQueryFilter(String propertyName, Boolean propertyValue) {
        JSONObject opFind = new JSONObject();
        opFind.put(propertyName, JSONBoolean.getInstance(propertyValue));
        filter.put("$find", opFind);
    }
    @Override
    public String toString() {
        return filter.toString();
    }
}
