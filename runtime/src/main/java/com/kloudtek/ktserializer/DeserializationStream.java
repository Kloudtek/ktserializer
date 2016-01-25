/*
 * Copyright (c) 2016 Kloudtek Ltd
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
    private SerializationEngine serializer;

    public DeserializationStream(@NotNull byte[] data, @NotNull SerializationEngine serializer) throws InvalidSerializedDataException {
        super(new ByteArrayInputStream(data));
        this.serializer = serializer;
        try {
            // no flags in use at the moment
            long flags = readUnsignedNumber();
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    @NotNull
    private <X extends Serializable> X readObject(X object, boolean specific) throws IOException, InvalidSerializedDataException {
        Class<? extends Serializable> expectedClass = object.getClass();
        SerializedDataHeader serializationMetadata = new SerializedDataHeader(this, serializer.getClassMapper(), specific ? object.getClass() : null);
        if (!serializationMetadata.getClassType().equals(expectedClass)) {
            throw new InvalidSerializedDataException("Object data of class " + serializationMetadata.getClassType().getName() + " does not match expected " + expectedClass.getName());
        }
        deserialize(object, serializationMetadata, this);
        return object;
    }

    @NotNull
    Serializable readObject() throws IOException, InvalidSerializedDataException {
        SerializedDataHeader serializationMetadata = new SerializedDataHeader(this, serializer.getClassMapper(), null);
        try {
            Serializable object = serializationMetadata.getClassType().newInstance();
            deserialize(object, serializationMetadata, this);
            return object;
        } catch (InstantiationException e) {
            throw new InvalidSerializedDataException("Unable to create serialized class " + serializationMetadata.getClassType().getName() + ": " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new InvalidSerializedDataException("Unable to create serialized class " + serializationMetadata.getClassType().getName() + ": " + e.getMessage(), e);
        }
    }

    @NotNull
    public SerializationEngine getSerializer() {
        return serializer;
    }

    public <X extends Serializable> X readObject(X object) throws IOException, InvalidSerializedDataException {
        return readObject(object, false);
    }

    public <X extends Serializable> X readSpecificObject(X object) throws IOException, InvalidSerializedDataException {
        return readObject(object, true);
    }

    public <X extends Serializable> X readObject(Class<X> expectedClass) throws IOException, InvalidSerializedDataException {
        Serializable obj = readObject();
        if (!expectedClass.isInstance(obj)) {
            throw new IllegalArgumentException("Invalid class deserialized " + obj.getClass().getName() + " is not " + expectedClass.getName());
        }
        return expectedClass.cast(obj);
    }

    public <X extends Serializable> List<X> readObjectList(Class<X> expectedClass) throws IOException, InvalidSerializedDataException {
        ArrayList<X> list = new ArrayList<X>();
        for (Serializable obj : readSpecificObject(SerializableList.class)) {
            try {
                list.add(expectedClass.cast(obj));
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Invalid class deserialized " + obj.getClass().getName() + " is not " + expectedClass.getName());
            }
        }
        return list;
    }

    @SuppressWarnings("unchecked")
    public List<Serializable> readObjectList() throws IOException, InvalidSerializedDataException {
        return readObjectList(Serializable.class);
    }

    public <X extends Serializable> X readSpecificObject(Class<X> specificClass) throws IOException, InvalidSerializedDataException {
        SerializedDataHeader serializationMetadata = new SerializedDataHeader(this, serializer.getClassMapper(), specificClass);
        try {
            Serializable serializable = serializationMetadata.getClassType().newInstance();
            try {
                X object = specificClass.cast(serializable);
                deserialize(object, serializationMetadata, this);
                return object;
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Invalid class deserialized " + readObject().getClass().getName() + " is not " + specificClass.getName());
            }
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
