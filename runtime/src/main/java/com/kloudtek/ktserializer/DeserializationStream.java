/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;
import com.kloudtek.util.io.ByteArrayDataInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Created by yannick on 30/12/2014.
 */
public class DeserializationStream extends ByteArrayDataInputStream {
    private final byte VERSIONED = 1;
    private final byte COMPRESSED = 2;
    private
    @NotNull
    Serializer serializer;

    public DeserializationStream(@NotNull byte[] data, @NotNull Serializer serializer) throws InvalidSerializedDataException {
        super(data);
        this.serializer = serializer;
        try {
            byte flag = readByte();
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    @NotNull
    public Serializer getSerializer() {
        return serializer;
    }

    public byte[] readRemaining() {
        try {
            byte[] data = new byte[available()];
            if( data.length > 0 ) {
                readFully(data);
            }
            return data;
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    public <X extends Serializable> X readObject( X object ) throws IOException, InvalidSerializedDataException {
        return readObject(object, true);
    }

    public <X extends Serializable> X readObject(X object, boolean subStream) throws IOException, InvalidSerializedDataException {
        DeserializationStream ds = subStream ? new DeserializationStream(readData(), serializer) : this;
        Class<? extends Serializable> expectedClass = object.getClass();
        ObjectSerializationMetadata serializationMetadata = new ObjectSerializationMetadata(ds, expectedClass, null);
        if (!serializationMetadata.getClassType().equals(expectedClass)) {
            throw new InvalidSerializedDataException("Object data of class " + serializationMetadata.getClassType().getName() + " does not match expected " + expectedClass.getName());
        }
        deserialize(object, serializationMetadata, ds);
        return object;
    }

    public <X extends Serializable> X readObject(Class<X> expectedClass) throws IOException, InvalidSerializedDataException {
        return readObject(expectedClass, null);
    }

    public <X extends Serializable> X readObject(Class<X> expectedClass, ClassMapper classMapper) throws IOException, InvalidSerializedDataException {
        return readObject(expectedClass, classMapper, true);
    }

    public <X extends Serializable> X readObject(Class<X> expectedClass, ClassMapper classMapper, boolean subStream) throws IOException, InvalidSerializedDataException {
        DeserializationStream ds = subStream ? new DeserializationStream(readData(), serializer) : this;
        ObjectSerializationMetadata serializationMetadata = new ObjectSerializationMetadata(ds, expectedClass, classMapper);
        try {
            Serializable object = serializationMetadata.getClassType().newInstance();
            deserialize(object, serializationMetadata, ds);
            return expectedClass.cast(object);
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Cannot instantiate class type " + serializationMetadata.getClassType().getName());
        } catch (IllegalAccessException e) {
            throw new UnexpectedException(e);
        }
    }

    private <X extends Serializable> void deserialize(X object, ObjectSerializationMetadata serializationMetadata, DeserializationStream ds) throws IOException, InvalidSerializedDataException {
        if( object instanceof CustomSerializable) {
            ((CustomSerializable) object).deserialize(ds, serializationMetadata.getVersion());
        } else {
            throw new IllegalArgumentException("Only CustomSerializable supported at this time");
        }
    }
}
