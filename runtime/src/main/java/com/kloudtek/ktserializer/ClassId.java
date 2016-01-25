/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import org.jetbrains.annotations.NotNull;

/**
 * Created by yannick on 01/08/15.
 */
public class ClassId implements java.io.Serializable {
    private static final long serialVersionUID = -7378821996187572142L;
    private LibraryId libraryId;
    private int classId;

    public ClassId(@NotNull LibraryId libraryId, int classId) {
        this.libraryId = libraryId;
        this.classId = classId;
    }

    @NotNull
    public LibraryId getLibraryId() {
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

        if (classId != classId1.classId) return false;
        return libraryId.equals(classId1.libraryId);

    }

    @Override
    public int hashCode() {
        int result = libraryId.hashCode();
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
