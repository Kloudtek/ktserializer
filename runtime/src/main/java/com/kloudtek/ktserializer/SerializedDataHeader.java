/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.io.DataOutputStream;

import java.io.IOException;

/**
 * Created by yannick on 03/01/2015.
 */
public class SerializedDataHeader {
    private int version;
    private ClassId classId;
    private Class<? extends Serializable> classType;
    private boolean forcedClassId;

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public SerializedDataHeader(DeserializationStream ds, ClassMapper classMapper) throws IOException, InvalidSerializedDataException {
        this(ds, classMapper, null);
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public SerializedDataHeader(DeserializationStream ds, ClassMapper classMapper, Class<? extends Serializable> forcedClass) throws IOException, InvalidSerializedDataException {
        if (classMapper == null) {
            classMapper = ds.getSerializer().getClassMapper();
        }
        String className;
        version = (int) ds.readUnsignedNumber();
        if (forcedClass == null) {
            int classId = (int) ds.readUnsignedNumber();
            if (classId == 0) {
                className = ds.readUTF();
                if (ds.getSerializer().isDisallowUnmappedClasses()) {
                    throw new InvalidSerializedDataException("Dynamic classes are not allowed: " + className);
                }
            } else {
                int libId = (int) ds.readUnsignedNumber();
                className = classMapper.get(libId, classId - 1);
                if (className == null) {
                    throw new InvalidSerializedDataException("Invalid class id: " + new ClassId(libId, classId));
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
            this.classType = forcedClass;
        }
    }

    public SerializedDataHeader(SerializationStream serializationStream, Integer version, Class<? extends Serializable> classType, ClassMapper classMapper, boolean forcedClassId) {
        this.forcedClassId = forcedClassId;
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
        if (serializationStream.getSerializer().isDisallowUnmappedClasses() && classId == null) {
            throw new IllegalArgumentException("Serialization of un-mapped classes is disallowed: " + classType.getName());
        }
    }

    public void write(DataOutputStream ss) throws IOException {
        ss.writeUnsignedNumber(version);
        if (!forcedClassId) {
            if (classId != null) {
                ss.writeUnsignedNumber(classId.getClassId() + 1);
                ss.writeUnsignedNumber(classId.getLibraryId());
            } else {
                ss.writeUnsignedNumber(0);
                ss.writeUTF(classType.getName());
            }
        }
    }

    public Integer getVersion() {
        return version;
    }

    public Class<? extends Serializable> getClassType() {
        return classType;
    }
}
