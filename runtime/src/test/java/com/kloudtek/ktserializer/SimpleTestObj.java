/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by yannick on 16/01/2015.
 */
public class SimpleTestObj extends AbstractCustomSerializable {
    public String val = UUID.randomUUID().toString();

    @Override
    public void serialize(@NotNull SerializationStream os) throws IOException {
        os.writeUTF(val);
    }

    @Override
    public void deserialize(@NotNull DeserializationStream is, int version) throws IOException, InvalidSerializedDataException {
        val = is.readUTF();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleTestObj that = (SimpleTestObj) o;

        if (!val.equals(that.val)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return val.hashCode();
    }
}
