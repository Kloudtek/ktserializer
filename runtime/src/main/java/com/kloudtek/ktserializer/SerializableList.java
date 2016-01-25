/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by yannick on 17/01/2015.
 */
public class SerializableList extends ArrayList<Serializable> implements CustomSerializable {
    public SerializableList(int capacity) {
        super(capacity);
    }

    public SerializableList() {
    }

    public SerializableList(Collection<? extends Serializable> collection) {
        super(collection);
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public void serialize(@NotNull SerializationStream os) throws IOException {
        os.writeUnsignedNumber(size());
        for (Serializable x : this) {
            os.writeObject(x);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deserialize(@NotNull DeserializationStream is, int version) throws IOException, InvalidSerializedDataException {
        long total = is.readUnsignedNumber();
        for (int i = 0; i < total; i++) {
            add(is.readObject());
        }
    }
}
