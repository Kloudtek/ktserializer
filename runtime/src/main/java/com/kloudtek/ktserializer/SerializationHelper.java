/*
 * Copyright (c) 2014 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;

/**
 * Created by yannick on 12/09/2014.
 */
public class SerializationHelper {
    public static <X> X deserialize( Class<X> classType, byte[] data ) throws InstantiationException, InvalidSerializedDataException {
        try {
            if( Serializable.class.isAssignableFrom(classType) ) {
                Serializable serializable = (Serializable) classType.newInstance();
                serializable.deserialize(data);
            } else {
                throw new IllegalArgumentException("Class cannot be deserialized: "+classType.getName());
            }
        } catch (IllegalAccessException e) {
            throw new UnexpectedException(e);
        }
        return null;
    }

    public static byte[] serialize( Object object ) {
        if( object instanceof Serializable ) {
            return ((Serializable) object).serialize();
        } else {
            throw new IllegalArgumentException("Class cannot be sserialized: "+object.getClass().getName());
        }
    }
}
