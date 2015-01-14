/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

/**
 * Created by yannick on 13/01/2015.
 */
public interface ClassMapper {
    String get(int classId);

    int get(String classType);
}
