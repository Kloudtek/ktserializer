/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.StringUtils;
import com.kloudtek.util.UnexpectedException;
import com.kloudtek.util.io.ByteArrayDataOutputStream;
import com.kloudtek.util.io.DataInputStream;
import com.kloudtek.util.io.DataOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by yannick on 22/08/15.
 */
public class StreamDictionary {
    private final ArrayList<byte[]> dataList = new ArrayList<byte[]>();
    private final HashMap<byte[], Long> index = new HashMap<byte[], Long>();

    public byte[] serialize() {
        try {
            final ByteArrayDataOutputStream buf = new ByteArrayDataOutputStream();
            serialize(buf);
            buf.close();
            return buf.toByteArray();
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    public void serialize(DataOutputStream os) throws IOException {
        os.writeUnsignedNumber(dataList.size());
        for (byte[] data : dataList) {
            os.writeData(data);
        }
    }

    public void deserialize(DataInputStream is) throws IOException {
        final long len = is.readUnsignedNumber();
        new ArrayList<byte[]>((int) len);
        for (int i = 0; i < len; i++) {
            dataList.add(is.readData());
        }
    }

    public synchronized byte[] getData(long id) throws InvalidSerializedDataException {
        try {
            return dataList.get((int) id);
        } catch (IndexOutOfBoundsException e) {
            throw new InvalidSerializedDataException("Invalid dictionary id " + id);
        }
    }

    public synchronized long setData(byte[] data) {
        Long id = index.get(data);
        if (id == null) {
            dataList.add(data);
            id = (long) (dataList.size() - 1);
            index.put(data, id);
        }
        return id;
    }

    public String getString(long id) throws InvalidSerializedDataException {
        return StringUtils.utf8(getData(id));
    }

    public long setString(String data) {
        return setData(StringUtils.utf8(data));
    }
}
