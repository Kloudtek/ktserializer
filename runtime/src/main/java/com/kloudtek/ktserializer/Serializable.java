/*
 * Copyright (c) 2014 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.io.DataInputStream;
import com.kloudtek.util.io.DataOutputStream;

import java.io.IOException;

/**
 * Created by yannick on 12/09/2014.
 */
public interface Serializable {
    byte[] serialize();
    void serialize( DataOutputStream os ) throws IOException;
    void deserialize( byte[] data ) throws InvalidSerializedDataException;
    void deserialize( DataInputStream is ) throws IOException, InvalidSerializedDataException;
}
