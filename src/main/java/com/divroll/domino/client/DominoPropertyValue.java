package com.divroll.domino.client;

import com.divroll.domino.client.exception.UnsupportedPropertyValueException;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import java.util.List;
import java.util.Map;

public class DominoPropertyValue {
    private JSONValue value = null;
    public DominoPropertyValue(Object value) {
//        System.out.println(value.getClass().getName()); //java.lang.String
//        System.out.println(String.class.getName());     //java.lang.String
        if(!value.getClass().getName().equalsIgnoreCase(String.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(Boolean.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(Integer.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(Long.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(Short.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(Float.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(Double.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(Map.class.getName())
                || !value.getClass().getName().equalsIgnoreCase(List.class.getName())) {
            throw new UnsupportedPropertyValueException(value.getClass().getName());
        } else if(value.getClass().getName().equals(String.class.getName())) {
            this.value = new JSONString((String) value);
        } else if(value.getClass().getName().equals(Boolean.class.getName())) {
            this.value = new JSONNumber((Integer) value);
        } else if(value.getClass().getName().equals(Integer.class.getName())) {
            this.value = new JSONNumber((Integer) value);
        } else if(value.getClass().getName().equals(Long.class.getName())) {
            this.value = new JSONNumber((Long) value);
        } else if(value.getClass().getName().equals(Short.class.getName())) {
            this.value = new JSONNumber((Short) value);
        } else if(value.getClass().getName().equals(Float.class.getName())) {
            this.value = new JSONNumber((Float) value);
        } else if(value.getClass().getName().equals(Double.class.getName())) {
            this.value = new JSONNumber((Double) value);
        } else if(value.getClass().getName().equals(Map.class.getName())) {

        } else if(value.getClass().getName().equals(List.class.getName())) {


        }
    }
    public JSONValue getValue() {
        return value;
    }
}
