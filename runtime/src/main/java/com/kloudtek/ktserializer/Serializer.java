/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.SystemUtils;
import com.kloudtek.util.UnexpectedException;
import com.kloudtek.util.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by yannick on 1/25/16.
 */
public class Serializer {
    private static final Logger logger = Logger.getLogger(Serializer.class.getName());
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
            Enumeration<URL> resources = ClassMapper.class.getClassLoader().getResources("META-INF/ktserializer");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                logger.info("Loading config file: " + url);
                InputStream is = url.openStream();
                try {
                    String filesStr = IOUtils.toString(is).trim();
                    String[] files = filesStr.split("\n");
                    for (String file : files) {
                        loadConfig(file.trim());
                    }
                } finally {
                    IOUtils.close(is);
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
