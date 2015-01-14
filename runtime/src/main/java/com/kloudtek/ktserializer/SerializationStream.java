/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.io.ByteArrayDataOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Created by yannick on 30/12/2014.
 */
public class SerializationStream extends ByteArrayDataOutputStream {
    private @NotNull SerializationContext context;
    private ByteArrayDataOutputStream stream;

    public SerializationStream(@NotNull SerializationContext context) throws IOException {
        super();
        this.context = context;
        // payload flags (nothing at the moment)
        write(0);
    }

    @NotNull
    public SerializationContext getContext() {
        return context;
    }

    public void writeObject(Serializable serializable, ClassMapper classMapper) throws IOException {
        if( serializable instanceof CustomSerializable ) {
            writeObject(serializable, classMapper, true);
        } else {
            throw new IllegalArgumentException("Only CustomSerializable supported at this time");
        }
    }

    void writeObject(Serializable serializable, ClassMapper classMapper, boolean subStream) throws IOException {
        SerializationStream ss = subStream ? new SerializationStream(context) : this;
        final ObjectSerializationMetadata metadata = new ObjectSerializationMetadata(
                ((CustomSerializable) serializable).getVersion(), serializable.getClass(), classMapper);
        metadata.write(ss);
        ((CustomSerializable) serializable).serialize(ss);
        if (subStream) {
            writeData(ss.toByteArray());
        }
    }
}
