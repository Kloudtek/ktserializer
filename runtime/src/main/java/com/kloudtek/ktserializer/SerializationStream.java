/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.io.ByteArrayDataOutputStream;
import com.kloudtek.util.io.DataOutputStream;
import com.kloudtek.util.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by yannick on 30/12/2014.
 */
public class SerializationStream extends DataOutputStream {
    @NotNull
    private SerializationEngine serializer;

    public SerializationStream(@NotNull SerializationEngine serializer) throws IOException {
        super(new ByteArrayDataOutputStream());
        this.serializer = serializer;
        // payload flags (nothing at the moment)
        writeUnsignedNumber(0L);
    }

    @NotNull
    public SerializationEngine getSerializer() {
        return serializer;
    }

    public void writeObject(Serializable serializable) throws IOException {
        writeObject(serializable, null);
    }

    public void writeObject(Serializable serializable, ClassMapper classMapper) throws IOException {
        if (serializable instanceof CustomSerializable) {
            writeObject(serializable, classMapper, false);
        } else {
            throw new IllegalArgumentException("Only CustomSerializable supported at this time");
        }
    }

    public void writeObjectList(Collection<? extends Serializable> collection) throws IOException {
        writeObjectList(collection, serializer.getClassMapper());
    }

    public void writeObjectList(Collection<? extends Serializable> collection, ClassMapper classMapper) throws IOException {
        writeObject(new SerializableList(collection), classMapper, true);
    }

    public void writeObject(Serializable serializable, ClassMapper classMapper, boolean specificClass) throws IOException {
        final SerializedDataHeader metadata = new SerializedDataHeader(this,
                ((CustomSerializable) serializable).getVersion(), serializable.getClass(), classMapper, specificClass);
        metadata.write(this);
        ((CustomSerializable) serializable).serialize(this);
    }

    public byte[] closeAndReturnData() {
        ByteArrayDataOutputStream buffer = (ByteArrayDataOutputStream) this.out;
        IOUtils.close(buffer);
        return buffer.toByteArray();
    }
}
