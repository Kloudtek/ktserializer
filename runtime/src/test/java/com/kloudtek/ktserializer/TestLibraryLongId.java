/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import org.jetbrains.annotations.NotNull;

/**
 * Created by yannick on 1/25/16.
 */
public class TestLibraryLongId implements Library {
    @NotNull
    @Override
    public Class<?>[] getClasses() {
        return new Class<?>[]{TestLibraryLongIdObj.class};
    }
}
