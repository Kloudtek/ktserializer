/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * A class mapper can be used when serializing an object which support serialization of sub-classes (or interface implementations).
 * This is a easy and simple way to specify mappings between an number (unsigned short) and class types. Naturally the
 * downside of this approach is it's lack of flexibility and extensibility. Also be careful not to change
 */
public class ClassMapper {
    static final Class<?>[] defaultClasses = new Class[]{SerializableList.class};
    private final ArrayList<ArrayList<String>> libraryClasses = new ArrayList<ArrayList<String>>();
    private final HashMap<ClassId, String> classIdToName = new HashMap<ClassId, String>();
    private final HashMap<String, ClassId> nameToClassId = new HashMap<String, ClassId>();
    private HashSet<Integer> libNb = new HashSet<Integer>();

    public ClassMapper() {
        registerLibrary(0, defaultClasses);
    }

    public ClassMapper(Class<?>... classes) {
        this();
        registerLibrary(1, classes);
    }

    public String get(int libraryId, int classId) {
        return classIdToName.get(new ClassId(libraryId, classId));
    }

    public ClassId get(String classType) {
        return nameToClassId.get(classType);
    }

    /**
     * Register class library.
     *
     * @param number  Library number starting at one. This is used to validate registration isn't done in wrong order.
     * @param classes Classes for that library
     */
    public synchronized void registerLibrary(int number, List<String> classes) {
        if (libNb.contains(number)) {
            throw new IllegalArgumentException("Serialization library registration number " + number
                    + " does not match " + libraryClasses.size() + 1);
        }
        ArrayList<String> list = new ArrayList<String>(classes.size());
        for (int i = 0; i < classes.size(); i++) {
            String className = classes.get(i);
            list.add(className);
            ClassId classId = new ClassId(number, i);
            classIdToName.put(classId, className);
            nameToClassId.put(className, classId);
        }
        libraryClasses.add(list);
        libNb.add(number);
    }

    /**
     * Register class library.
     *
     * @param number  Library number starting at one. This is used to validate registration isn't done in wrong order.
     * @param classes Classes for that library
     */
    public void registerLibrary(int number, Class<?>... classes) {
        ArrayList<String> classNames = new ArrayList<String>();
        for (Class<?> cl : classes) {
            classNames.add(cl.getName());
        }
        registerLibrary(number, classNames);
    }
}
