/*
 * Copyright (c) 2014 Kloudtek Ltd
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
    private @NotNull SerializationContext context;

    public DeserializationStream(@NotNull byte[] data, @NotNull SerializationContext context) throws InvalidSerializedDataException {
        super(data);
        this.context = context;
        try {
            byte flag = readByte();
            if( (flag & VERSIONED) == VERSIONED ) {
                context.setVersion(readUnsignedShort());
            }
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    @NotNull
    public SerializationContext getContext() {
        return context;
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
        ObjectSerializationMetadata serializationMetadata = new ObjectSerializationMetadata(this);
        deserialize(object,serializationMetadata);
        return object;
    }

    @SuppressWarnings("unchecked")
    public <X extends Serializable> X readObject(ClassMapper classMapper) throws IOException, InvalidSerializedDataException {
        ObjectSerializationMetadata serializationMetadata = new ObjectSerializationMetadata(this);
        Class<? extends Serializable> classType = classMapper.get(serializationMetadata.getClassId());
        try {
            Serializable object = classType.newInstance();
            deserialize(object, serializationMetadata);
            return (X) object;
        } catch (InstantiationException e) {
            throw new IllegalArgumentException("Cannot instantiate class type "+classType);
        } catch (IllegalAccessException e) {
            throw new UnexpectedException(e);
        }
    }

    private <X extends Serializable> void deserialize(X object, ObjectSerializationMetadata serializationMetadata) throws IOException, InvalidSerializedDataException {
        if( object instanceof CustomSerializable) {
            ((CustomSerializable) object).deserialize(new DeserializationStream(readData(), context), serializationMetadata.getVersion() );
        } else {
            throw new IllegalArgumentException("Only CustomSerializable supported at this time");
        }
    }
}
