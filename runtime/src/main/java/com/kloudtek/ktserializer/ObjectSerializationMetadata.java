/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;
import com.kloudtek.util.io.DataOutputStream;

import java.io.IOException;

/**
 * Created by yannick on 03/01/2015.
 */
public class ObjectSerializationMetadata {
    private static final int VERSION_ZERO = 0;
    private static final int VERSION_BYTE = 1;
    private static final int VERSION_SHORT = 2;
    private static final int VERSION_INT = 3;
    private static final int CLASS_DYNAMIC = 0;
    private static final int CLASS_BYTE = 1;
    private static final int CLASS_SHORT = 2;
    private static final int CLASS_INT = 3;
    private static final int DLEN_REMAINING = 0;
    private static final int DLEN_BYTE = 1;
    private static final int DLEN_SHORT = 2;
    private static final int DLEN_INT = 3;
    private int version;
    private Integer classId;
    private Class<? extends Serializable> classType;
    private boolean compressed;
    private int dataLen = -1;
    private boolean forcedClassId;

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public ObjectSerializationMetadata(DeserializationStream ds, ClassMapper classMapper) throws IOException, InvalidSerializedDataException {
        this(ds, classMapper, null);
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public ObjectSerializationMetadata(DeserializationStream ds, ClassMapper classMapper, Class<? extends Serializable> overrideClassType) throws IOException, InvalidSerializedDataException {
        int flags = ds.readUnsignedByte();
        int versionFlags = flags & 3;
        int classFlags = (flags & 12) >> 2;
        compressed = ((flags & 16) >> 4) > 0;
        int dataLenFlags = (flags & 96) >> 5;
        switch (versionFlags) {
            case VERSION_ZERO:
                version = 0;
                break;
            case VERSION_BYTE:
                version = ds.readUnsignedByte();
                break;
            case VERSION_SHORT:
                version = ds.readUnsignedShort();
                break;
            case VERSION_INT:
                version = ds.readInt();
                break;
            default:
                throw new UnexpectedException("BUG: invalid version flags: " + versionFlags);
        }
        String className = null;
        if (overrideClassType == null) {
            switch (classFlags) {
                case CLASS_DYNAMIC:
                    if (ds.getSerializer().isDisallowUnmappedClasses()) {
                        throw new InvalidSerializedDataException("Dynamic classes are not allowed");
                    }
                    className = ds.readUTF();
                    break;
                case CLASS_BYTE:
                    classId = ds.readUnsignedByte();
                    break;
                case CLASS_SHORT:
                    classId = ds.readUnsignedShort();
                    break;
                case CLASS_INT:
                    classId = ds.readInt();
                    break;
            }
        }
        switch (dataLenFlags) {
            case DLEN_REMAINING:
                dataLen = -1;
                break;
            case DLEN_BYTE:
                dataLen = ds.readUnsignedByte();
                break;
            case DLEN_SHORT:
                dataLen = ds.readUnsignedShort();
                break;
            case DLEN_INT:
                dataLen = ds.readInt();
                break;
        }
        if (overrideClassType == null) {
            if (classId != null) {
                if (classMapper == null) {
                    classMapper = ds.getSerializer().getClassMapper();
                    if (classMapper == null) {
                        throw new InvalidSerializedDataException("Class id can't be mapped: " + classId);
                    }
                }
                className = classMapper.get(classId);
                if (className == null) {
                    throw new InvalidSerializedDataException("Class id can't be mapped: " + classId);
                }
            }
            try {
                classType = (Class<? extends Serializable>) Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new InvalidSerializedDataException(e);
            } catch (ClassCastException e) {
                throw new InvalidSerializedDataException(e);
            }
        } else {
            classType = overrideClassType;
        }
    }

    public ObjectSerializationMetadata(SerializationStream serializationStream, Integer version, Class<? extends Serializable> classType, ClassMapper classMapper, Integer forceClassId) {
        if (version != null) {
            long v = version;
            if (v < 0) {
                throw new IllegalArgumentException("version cannot be a negative number: " + version);
            } else if (v > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("version cannot be higher than " + Integer.MAX_VALUE);
            }
            this.version = version;
        }
        if (forceClassId != null) {
            classId = forceClassId;
            forcedClassId = true;
        } else {
            this.classType = classType;
            if (classMapper == null) {
                classMapper = serializationStream.getSerializer().getClassMapper();
            }
            if (classMapper != null) {
                classId = classMapper.get(classType.getName());
            }
            if (serializationStream.getSerializer().isDisallowUnmappedClasses() && classId == null) {
                throw new IllegalArgumentException("Serialization of un-mapped classes is disallowed");
            }
        }
    }

    public void write(DataOutputStream ss) throws IOException {
        int versionFlags = getVersionFlags();
        int classFlags = getClassFlags();
        int compressionFlags = compressed ? 1 : 0;
        int dataLenFlags = getDataLenFlags();
        ss.write(versionFlags | (classFlags << 2) | (compressionFlags << 4) | (dataLenFlags << 5));
        switch (versionFlags) {
            case VERSION_BYTE:
                ss.write(version);
                break;
            case VERSION_SHORT:
                ss.writeShort(version);
                break;
            case VERSION_INT:
                ss.writeInt(version);
                break;
        }
        if (!forcedClassId) {
            switch (classFlags) {
                case CLASS_DYNAMIC:
                    ss.writeUTF(classType.getName());
                    break;
                case CLASS_BYTE:
                    ss.write(classId);
                    break;
                case CLASS_SHORT:
                    ss.writeShort(classId);
                    break;
                case CLASS_INT:
                    ss.writeInt(classId);
                    break;
            }
        }
        switch (dataLenFlags) {
            case DLEN_BYTE:
                ss.write(dataLen);
                break;
            case DLEN_SHORT:
                ss.writeShort(dataLen);
                break;
            case DLEN_INT:
                ss.writeInt(dataLen);
                break;
        }
    }

    public Integer getVersion() {
        return version;
    }

    public boolean isCompressed() {
        return compressed;
    }

    public void setCompressed(boolean compressed) {
        this.compressed = compressed;
    }

    public int getDataLen() {
        return dataLen;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }

    public Class<? extends Serializable> getClassType() {
        return classType;
    }

    private int getVersionFlags() {
        if (version == 0) {
            return VERSION_ZERO;
        } else if (version <= 255) {
            return VERSION_BYTE;
        } else if (version <= 65535) {
            return VERSION_SHORT;
        } else {
            return VERSION_INT;
        }
    }

    private int getClassFlags() {
        if (classId == null) {
            return CLASS_DYNAMIC;
        } else if (classId <= 255) {
            return CLASS_BYTE;
        } else if (classId <= 65535) {
            return CLASS_SHORT;
        } else {
            return CLASS_INT;
        }
    }

    private int getDataLenFlags() {
        if (dataLen < 0) {
            return DLEN_REMAINING;
        } else if (dataLen <= 255) {
            return DLEN_BYTE;
        } else if (dataLen <= 65535) {
            return DLEN_SHORT;
        } else {
            return DLEN_INT;
        }
    }
}
