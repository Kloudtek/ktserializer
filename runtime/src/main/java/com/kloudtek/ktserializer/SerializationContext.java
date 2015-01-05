/*
 * Copyright (c) 2014 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import java.util.HashMap;

/**
 * Created by yannick on 30/12/2014.
 */
public class SerializationContext {
    protected SerializationContext parent;
    protected Integer version;
    protected final HashMap<String, Object> map = new HashMap<String, Object>();

    public SerializationContext() {
        parent = Serializer.getThreadLocalContext();
    }

    public SerializationContext(Integer version) {
        this.version = version;
        parent = Serializer.getThreadLocalContext();
    }

    public SerializationContext(SerializationContext parent) {
        this.parent = parent;
    }

    public SerializationContext(Integer version, SerializationContext parent) {
        this.version = version;
        this.parent = parent;
    }

    public SerializationContext set(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public Object get(String key) {
        return getImpl(key);
    }

    public SerializationContext set(Object value) {
        return set(value.getClass().getName(), value);
    }

    public SerializationContext set(Class<?> classType, Object value) {
        map.put(classType.getName(), value);
        return this;
    }

    public <X> X get(Class<X> classType) {
        return classType.cast(getImpl(classType.getName()));
    }

    public Integer getVersion() {
        return version;
    }

    void setVersion(Integer version) {
        this.version = version;
    }

    private Object getImpl(String key) {
        Object value = map.get(key);
        if (value == null && parent != null) {
            return parent.get(key);
        }
        return value;
    }
}
