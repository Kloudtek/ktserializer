/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;

import java.io.IOException;

/**
 * Created by yannick on 03/01/2015.
 */
public class ObjectSerializationMetadata {
    private static final int VERSION_ZERO = 0;
    private static final int VERSION_BYTE = 1;
    private static final int VERSION_SHORT = 2;
    private static final int VERSION_INT = 3;
    private static final int CLASS_UNKNOWN = 0;
    private static final int CLASS_DYNAMIC = 1;
    private static final int CLASS_BYTE = 2;
    private static final int CLASS_SHORT = 3;
    private int version;
    private Integer classId;
    private Class<? extends Serializable> classType;

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public ObjectSerializationMetadata(DeserializationStream ds, Class<? extends Serializable> expectedClassType, ClassMapper classMapper) throws IOException, InvalidSerializedDataException {
        int flags = ds.readUnsignedByte();
        int versionFlags = flags & 3;
        int classFlags = (flags & 12) >> 2;
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
        if (classFlags == CLASS_UNKNOWN) {
            classType = expectedClassType;
        } else if (classFlags == CLASS_DYNAMIC) {
            className = ds.readUTF();
        } else {
            if (classFlags == CLASS_BYTE) {
                classId = ds.readUnsignedByte();
            } else if (classFlags == CLASS_SHORT) {
                classId = ds.readUnsignedShort();
            } else {
                throw new UnexpectedException("BUG: invalid classTypeFlag value: " + versionFlags);
            }
            if (classMapper == null) {
                classMapper = Serializer.getClassMapper(expectedClassType);
                if (classMapper == null) {
                    throw new InvalidSerializedDataException("Class id can't be mapped: " + classId);
                }
            }
            className = classMapper.get(classId);
            if (className == null) {
                throw new InvalidSerializedDataException("Class id can't be mapped: " + classId);
            }
        }
        if (classType == null) {
            try {
                classType = (Class<? extends Serializable>) Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new InvalidSerializedDataException(e);
            } catch (ClassCastException e) {
                throw new InvalidSerializedDataException(e);
            }
        }
    }

    public ObjectSerializationMetadata(Integer version, Class<? extends Serializable> classType, ClassMapper classMapper) {
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
            classMapper = Serializer.getClassMapper(classType);
        }
        if (classMapper != null) {
            classId = classMapper.get(classType.getName());
        }
    }

    public ObjectSerializationMetadata(Integer version, Class<? extends Serializable> classType) {
        this(version, classType, null);
    }

    public void write(SerializationStream ss) throws IOException {
        int versionFlags = getVersionFlags();
        int classFlags = getClassFlags();
        ss.write(versionFlags | (classFlags << 2));
        if (versionFlags == VERSION_BYTE) {
            ss.write(version);
        } else if (versionFlags == VERSION_SHORT) {
            ss.writeShort(version);
        } else if (versionFlags == VERSION_INT) {
            ss.writeInt(version);
        }
        if (classFlags != CLASS_UNKNOWN) {
            if (classFlags == CLASS_BYTE) {
                ss.write(classId);
            } else if (classFlags == CLASS_SHORT) {
                ss.writeShort(classId);
            } else if (classFlags == CLASS_DYNAMIC) {
                ss.writeUTF(classType.getName());
            } else {
                throw new UnexpectedException("BUG: invalid class flag " + classFlags);
            }
        }
    }

    public Integer getVersion() {
        return version;
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
            return CLASS_UNKNOWN;
        } else if (classId <= 255) {
            return CLASS_BYTE;
        } else if (classId <= 65535) {
            return CLASS_SHORT;
        } else {
            return CLASS_DYNAMIC;
        }
    }
}
