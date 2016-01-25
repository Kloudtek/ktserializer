/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

/**
 * Created by yannick on 1/25/16.
 */
public class InvalidConfigException extends RuntimeException {
    public InvalidConfigException() {
    }

    public InvalidConfigException(String message) {
        super(message);
    }

    public InvalidConfigException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidConfigException(Throwable cause) {
        super(cause);
    }
}
