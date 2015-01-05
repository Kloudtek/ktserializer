/*
 * Copyright (c) 2014 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.io.ByteArrayDataOutputStream;
import com.kloudtek.util.io.DataInputStream;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by yannick on 30/12/2014.
 */
public class SerializationStream extends ByteArrayDataOutputStream {
    private @NotNull SerializationContext context;

    public SerializationStream(@NotNull SerializationContext context) {
        super();
        this.context = context;
    }

    @NotNull
    public SerializationContext getContext() {
        return context;
    }

    public void writeObject( Serializable serializable ) throws IOException {
        if( serializable instanceof CustomSerializable ) {
            new ObjectSerializationMetadata(((CustomSerializable) serializable).getVersion()).write(this);
            writeData(Serializer.serialize(serializable));
        } else {
            throw new IllegalArgumentException("Only CustomSerializable supported at this time");
        }
    }
}
