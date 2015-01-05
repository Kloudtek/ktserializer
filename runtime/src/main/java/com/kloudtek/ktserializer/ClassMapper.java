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
public class ClassMapper {
    private final Class<? extends Serializable>[] classTypes;
    private HashMap<Class<? extends Serializable>,Integer> reverseMap;

    public ClassMapper(Class<? extends Serializable>... classTypes) {
        this.classTypes = classTypes;
        for (int i = 0; i < classTypes.length; i++) {
            reverseMap.put(classTypes[i],i);
        }
    }

    public Class<? extends Serializable> get(Integer classId) {
        if( classId < 0 || classId > classTypes.length ) {
            throw new IllegalArgumentException("Invalid class id: "+classId);
        }
        return classTypes[classId];
    }
}
