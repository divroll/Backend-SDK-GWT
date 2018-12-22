package com.divroll.backend.sdk.filter;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;

public class StartsWithQueryFilter implements QueryFilter {
    private JSONObject filter = new JSONObject();
    private StartsWithQueryFilter() {}
    public StartsWithQueryFilter(String propertyName, String propertyValue) {
        JSONObject opFind = new JSONObject();
        opFind.put(propertyName, new JSONString(propertyValue));
        filter.put("$findStartingWith", opFind);
    }
    @Override
    public String toString() {
        return filter.toString();
    }
}
