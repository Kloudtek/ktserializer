/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import java.io.IOException;
import java.util.BitSet;

/**
 * Created by yannick on 03/01/2015.
 */
public class ObjectSerializationMetadata {
    private static final short FLAG_VERSIONED = 1;
    private static final short FLAG_CLASSID = 2;
    private static final int MAX_BYTE = ( Byte.MAX_VALUE * 2 ) + 1;
    private int version;
    private Integer classId;

    public ObjectSerializationMetadata(DeserializationStream ds) throws IOException {
        BitSet bitSet = BitSet.valueOf(new byte[]{ds.readByte()});
        if (bitSet.get(FLAG_VERSIONED)) {
            version = ds.readUnsignedByte();
        } else {
            version = 0;
        }
        if (bitSet.get(FLAG_CLASSID)) {
            classId = ds.readUnsignedByte();
        }
    }

    public ObjectSerializationMetadata(Integer version) {
        if (version != null) {
            long v = version;
            if (v < 0) {
                throw new IllegalArgumentException("version cannot be a negative number: " + version);
            } else if (v > MAX_BYTE) {
                throw new IllegalArgumentException("version cannot be higher than unsigned byte at this time");
            }
            this.version = version;
        }
    }

    public ObjectSerializationMetadata(Integer version, Integer classId) {
        this(version);
        this.classId = classId;
    }

    public void write(SerializationStream ss) throws IOException {
        BitSet bitSet = new BitSet();
        bitSet.set(FLAG_VERSIONED, version > 0);
        bitSet.set(FLAG_CLASSID, classId != null);
        ss.writeByte(bitSet.toByteArray()[0]);
        if (version > 0) {
            ss.write(version);
        }
        if (classId != null) {
            ss.write(classId);
        }
    }

    public Integer getVersion() {
        return version;
    }

    public Integer getClassId() {
        return classId;
    }
}
