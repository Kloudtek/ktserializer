/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yannick on 12/09/2014.
 */
public class SerializationEngine {
    protected final HashMap<String, Object> map = new HashMap<String, Object>();
    protected ClassMapper classMapper;
    protected boolean disallowUnmappedClasses = true;
    protected int maxReadSize;

    public SerializationEngine() {
        this(Serializer.systemClassMapper());
    }

    public SerializationEngine(ClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    public <S extends Serializable> S deserialize(@NotNull S serializableObj, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readObject(serializableObj);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public <S extends Serializable> S deserializeSpecific(@NotNull S serializableObj, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readSpecificObject(serializableObj);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public <X extends Serializable> X deserialize(@NotNull Class<X> classType, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readObject(classType);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public <X extends Serializable> X deserializeSpecific(@NotNull Class<X> classType, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readSpecificObject(classType);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public Serializable deserialize(@NotNull byte[] serializedData) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readObject();
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public List<Serializable> deserializeList(@NotNull byte[] serializedData) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readObjectList();
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public <S extends Serializable> List<S> deserializeList(@NotNull Class<S> classType, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readObjectList(classType);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public byte[] serialize(@NotNull Serializable object) {
        return serialize(object, false);
    }

    public byte[] serializeSpecific(@NotNull Serializable object) {
        return serialize(object, true);
    }

    private byte[] serialize(@NotNull Serializable object, boolean specific) {
        try {
            SerializationStream os = new SerializationStream(this);
            os.writeObject(object, classMapper, specific);
            return os.closeAndReturnData();
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    public byte[] serializeList(@NotNull Collection<? extends Serializable> collection) {
        try {
            SerializationStream os = new SerializationStream(this);
            os.writeObjectList(collection, classMapper);
            return os.closeAndReturnData();
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    public SerializationEngine setInject(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public Object getInject(String key) {
        return getImpl(key);
    }

    public SerializationEngine setInject(Object value) {
        return setInject(value.getClass().getName(), value);
    }

    public SerializationEngine setInject(Class<?> classType, Object value) {
        map.put(classType.getName(), value);
        return this;
    }

    public <X> X getInject(Class<X> classType) {
        return classType.cast(getImpl(classType.getName()));
    }

    private Object getImpl(String key) {
        return map.get(key);
    }

    public ClassMapper getClassMapper() {
        return classMapper;
    }

    public boolean isDisallowUnmappedClasses() {
        return disallowUnmappedClasses;
    }

    public void setDisallowUnmappedClasses(boolean disallowUnmappedClasses) {
        this.disallowUnmappedClasses = disallowUnmappedClasses;
    }

    /**
     * Set the size limit on serialized payloads to read (defaults to 10K/10240 bytes)
     *
     * @return size limit
     */
    public int getMaxReadSize() {
        return maxReadSize;
    }

    public void setMaxReadSize(int maxReadSize) {
        this.maxReadSize = maxReadSize;
    }
}
