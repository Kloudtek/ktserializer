/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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

    public void readLibraryConfig(String classpathResourcePath) throws IOException {
        Enumeration<URL> resources = ClassMapper.class.getClassLoader().getResources(classpathResourcePath);
        if (!resources.hasMoreElements()) {
            throw new IOException("library config not found: " + classpathResourcePath);
        }
        while (resources.hasMoreElements()) {
            InputStream is = resources.nextElement().openStream();
            try {
                Properties p = new Properties();
                p.load(is);
                for (Map.Entry<Object, Object> entry : p.entrySet()) {
                    String libraryIdStr = entry.getKey().toString();
                    LibraryId libraryId;
                    try {
                        libraryId = new ShortLibraryId(Short.parseShort(libraryIdStr));
                    } catch (NumberFormatException e) {
                        libraryId = new LongLibraryId(libraryIdStr);
                    }
                    String className = entry.getValue().toString();
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (Library.class.isAssignableFrom(clazz)) {
                            Library library = clazz.asSubclass(Library.class).newInstance();
                            registerLibrary(libraryId, library.getClasses());
                        } else {
                            throw new InvalidConfigException("Invalid ktserializer class isn't a library: " + clazz.getName());
                        }
                    } catch (ClassNotFoundException e) {
                        throw new InvalidConfigException("Unable to find class: " + className + ": " + e.getMessage(), e);
                    } catch (InstantiationException e) {
                        throw new InvalidConfigException("Unable to instantiate class: " + className + ": " + e.getMessage(), e);
                    } catch (IllegalAccessException e) {
                        throw new InvalidConfigException("Unable to instantiate class: " + className + ": " + e.getMessage(), e);
                    }
                }
            } finally {
                IOUtils.close(is);
            }
        }
    }
}
