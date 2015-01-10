/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;
import org.jetbrains.annotations.NotNull;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * {@link com.kloudtek.ktserializer.Serializable} class which is implemented by custom java code. This class is also
 * {@link java.io.Externalizable} so it supports standard java serialization
 */
public abstract class AbstractCustomSerializable implements CustomSerializable, Externalizable {
    protected AbstractCustomSerializable() {
    }

    public void deserialize(@NotNull byte[] serialized, ObjectSerializationMetadata objectMetadata) throws InvalidSerializedDataException {
        deserialize(serialized,new SerializationContext(), objectMetadata);
    }

    public void deserialize(@NotNull byte[] serialized, @NotNull SerializationContext serializationContext, ObjectSerializationMetadata objectMetadata) throws InvalidSerializedDataException {
        try {
            deserialize(new DeserializationStream(serialized, serializationContext), objectMetadata.getVersion());
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @NotNull
    public byte[] serialize() {
        return serialize(new SerializationContext());
    }

    @NotNull
    public byte[] serialize(@NotNull SerializationContext context) {
        try {
            SerializationStream os = new SerializationStream(context);
            serialize(os);
            return os.toByteArray();
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        byte[] serialized = Serializer.serialize(this);
        out.writeInt(serialized.length);
        out.writeObject(serialized);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        int i = in.readInt();
        if( i > 0 ) {
            byte[] data = new byte[i];
            in.readFully(data);
            try {
                Serializer.deserialize(this,data);
            } catch (InvalidSerializedDataException e) {
                throw new IOException();
            }
        }
    }
}
