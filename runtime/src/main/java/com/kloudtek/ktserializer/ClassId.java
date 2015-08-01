/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

/**
 * Created by yannick on 01/08/15.
 */
public class ClassId implements java.io.Serializable {
    private static final long serialVersionUID = -7378821996187572142L;
    private int libraryId;
    private int classId;

    public ClassId(int libraryId, int classId) {
        this.libraryId = libraryId;
        this.classId = classId;
    }

    public int getLibraryId() {
        return libraryId;
    }

    public int getClassId() {
        return classId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassId)) return false;

        ClassId classId1 = (ClassId) o;

        return libraryId == classId1.libraryId && classId == classId1.classId;
    }

    @Override
    public int hashCode() {
        int result = libraryId;
        result = 31 * result + classId;
        return result;
    }

    @Override
    public String toString() {
        return "ClassId{" +
                "libraryId=" + libraryId +
                ", classId=" + classId +
                '}';
    }
}
