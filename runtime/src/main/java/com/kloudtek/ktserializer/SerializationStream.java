/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.io.ByteArrayDataOutputStream;
import com.kloudtek.util.io.DataOutputStream;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Stack;
import java.util.zip.GZIPOutputStream;

/**
 * Created by yannick on 30/12/2014.
 */
public class SerializationStream extends DataOutputStream {
    @NotNull
    private Serializer serializer;
    private final boolean compressed;
    private ByteArrayDataOutputStream dataBuffer;
    private final Stack<OutputStream> streams = new Stack<OutputStream>();
    private boolean disallowUnmappedClasses;

    public SerializationStream(@NotNull Serializer serializer, boolean compressed) throws IOException {
        super(new ByteArrayDataOutputStream());
        dataBuffer = (ByteArrayDataOutputStream) out;
        this.serializer = serializer;
        this.compressed = compressed;
        // payload flags (nothing at the moment)
        write(0);
    }

    @NotNull
    public Serializer getSerializer() {
        return serializer;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void writeObject(Serializable serializable) throws IOException {
        writeObject(serializable, null);
    }

    public void writeObject(Serializable serializable, ClassMapper classMapper) throws IOException {
        if (serializable instanceof CustomSerializable) {
            writeObject(serializable, classMapper, true, null);
        } else {
            throw new IllegalArgumentException("Only CustomSerializable supported at this time");
        }
    }

    public void writeObjectList(Collection<? extends Serializable> collection) throws IOException {
        writeObjectList(collection, null, true);
    }

    public void writeObjectList(Collection<? extends Serializable> collection, ClassMapper classMapper) throws IOException {
        writeObjectList(collection, classMapper, true);
    }

    void writeObjectList(Collection<? extends Serializable> collection, ClassMapper classMapper, boolean subStream) throws IOException {
        writeObject(new SerializableList<Serializable>(collection), classMapper, subStream, 0);
    }

    void writeObject(Serializable serializable, ClassMapper classMapper, boolean subStream, Integer forceClassId) throws IOException {
        final ObjectSerializationMetadata metadata = new ObjectSerializationMetadata(this,
                ((CustomSerializable) serializable).getVersion(), serializable.getClass(), classMapper, forceClassId);
        streams.push(out);
        ByteArrayDataOutputStream objectBuffer = new ByteArrayDataOutputStream();
        out = objectBuffer;
        try {
            ((CustomSerializable) serializable).serialize(this);
        } finally {
            objectBuffer.close();
        }
        byte[] objectData = objectBuffer.toByteArray();
        if (compressed && !subStream) {
            ByteArrayDataOutputStream compressedBuffer = new ByteArrayDataOutputStream();
            GZIPOutputStream gs = new GZIPOutputStream(compressedBuffer, 10240);
            try {
                gs.write(objectData);
            } finally {
                gs.close();
            }
            byte[] compressedData = compressedBuffer.toByteArray();
            if (compressedData.length < objectData.length) {
                objectData = compressedData;
                metadata.setCompressed(true);
            }
        }
        metadata.setDataLen(subStream ? objectData.length : -1);
        out = streams.pop();
        metadata.write(this);
        out.write(objectData);
    }

    public boolean isDisallowUnmappedClasses() {
        return disallowUnmappedClasses;
    }

    public void setDisallowUnmappedClasses(boolean disallowUnmappedClasses) {
        this.disallowUnmappedClasses = disallowUnmappedClasses;
    }

    public byte[] closeAndReturnData() {
        return dataBuffer.toByteArray();
    }
}
