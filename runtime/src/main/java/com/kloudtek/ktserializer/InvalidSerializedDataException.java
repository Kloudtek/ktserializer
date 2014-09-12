/*
 * Copyright (c) 2014 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

/**
 * Created by yannick on 12/09/2014.
 */
public class InvalidSerializedDataException extends Exception {
    private static final long serialVersionUID = 834948226407023125L;

    public InvalidSerializedDataException() {
    }

    public InvalidSerializedDataException(String message) {
        super(message);
    }

    public InvalidSerializedDataException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidSerializedDataException(Throwable cause) {
        super(cause);
    }
}
