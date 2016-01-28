/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import java.util.Map;

/**
 * Created by yannick on 1/28/16.
 */
public interface ISerializerConfig {
    String getId();

    boolean isDynaClassesAllowed();

    Map<LibraryId, Class<? extends Library>> getLibraries();
}
