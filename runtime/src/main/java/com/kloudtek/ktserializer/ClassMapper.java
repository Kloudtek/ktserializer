/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import org.jetbrains.annotations.NotNull;

import java.util.*;

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
    private final HashSet<LibraryId> libraries = new HashSet<LibraryId>();

    public ClassMapper() {
        registerLibraryInternal(new ShortLibraryId((short) 0), toStrings(defaultClasses));
    }

    public ClassMapper(Class<?>... classes) {
        this();
        registerLibrary(new ShortLibraryId((short) 1), toStrings(classes));
    }

    public String get(LibraryId libraryId, int classId) {
        return classIdToName.get(new ClassId(libraryId, classId));
    }

    public ClassId get(String classType) {
        return nameToClassId.get(classType);
    }

    public boolean isLibraryRegistered(LibraryId libraryId) {
        return libraries.contains(libraryId);
    }

    public void registerLibrary(LibraryId libraryId, Class<?>... classes) {
        registerLibraryInternal(libraryId, toStrings(Arrays.asList(classes)));
    }

    public void registerLibrary(LibraryId libraryId, String... classes) {
        registerLibraryInternal(libraryId, Arrays.asList(classes));
    }

    /**
     * Register class library.
     *
     * @param libraryId Library id
     * @param classes   Classes for that library
     */
    public void registerLibrary(LibraryId libraryId, List<String> classes) {
        if (libraryId instanceof ShortLibraryId && ((ShortLibraryId) libraryId).getId() == 0) {
            throw new IllegalArgumentException("Short library id cannot be zero");
        }
        registerLibraryInternal(libraryId, classes);
    }

    private synchronized void registerLibraryInternal(LibraryId libraryId, List<String> classes) {
        if (libraries.contains(libraryId)) {
            throw new IllegalArgumentException("Library already registered: " + libraryId);
        }
        ArrayList<String> list = new ArrayList<String>(classes.size());
        for (int i = 0; i < classes.size(); i++) {
            String className = classes.get(i);
            ClassId classId = new ClassId(libraryId, i);
            ClassId existing = nameToClassId.get(className);
            if (existing != null && !existing.equals(classId)) {
                throw new InvalidConfigException("Duplicate class registration: class name=" + className + " : new class id " + classId + " : existing class id " + existing);
            }
            list.add(className);
            classIdToName.put(classId, className);
            nameToClassId.put(className, classId);
        }
        libraryClasses.add(list);
        libraries.add(libraryId);
    }

    @NotNull
    private ArrayList<String> toStrings(@NotNull Class<?>[] classes) {
        return toStrings(Arrays.asList(classes));
    }

    @NotNull
    private ArrayList<String> toStrings(@NotNull List<Class<?>> classes) {
        ArrayList<String> list = new ArrayList<String>();
        for (Class cl : classes) {
            list.add(cl.getName());
        }
        return list;
    }
}
