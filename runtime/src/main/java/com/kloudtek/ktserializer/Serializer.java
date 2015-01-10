/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.WeakHashMap;

/**
 * Created by yannick on 12/09/2014.
 */
public class Serializer {
    private static SerializationContext globalContext;
    private static ThreadLocal<SerializationContext> threadLocalContexts = new ThreadLocal<SerializationContext>();
    private static WeakHashMap<Class<?>,Metadata> metadataCache = new WeakHashMap<Class<?>, Metadata>();

    static {
        globalContext = new SerializationContext();
//        try {
//            Enumeration<URL> resources = Serializer.class.getClassLoader().getResources("/META-INF/ktserializer");
//            while (resources.hasMoreElements()) {
//                URL resource = resources.nextElement();
//
//            }
//        } catch (IOException e) {
//            throw new UnexpectedException(e);
//        }
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
        if( serializationContext != null ) {
            return serializationContext;
        } else {
            return globalContext;
        }
    }

    public static SerializationContext getGlobalContext() {
        return globalContext;
    }

    public static <S extends Serializable> S deserialize(@NotNull S serializableObj, @NotNull byte[] serialized) throws InvalidSerializedDataException {
        return deserialize(serializableObj, new DeserializationStream(serialized,new SerializationContext(getThreadLocalContext())));
    }

    public static <S extends Serializable> S deserialize(@NotNull S serializableObj, @NotNull byte[] serialized, @NotNull SerializationContext context) throws InvalidSerializedDataException {
        return deserialize(serializableObj, new DeserializationStream(serialized,context));
    }

    public static <S extends Serializable> S deserialize(@NotNull S serializableObj, @NotNull DeserializationStream ds) throws InvalidSerializedDataException {
        try {
            if( serializableObj instanceof CustomSerializable ) {
                ObjectSerializationMetadata metadata = new ObjectSerializationMetadata(ds);
                ((CustomSerializable) serializableObj).deserialize(ds, metadata.getVersion() );
                return serializableObj;
            } else {
                throw new IllegalArgumentException("Unsupported serialization (only CustomSerializable supported at this time)");
            }
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public static <X> X deserialize(@NotNull Class<X> classType, @NotNull byte[] data) throws InstantiationException, InvalidSerializedDataException {
        return deserialize(classType, data, getThreadLocalContext());
    }

    public static <X> X deserialize(@NotNull Class<X> classType, @NotNull byte[] data, @NotNull SerializationContext context) throws InvalidSerializedDataException {
        try {
            try {
                DeserializationStream ds = new DeserializationStream(data, context);
                if (CustomSerializable.class.isAssignableFrom(classType)) {
                    CustomSerializable serializable = (CustomSerializable) classType.newInstance();
                    serializable.deserialize(ds, new ObjectSerializationMetadata(ds).getVersion());
                    return classType.cast(serializable);
                } else {
                    throw new IllegalArgumentException("Class cannot be de-serialized: " + classType.getName());
                }
            } catch (IllegalAccessException e) {
                throw new UnexpectedException(e);
            } catch(InstantiationException e ) {
                throw new IllegalArgumentException("Cannot deserialize class type "+classType.getName()+" because constructor failed: "+e.getMessage(),e);
            }
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public static byte[] serialize(@NotNull Object object) {
        return serialize(object, new SerializationContext(Serializer.getThreadLocalContext()));
    }

    public static byte[] serialize(@NotNull Object object, @NotNull SerializationContext context) {
        try {
            SerializationStream os = new SerializationStream(context);
            if (object instanceof CustomSerializable) {
                ((CustomSerializable) object).serialize(os);
            } else {
                throw new IllegalArgumentException("Class cannot be serialized: " + object.getClass().getName());
            }
            return os.toByteArray();
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    static class Metadata {
    }
}
