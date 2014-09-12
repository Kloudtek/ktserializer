/*
 * Copyright (c) 2014 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;
import com.kloudtek.util.io.*;

import java.io.*;

/**
 * Created by yannick on 12/09/2014.
 */
public abstract class AbstractSerializable implements Serializable {
    protected AbstractSerializable() {
    }

    protected AbstractSerializable(byte[] serialized) throws InvalidSerializedDataException {
        deserialize(serialized);
    }

    @Override
    public byte[] serialize() {
        try {
            ByteArrayDataOutputStream os = new ByteArrayDataOutputStream();
            try {
                serialize(os);
            } finally {
                os.close();
            }
            return os.toByteArray();
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    @Override
    public void deserialize(byte[] data) throws InvalidSerializedDataException {
        try {
            deserialize(new ByteArrayDataInputStream(data));
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }
}
