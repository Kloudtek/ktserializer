/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by yannick on 16/01/2015.
 */
public class LargeTestObj extends AbstractCustomSerializable {
    public byte[] val = new byte[5000];

    public LargeTestObj() {
    }

    public LargeTestObj(byte v) {
        Arrays.fill(val, v);
    }

    @Override
    public void serialize(@NotNull SerializationStream os) throws IOException {
        os.writeData(val);
    }

    @Override
    public void deserialize(@NotNull DeserializationStream is, int version) throws IOException, InvalidSerializedDataException {
        val = is.readData();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LargeTestObj that = (LargeTestObj) o;

        if (!Arrays.equals(val, that.val)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(val);
    }
}
