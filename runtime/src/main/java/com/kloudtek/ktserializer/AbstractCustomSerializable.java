/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * {@link com.kloudtek.ktserializer.Serializable} class which is implemented by custom java code. This class is also
 * {@link java.io.Externalizable} so it supports standard java serialization
 */
public abstract class AbstractCustomSerializable implements CustomSerializable {
    protected AbstractCustomSerializable() {
    }

    public void deserialize(@NotNull byte[] serialized, @NotNull Serializer serializer, @NotNull ObjectSerializationMetadata objectMetadata) throws InvalidSerializedDataException {
        try {
            deserialize(new DeserializationStream(serialized, serializer), objectMetadata.getVersion());
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @NotNull
    public byte[] serialize(@NotNull Serializer serializer) {
        try {
            SerializationStream os = new SerializationStream(serializer);
            serialize(os);
            return os.toByteArray();
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }
}
