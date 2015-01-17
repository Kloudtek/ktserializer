/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Created by yannick on 16/01/2015.
 */
public class CompositeTestObject extends SimpleTestObj {
    public SimpleTestObj o1 = new SimpleTestObj();
    public SimpleTestObj o2 = new SimpleTestObj();

    @Override
    public void serialize(@NotNull SerializationStream os) throws IOException {
        super.serialize(os);
        os.writeObject(o1);
        os.writeObject(o2);
    }

    @Override
    public void deserialize(@NotNull DeserializationStream is, int version) throws IOException, InvalidSerializedDataException {
        super.deserialize(is, version);
        o1 = is.readObject(SimpleTestObj.class);
        o2 = is.readObject(SimpleTestObj.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CompositeTestObject that = (CompositeTestObject) o;

        if (!o1.equals(that.o1)) return false;
        if (!o2.equals(that.o2)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + o1.hashCode();
        result = 31 * result + o2.hashCode();
        return result;
    }
}
