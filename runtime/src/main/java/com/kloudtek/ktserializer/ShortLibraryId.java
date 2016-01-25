/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

/**
 * Created by yannick on 1/25/16.
 */
public class ShortLibraryId implements LibraryId {
    private short id;

    public ShortLibraryId(int id) {
        if (id < 0 || id > 255) {
            throw new IllegalArgumentException("Short library id must be between 0 and 255: " + id);
        }
        this.id = (short) id;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShortLibraryId)) return false;

        ShortLibraryId that = (ShortLibraryId) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) id;
    }

    @Override
    public String toString() {
        return Short.toString(id);
    }
}
