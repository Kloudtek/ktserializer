/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;
import com.kloudtek.util.io.DataInputStream;
import com.kloudtek.util.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.zip.GZIPInputStream;

/**
 * Created by yannick on 30/12/2014.
 */
public class DeserializationStream extends DataInputStream {
    @NotNull
    private Serializer serializer;
    private final Stack<InputStream> streams = new Stack<InputStream>();
    private ByteArrayInputStream buffer;

    public DeserializationStream(@NotNull byte[] data, @NotNull Serializer serializer) throws InvalidSerializedDataException {
        super(new ByteArrayInputStream(data));
        buffer = (ByteArrayInputStream) in;
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
            if (data.length > 0) {
                readFully(data);
            }
            return data;
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    public <X extends Serializable> X readObject(X object) throws IOException, InvalidSerializedDataException {
        return readObject(object, true);
    }

    public <X extends Serializable> X readObject(X object, boolean subStream) throws IOException, InvalidSerializedDataException {
        DeserializationStream ds = subStream ? new DeserializationStream(readData(), serializer) : this;
        Class<? extends Serializable> expectedClass = object.getClass();
        ObjectSerializationMetadata serializationMetadata = new ObjectSerializationMetadata(ds, null);
        if (!serializationMetadata.getClassType().equals(expectedClass)) {
            throw new InvalidSerializedDataException("Object data of class " + serializationMetadata.getClassType().getName() + " does not match expected " + expectedClass.getName());
        }
        deserialize(object, serializationMetadata, ds);
        return object;
    }

    public Serializable readObject() throws IOException, InvalidSerializedDataException {
        return readObject((ClassMapper) null, null);
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
        ObjectSerializationMetadata serializationMetadata = new ObjectSerializationMetadata(this, classMapper, overrideClass);
        try {
            byte[] objectData;
            if (serializationMetadata.getDataLen() < 0) {
                objectData = readRemaining();
            } else {
                objectData = new byte[serializationMetadata.getDataLen()];
                readFully(objectData);
            }
            if (serializationMetadata.isCompressed()) {
                GZIPInputStream gi = new GZIPInputStream(new ByteArrayInputStream(objectData), 10240);
                try {
                    objectData = IOUtils.toByteArray(gi);
                } finally {
                    gi.close();
                }
            }
            streams.push(in);
            in = new ByteArrayInputStream(objectData);
            Serializable object = serializationMetadata.getClassType().newInstance();
            deserialize(object, serializationMetadata, this);
            in = streams.pop();
            return object;
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Cannot instantiate class type " + serializationMetadata.getClassType().getName());
        } catch (IllegalAccessException e) {
            throw new UnexpectedException(e);
        }
    }

    private <X extends Serializable> void deserialize(X object, ObjectSerializationMetadata serializationMetadata, DeserializationStream ds) throws IOException, InvalidSerializedDataException {
        if (object instanceof CustomSerializable) {
            ((CustomSerializable) object).deserialize(ds, serializationMetadata.getVersion());
        } else {
            throw new IllegalArgumentException("Only CustomSerializable supported at this time");
        }
    }
}
