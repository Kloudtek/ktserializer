/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yannick on 12/09/2014.
 */
public class Serializer {
    protected final HashMap<String, Object> map = new HashMap<String, Object>();
    private ClassMapper classMapper;
    private boolean disallowUnmappedClasses = false;
    private int maxReadSize;

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
            return ds.readObject(classType, classMapper != null ? classMapper : this.classMapper);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public Serializable deserialize(@NotNull byte[] serializedData) throws InvalidSerializedDataException {
        return deserialize(serializedData, null);
    }

    public Serializable deserialize(@NotNull byte[] serializedData, @Nullable ClassMapper classMapper) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readObject(classMapper != null ? classMapper : this.classMapper, null);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public List<Serializable> deserializeList(@NotNull byte[] serializedData) throws InvalidSerializedDataException {
        return deserializeList(serializedData, null);
    }

    public List<Serializable> deserializeList(@NotNull byte[] serializedData, @Nullable ClassMapper classMapper) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readObjectList(classMapper != null ? classMapper : this.classMapper);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public <S extends Serializable> List<S> deserializeList(@NotNull Class<S> classType, @NotNull byte[] serializedData, @Nullable ClassMapper classMapper) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readObjectList(classType, classMapper != null ? classMapper : this.classMapper);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public byte[] serialize(@NotNull Serializable object) {
        return serialize(object, null);
    }

    public byte[] serialize(@NotNull Serializable object, @Nullable ClassMapper classMapper) {
        return serialize(object, classMapper, false);
    }

    public byte[] serialize(@NotNull List<? extends Serializable> list) {
        return serialize(list, null);
    }

    public byte[] serialize(@NotNull List<? extends Serializable> list, @Nullable ClassMapper classMapper) {
        return serialize(list, classMapper, false);
    }

    public byte[] serialize(@NotNull Serializable object, @Nullable ClassMapper classMapper, boolean compression) {
        try {
            SerializationStream os = new SerializationStream(this, compression);
            os.writeObject(object, classMapper != null ? classMapper : this.classMapper, false, null);
            return os.closeAndReturnData();
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    public byte[] serialize(@NotNull Collection<? extends Serializable> collection, @Nullable ClassMapper classMapper, boolean compression) {
        try {
            SerializationStream os = new SerializationStream(this, compression);
            os.writeObjectList(collection, classMapper != null ? classMapper : this.classMapper, false);
            return os.closeAndReturnData();
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
     * Set the size limit on serialized payloads to read (defaults to 10K - 10240 bytes
     *
     * @return
     */
    public int getMaxReadSize() {
        return maxReadSize;
    }

    public void setMaxReadSize(int maxReadSize) {
        this.maxReadSize = maxReadSize;
    }
}
