/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.WeakHashMap;

/**
 * Created by yannick on 12/09/2014.
 */
public class Serializer {
    private static final SerializationContext globalContext;
    private static final ThreadLocal<SerializationContext> threadLocalContexts = new ThreadLocal<SerializationContext>();
    static final WeakHashMap<Class<? extends Serializable>, ClassMapper> classMappersCache = new WeakHashMap<Class<? extends Serializable>, ClassMapper>();

    static {
        globalContext = new SerializationContext();
    }

    public static SerializationContext createThreadLocalContext() {
        SerializationContext ctx = new SerializationContext(globalContext);
        threadLocalContexts.set(ctx);
        return ctx;
    }

    public static void removeThreadLocalContext() {
        threadLocalContexts.remove();
    }

    public static SerializationContext getThreadLocalContext() {
        SerializationContext serializationContext = threadLocalContexts.get();
        if (serializationContext != null) {
            return serializationContext;
        } else {
            return globalContext;
        }
    }

    public static SerializationContext getGlobalContext() {
        return globalContext;
    }

    public static <X extends Serializable> X deserialize(@NotNull Class<X> classType, @NotNull byte[] serializedData) throws InstantiationException, InvalidSerializedDataException {
        return deserialize(classType, serializedData, getThreadLocalContext(), null);
    }

    public static <X extends Serializable> X deserialize(@NotNull Class<X> classType, @NotNull byte[] serializedData, @Nullable ClassMapper classMapper) throws InstantiationException, InvalidSerializedDataException {
        return deserialize(classType, serializedData, getThreadLocalContext(), classMapper);
    }

    public static <S extends Serializable> S deserialize(@NotNull S serializableObj, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        return deserialize(serializableObj, serializedData, getThreadLocalContext());
    }

    public static <S extends Serializable> S deserialize(@NotNull S serializableObj, @NotNull byte[] serializedData, @NotNull SerializationContext context) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, context);
            return ds.readObject(serializableObj, false);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public static <X extends Serializable> X deserialize(@NotNull Class<X> classType, @NotNull byte[] serializedData, @NotNull SerializationContext context, @Nullable ClassMapper classMapper) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, context);
            return ds.readObject(classType, classMapper, false);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public static byte[] serialize(@NotNull Serializable object) {
        return serialize(object, null, null);
    }

    public static byte[] serialize(@NotNull Serializable object, @Nullable ClassMapper classMapper) {
        return serialize(object, null, classMapper);
    }

    public static byte[] serialize(@NotNull Serializable object, @Nullable SerializationContext context) {
        return serialize(object, context, null);
    }

    public static byte[] serialize(@NotNull Serializable object, @Nullable SerializationContext context, @Nullable ClassMapper classMapper) {
        try {
            if (context == null) {
                context = Serializer.getThreadLocalContext();
            }
            SerializationStream os = new SerializationStream(context);
            os.writeObject(object, classMapper, false);
            return os.toByteArray();
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    public static ClassMapper getClassMapper(Class<? extends Serializable> classType) {
        return null;
    }
}
