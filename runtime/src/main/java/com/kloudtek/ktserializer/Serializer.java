/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.SystemUtils;
import com.kloudtek.util.UnexpectedException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

/**
 * Created by yannick on 1/25/16.
 */
public class Serializer {
    private static final Logger logger = Logger.getLogger(Serializer.class.getName());
    public static final String META_INF_KTSERIALIZER = "META-INF/ktserializer";
    private static ClassMapper systemClassMapper = new ClassMapper();
    private static final SerializationEngine globalInstance = new SerializationEngine(systemClassMapper);

    static {
        if (SystemUtils.isAndroid()) {
            // Let's be friendly to android strict mode
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                executor.submit(new Runnable() {
                    @Override
                    public void run() {
                        loadConfig();
                    }
                }).get();
            } catch (Exception e) {
                throw new UnexpectedException(e);
            } finally {
                executor.shutdown();
            }
        } else {
            loadConfig();
        }
    }

    private static void loadConfig() {
        try {
            Enumeration<URL> resources = ClassMapper.class.getClassLoader().getResources(META_INF_KTSERIALIZER);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                for (String file : listFiles(url)) {
                    loadConfig(file.trim());
                }
            }
        } catch (IOException e) {
            throw new InvalidConfigException(e);
        } catch (NumberFormatException e) {
            throw new InvalidConfigException(e);
        } catch (ClassNotFoundException e) {
            throw new InvalidConfigException(e);
        } catch (ClassCastException e) {
            throw new InvalidConfigException(e);
        } catch (InstantiationException e) {
            throw new InvalidConfigException(e);
        } catch (IllegalAccessException e) {
            throw new InvalidConfigException(e);
        }
    }

    private static String[] listFiles(@NotNull URL url) throws IOException {
        if (url.getProtocol().equals("file")) {
            try {
                return new File(url.toURI()).list();
            } catch (URISyntaxException e) {
                throw new UnexpectedException(e);
            }
        }

        if (url.getProtocol().equals("jar")) {
            String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
            Enumeration<JarEntry> entries = jar.entries();
            Set<String> result = new HashSet<String>();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (name.startsWith(META_INF_KTSERIALIZER)) {
                    String entry = name.substring(META_INF_KTSERIALIZER.length());
                    int checkSubdir = entry.indexOf("/");
                    if (checkSubdir >= 0) {
                        entry = entry.substring(0, checkSubdir);
                    }
                    result.add(entry);
                }
            }
            return result.toArray(new String[result.size()]);
        }

        throw new UnsupportedOperationException("Cannot list files for URL " + url);
    }

    private static void loadConfig(String path) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        globalInstance.classMapper.readLibraryConfig("META-INF/ktserializer/" + path);
    }

    public static <S extends Serializable> S deserialize(@NotNull S serializableObj, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        return globalInstance.deserialize(serializableObj, serializedData);
    }

    public static <S extends Serializable> List<S> deserializeList(@NotNull Class<S> classType, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        return globalInstance.deserializeList(classType, serializedData);
    }

    public static SerializationEngine setInject(Object value) {
        return globalInstance.setInject(value);
    }

    public static Object getInject(String key) {
        return globalInstance.getInject(key);
    }

    public static ClassMapper getClassMapper() {
        return globalInstance.getClassMapper();
    }

    public static byte[] serializeSpecific(@NotNull Serializable object) {
        return globalInstance.serializeSpecific(object);
    }

    public static int getMaxReadSize() {
        return globalInstance.getMaxReadSize();
    }

    public static void setMaxReadSize(int maxReadSize) {
        globalInstance.setMaxReadSize(maxReadSize);
    }

    public static byte[] serializeList(@NotNull Collection<? extends Serializable> collection) {
        return globalInstance.serializeList(collection);
    }

    public static SerializationEngine setInject(String key, Object value) {
        return globalInstance.setInject(key, value);
    }

    public static <X extends Serializable> X deserialize(@NotNull Class<X> classType, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        return globalInstance.deserialize(classType, serializedData);
    }

    public static SerializationEngine setInject(Class<?> classType, Object value) {
        return globalInstance.setInject(classType, value);
    }

    public static <X> X getInject(Class<X> classType) {
        return globalInstance.getInject(classType);
    }

    public static <S extends Serializable> S deserializeSpecific(@NotNull S serializableObj, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        return globalInstance.deserializeSpecific(serializableObj, serializedData);
    }

    public static <X extends Serializable> X deserializeSpecific(@NotNull Class<X> classType, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        return globalInstance.deserializeSpecific(classType, serializedData);
    }

    public static List<Serializable> deserializeList(@NotNull byte[] serializedData) throws InvalidSerializedDataException {
        return globalInstance.deserializeList(serializedData);
    }

    public static void setUnmappedClassesAllowed(boolean disallowUnmappedClasses) {
        globalInstance.setUnmappedClassesAllowed(disallowUnmappedClasses);
    }

    public static boolean isUnmappedClassesAllowed() {
        return globalInstance.isUnmappedClassesAllowed();
    }

    public static Serializable deserialize(@NotNull byte[] serializedData) throws InvalidSerializedDataException {
        return globalInstance.deserialize(serializedData);
    }

    public static byte[] serialize(@NotNull Serializable object) {
        return globalInstance.serialize(object);
    }

    public static void registerLibrary(LibraryId libraryId, Class<?>... classes) {
        globalInstance.classMapper.registerLibrary(libraryId, classes);
    }

    public static void registerLibrary(LibraryId libraryId, String... classes) {
        globalInstance.classMapper.registerLibrary(libraryId, classes);
    }

    public static void registerLibrary(LibraryId libraryId, List<String> classes) {
        globalInstance.classMapper.registerLibrary(libraryId, classes);
    }

    public static void readLibraryConfig(String classpathResourcePath) throws IOException {
        globalInstance.classMapper.readLibraryConfig(classpathResourcePath);
    }

    public static SerializationEngine engine() {
        return globalInstance;
    }

    public static ClassMapper systemClassMapper() {
        return systemClassMapper;
    }
}
