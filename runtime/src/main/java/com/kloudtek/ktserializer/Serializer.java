/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.HashMap;

/**
 * Created by yannick on 12/09/2014.
 */
public class Serializer {
    protected final HashMap<String, Object> map = new HashMap<String, Object>();
    private ClassMapper classMapper;

    public Serializer() {
    }

    public Serializer(ClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    public <S extends Serializable> S deserialize(@NotNull S serializableObj, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readObject(serializableObj, false);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public <X extends Serializable> X deserialize(@NotNull Class<X> classType, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        return deserialize(classType, serializedData, null);
    }

    public <X extends Serializable> X deserialize(@NotNull Class<X> classType, @NotNull byte[] serializedData, @Nullable ClassMapper classMapper) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readObject(classType, classMapper != null ? classMapper : this.classMapper, false);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public byte[] serialize(@NotNull Serializable object) {
        return serialize(object, null);
    }

    public byte[] serialize(@NotNull Serializable object, @Nullable ClassMapper classMapper) {
        try {
            SerializationStream os = new SerializationStream(this);
            os.writeObject(object, classMapper != null ? classMapper : this.classMapper, false);
            return os.toByteArray();
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }


    public Serializer setInject(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public Object getInject(String key) {
        return getImpl(key);
    }

    public Serializer setInject(Object value) {
        return setInject(value.getClass().getName(), value);
    }

    public Serializer setInject(Class<?> classType, Object value) {
        map.put(classType.getName(), value);
        return this;
    }

    public <X> X getInject(Class<X> classType) {
        return classType.cast(getImpl(classType.getName()));
    }

    private Object getImpl(String key) {
        return map.get(key);
    }

    public static ClassMapper getClassMapper(Class<? extends Serializable> classType) {
        return null;
    }
}
