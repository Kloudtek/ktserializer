/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>A class map list has a list of class names (but no class ids). All specified class map lists are merged into a master
 * index, and class ids assigned.</p>
 * <p>It is of the upmost importance that when class mapping is using to remember that any changes to the class maps can result
 * in breaking backwards compatibility. When a class needs to be removed just leave the array location as null, to avoid
 * changes to the class ids</p>
 */
public class ClassMapList {
    private List<String> classNames;

    public ClassMapList() {
    }

    public ClassMapList(String... classNames) {
        this.classNames = Arrays.asList(classNames);
    }

    public ClassMapList(Class... classTypes) {
        classNames = new ArrayList<String>(classTypes.length);
        int len = classTypes.length;
        for (int i = 0; i < len; i++) {
            Class[] ct = classTypes;
            if (ct != null) {
                classNames.add(ct[i].getName());
            } else {
                classNames.add(null);
            }
        }
    }

    public ClassMapList(int emptySize) {
        this.classNames = new ArrayList<String>(emptySize);
        for (int i = 0; i < emptySize; i++) {
            classNames.add(null);
        }
    }

    public List<String> getClassNames() {
        return classNames;
    }
}
