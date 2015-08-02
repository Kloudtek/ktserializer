/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;
import com.kloudtek.util.io.DataInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yannick on 30/12/2014.
 */
public class DeserializationStream extends DataInputStream {
    @NotNull
    private Serializer serializer;

    public DeserializationStream(@NotNull byte[] data, @NotNull Serializer serializer) throws InvalidSerializedDataException {
        super(new ByteArrayInputStream(data));
        this.serializer = serializer;
        try {
            // no flags in use at the moment
            byte flags = readByte();
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    @NotNull
    public Serializer getSerializer() {
        return serializer;
    }

    public <X extends Serializable> X readObject(X object) throws IOException, InvalidSerializedDataException {
        Class<? extends Serializable> expectedClass = object.getClass();
        SerializedDataHeader serializationMetadata = new SerializedDataHeader(this, null);
        if (!serializationMetadata.getClassType().equals(expectedClass)) {
            throw new InvalidSerializedDataException("Object data of class " + serializationMetadata.getClassType().getName() + " does not match expected " + expectedClass.getName());
        }
        deserialize(object, serializationMetadata, this);
        return object;
    }

    public Serializable readObject() throws IOException, InvalidSerializedDataException {
        return readObject(serializer.getClassMapper(), null);
    }

    public <X extends Serializable> X readObject(Class<X> expectedClass) throws IOException, InvalidSerializedDataException {
        return readObject(expectedClass, null);
    }

    <X extends Serializable> X readObject(Class<X> expectedClass, ClassMapper classMapper) throws IOException, InvalidSerializedDataException {
        Serializable obj = readObject(classMapper, null);
        if (!expectedClass.isInstance(obj)) {
            throw new IllegalArgumentException("Invalid class deserialized " + obj.getClass().getName());
        }
        return expectedClass.cast(obj);
    }

    public <X extends Serializable> List<X> readObjectList(Class<X> expectedClass) throws IOException, InvalidSerializedDataException {
        return readObjectList(expectedClass, null);
    }

    @SuppressWarnings("unchecked")
    public <X extends Serializable> List<X> readObjectList(Class<X> expectedClass, ClassMapper classMapper) throws IOException, InvalidSerializedDataException {
        return new ArrayList<X>((SerializableList) readObject(classMapper, SerializableList.class));
    }

    @SuppressWarnings("unchecked")
    public List<Serializable> readObjectList(ClassMapper classMapper) throws IOException, InvalidSerializedDataException {
        return new ArrayList<Serializable>((SerializableList) readObject(classMapper, SerializableList.class));
    }

    Serializable readObject(ClassMapper classMapper, Class<? extends Serializable> overrideClass) throws IOException, InvalidSerializedDataException {
        SerializedDataHeader serializationMetadata = new SerializedDataHeader(this, classMapper, overrideClass);
        try {
            Serializable object = serializationMetadata.getClassType().newInstance();
            deserialize(object, serializationMetadata, this);
            return object;
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Cannot instantiate class type " + serializationMetadata.getClassType().getName());
        } catch (IllegalAccessException e) {
            throw new UnexpectedException(e);
        }
    }

    private <X extends Serializable> void deserialize(X object, SerializedDataHeader serializationMetadata, DeserializationStream ds) throws IOException, InvalidSerializedDataException {
        if (object instanceof CustomSerializable) {
            ((CustomSerializable) object).deserialize(ds, serializationMetadata.getVersion());
        } else {
            throw new IllegalArgumentException("Only CustomSerializable supported at this time");
        }
    }
}
