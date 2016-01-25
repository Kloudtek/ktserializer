/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Created by yannick on 16/01/2015.
 */
public class TestLibraryLongIdObj extends AbstractCustomSerializable {
    public long val = 8;

    @Override
    public void serialize(@NotNull SerializationStream os) throws IOException {
        os.writeUnsignedNumber(val);
    }

    @Override
    public void deserialize(@NotNull DeserializationStream is, int version) throws IOException, InvalidSerializedDataException {
        val = is.readUnsignedNumber();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestLibraryLongIdObj)) return false;

        TestLibraryLongIdObj that = (TestLibraryLongIdObj) o;

        return val == that.val;

    }

    @Override
    public int hashCode() {
        return (int) val;
    }
}
