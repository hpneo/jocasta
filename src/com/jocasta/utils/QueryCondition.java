package com.jocasta.utils;

import java.util.Map.Entry;

public class QueryCondition<K, V> implements Entry<K, V> {
    private K key;
    private V value;
    
    public QueryCondition() {
    }
    
    public QueryCondition(K key, V value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public K getKey() {
        return this.key;
    }

    @Override
    public V getValue() {
        return this.value;
    }

    @Override
    public V setValue(V value) {
        V oldValue = this.value;
        this.value = value;
        return oldValue;
    }

    public static String getColumnType(Class<?> type) {
        if ((type.equals(Boolean.class)) ||
                (type.equals(Boolean.TYPE)) ||
                (type.equals(java.util.Date.class)) ||
                (type.equals(java.util.Calendar.class)) ||
                (type.equals(java.sql.Date.class)) ||
                (type.equals(Integer.class)) ||
                (type.equals(Integer.TYPE)) ||
                (type.equals(Long.class)) ||
                (type.equals(Long.TYPE))) {
            return "INTEGER";
        }

        if ((type.equals(Double.class)) || (type.equals(Double.TYPE)) || (type.equals(Float.class)) ||
                (type.equals(Float.TYPE))) {
            return "FLOAT";
        }

        if ((type.equals(String.class)) || (type.equals(Character.TYPE))) {
            return "TEXT";
        }

        return "";
    }
    
    public static String getColumnName(String fieldName) {
        return Inflector.underscore(fieldName);
    }
}
