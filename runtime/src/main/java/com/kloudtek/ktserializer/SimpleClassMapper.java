/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import java.util.HashMap;

/**
 * A class mapper can be used when serializing an object which support serialization of sub-classes (or interface implementations).
 * This is a easy and simple way to specify mappings between an number (unsigned short) and class types. Naturally the
 * downside of this approach is it's lack of flexibility and extensibility. Also be careful not to change
 */
public class SimpleClassMapper implements ClassMapper {
    private final String[] classNames;
    private HashMap<String, Integer> reverseMap = new HashMap<String, Integer>();

    public SimpleClassMapper(Class<?>... classes) {
        int len = classes.length;
        classNames = new String[len];
        for (int i = 0; i < len; i++) {
            Class<?> cl = classes[i];
            if (cl != null) {
                String name = cl.getName();
                classNames[i] = name;
                reverseMap.put(name, i);
            }
        }
    }

    public SimpleClassMapper(String... classNames) {
        this.classNames = classNames;
        int len = classNames.length;
        for (int i = 0; i < len; i++) {
            if (classNames[i] != null) {
                reverseMap.put(classNames[i], i);
            }
        }
    }

    @Override
    public String get(int classId) {
        if (classId < 0 || classId > classNames.length) {
            return null;
        }
        return classNames[classId];
    }

    @Override
    public Integer get(String classType) {
        return reverseMap.get(classType);
    }
}
