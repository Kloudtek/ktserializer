/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Created by yannick on 12/09/2014.
 */
public interface CustomSerializable extends Serializable {
    int getSerializationVersion();
    void serialize(@NotNull SerializationStream os) throws IOException;
    void deserialize(@NotNull DeserializationStream is, int version) throws IOException, InvalidSerializedDataException;
}
