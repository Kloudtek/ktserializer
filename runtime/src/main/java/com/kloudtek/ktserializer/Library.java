/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import org.jetbrains.annotations.NotNull;

/**
 * Created by yannick on 22/08/15.
 */
public interface Library {
    @NotNull
    Class<?>[] getClasses();
}
