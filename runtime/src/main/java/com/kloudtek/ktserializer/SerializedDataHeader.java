/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.io.DataOutputStream;

import java.io.IOException;
import java.util.BitSet;

/**
 * Created by yannick on 03/01/2015.
 */
public class SerializedDataHeader {
    public static final long[] EMPTYFLAGS = new long[]{0L};
    private int version;
    private ClassId classId;
    private Class<? extends Serializable> classType;
    private boolean specificClass;

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public SerializedDataHeader(DeserializationStream ds, ClassMapper classMapper) throws IOException, InvalidSerializedDataException {
        this(ds, classMapper, null);
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public SerializedDataHeader(DeserializationStream ds, ClassMapper classMapper, Class<? extends Serializable> specificClass) throws IOException, InvalidSerializedDataException {
        if (classMapper == null) {
            classMapper = ds.getSerializer().getClassMapper();
        }
        String className;
        int flagsBytes = ds.readUnsignedByte();
        BitSet flags = BitSet.valueOf(new long[]{(long) flagsBytes});
        boolean specific = checkFlag(flags, Flags.SPECIFIC);
        boolean longLibId = checkFlag(flags, Flags.LONGLIBID);
        version = (int) ds.readUnsignedNumber();
        if (specific && specificClass == null) {
            throw new InvalidSerializedDataException("Class was serialized as specific but no specific class was provided: " + specificClass);
        } else if (!specific && specificClass != null) {
            throw new InvalidSerializedDataException("Class was not serialized as specific but a specific class was provided: " + specificClass);
        }
        if (!specific) {
            int classId = (int) ds.readUnsignedNumber();
            if (classId == 0) {
                className = ds.readUTF();
                if (ds.getSerializer().isDisallowUnmappedClasses()) {
                    throw new InvalidSerializedDataException("Dynamic classes are not allowed: " + className);
                }
            } else {
                LibraryId libraryId;
                if (longLibId) {
                    libraryId = new LongLibraryId(ds.readUTF());
                } else {
                    libraryId = new ShortLibraryId((short) ds.readUnsignedByte());
                }
                className = classMapper.get(libraryId, classId - 1);
                if (className == null) {
                    throw new InvalidSerializedDataException("Invalid class id: " + new ClassId(libraryId, classId));
                }
            }
            try {
                this.classType = (Class<? extends Serializable>) Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new InvalidSerializedDataException(e);
            } catch (ClassCastException e) {
                throw new InvalidSerializedDataException(e);
            }
        } else {
            this.classType = specificClass;
        }
    }

    public SerializedDataHeader(SerializationStream serializationStream, Integer version, Class<? extends Serializable> classType, ClassMapper classMapper, boolean specificClass) {
        this.specificClass = specificClass;
        if (version != null) {
            long v = version;
            if (v < 0) {
                throw new IllegalArgumentException("version cannot be a negative number: " + version);
            } else if (v > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("version cannot be higher than " + Integer.MAX_VALUE);
            }
            this.version = version;
        }
        this.classType = classType;
        if (classMapper == null) {
            classMapper = serializationStream.getSerializer().getClassMapper();
        }
        if (classMapper != null) {
            classId = classMapper.get(classType.getName());
        }
        if (!specificClass && serializationStream.getSerializer().isDisallowUnmappedClasses() && classId == null) {
            throw new IllegalArgumentException("Serialization of un-mapped classes is disallowed: " + classType.getName());
        }
    }

    public void write(DataOutputStream ss) throws IOException {
        BitSet flags = new BitSet();
        if (specificClass) {
            setFlag(flags, Flags.SPECIFIC);
        }
        boolean longId = classId != null && classId.getLibraryId() instanceof LongLibraryId;
        if (longId) {
            setFlag(flags, Flags.LONGLIBID);
        }
        long[] flagsBytes = flags.toLongArray();
        if (flagsBytes.length == 0) {
            flagsBytes = EMPTYFLAGS;
        }
        ss.writeUnsignedNumber(flagsBytes[0]);
        ss.writeUnsignedNumber(version);
        if (!specificClass) {
            if (classId != null) {
                ss.writeUnsignedNumber(classId.getClassId() + 1);
                if (longId) {
                    ss.writeUTF(classId.getLibraryId().toString());
                } else {
                    ss.writeByte(((ShortLibraryId) classId.getLibraryId()).getId());
                }
            } else {
                ss.writeUnsignedNumber(0);
                ss.writeUTF(classType.getName());
            }
        }
    }

    private void setFlag(BitSet flags, Flags flag) {
        flags.set(flag.ordinal());
    }

    private boolean checkFlag(BitSet flags, Flags flag) {
        return flags.get(flag.ordinal());
    }

    public Integer getVersion() {
        return version;
    }

    public Class<? extends Serializable> getClassType() {
        return classType;
    }

    public enum Flags {
        // new flags must always be added at the end of this list, change to the order will break all serialized data !!!!
        SPECIFIC, LONGLIBID
    }
}
