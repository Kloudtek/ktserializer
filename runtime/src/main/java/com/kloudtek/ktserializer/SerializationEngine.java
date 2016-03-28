/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import com.kloudtek.util.UnexpectedException;
import com.kloudtek.util.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Created by yannick on 12/09/2014.
 */
public class SerializationEngine {
    protected final HashMap<String, Object> map = new HashMap<String, Object>();
    protected ClassMapper classMapper;
    protected boolean unmappedClassesAllowed = true;
    protected int maxReadSize;
    private final HashSet<String> loadedCfgs = new HashSet<String>();
    private final HashMap<String, String> cfgLocations = new HashMap<String, String>();

    public SerializationEngine() {
        this(Serializer.systemClassMapper());
    }

    public SerializationEngine(ClassMapper classMapper) {
        this.classMapper = classMapper;
    }

    public <S extends Serializable> S deserialize(@NotNull S serializableObj, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readObject(serializableObj);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public <S extends Serializable> S deserializeSpecific(@NotNull S serializableObj, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readSpecificObject(serializableObj);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public <X extends Serializable> X deserialize(@NotNull Class<X> classType, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readObject(classType);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public <X extends Serializable> X deserializeSpecific(@NotNull Class<X> classType, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readSpecificObject(classType);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public Serializable deserialize(@NotNull byte[] serializedData) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readObject();
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public List<Serializable> deserializeList(@NotNull byte[] serializedData) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readObjectList();
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public <S extends Serializable> List<S> deserializeList(@NotNull Class<S> classType, @NotNull byte[] serializedData) throws InvalidSerializedDataException {
        try {
            DeserializationStream ds = new DeserializationStream(serializedData, this);
            return ds.readObjectList(classType);
        } catch (IOException e) {
            throw new InvalidSerializedDataException(e);
        }
    }

    public byte[] serialize(@NotNull Serializable object) {
        return serialize(object, false);
    }

    public byte[] serializeSpecific(@NotNull Serializable object) {
        return serialize(object, true);
    }

    private byte[] serialize(@NotNull Serializable object, boolean specific) {
        try {
            SerializationStream os = new SerializationStream(this);
            os.writeObject(object, classMapper, specific);
            return os.closeAndReturnData();
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    public byte[] serializeList(@NotNull Collection<? extends Serializable> collection) {
        try {
            SerializationStream os = new SerializationStream(this);
            os.writeObjectList(collection, classMapper);
            return os.closeAndReturnData();
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    public SerializationEngine setInject(String key, Object value) {
        map.put(key, value);
        return this;
    }

    public Object getInject(String key) {
        return getImpl(key);
    }

    public SerializationEngine setInject(Object value) {
        return setInject(value.getClass().getName(), value);
    }

    public SerializationEngine setInject(Class<?> classType, Object value) {
        map.put(classType.getName(), value);
        return this;
    }

    public <X> X getInject(Class<X> classType) {
        return classType.cast(getImpl(classType.getName()));
    }

    private Object getImpl(String key) {
        return map.get(key);
    }

    public ClassMapper getClassMapper() {
        return classMapper;
    }

    public boolean isUnmappedClassesAllowed() {
        return unmappedClassesAllowed;
    }

    public void setUnmappedClassesAllowed(boolean unmappedClassesAllowed) {
        this.unmappedClassesAllowed = unmappedClassesAllowed;
    }

    /**
     * Set the size limit on serialized payloads to read (defaults to 10K/10240 bytes)
     *
     * @return size limit
     */
    public int getMaxReadSize() {
        return maxReadSize;
    }

    public void setMaxReadSize(int maxReadSize) {
        this.maxReadSize = maxReadSize;
    }

    public synchronized boolean loadDefaultConfig() {
        try {
            String configClassName = System.getProperty("com.kloudtek.ktserializer.ISerializerConfig", "KTSerializerConfig");
            Class<?> configClass = Class.forName(configClassName);
            ISerializerConfig config = ISerializerConfig.class.cast(configClass.newInstance());
            load(config);
            return true;
        } catch (InstantiationException e) {
            return false;
        } catch (IllegalAccessException e) {
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void load(ISerializerConfig config) {
        setUnmappedClassesAllowed(config.isDynaClassesAllowed());
        for (Map.Entry<LibraryId, Class<? extends Library>> entry : config.getLibraries().entrySet()) {
            classMapper.registerLibrary(entry.getKey(), entry.getValue());
        }
    }

    public synchronized void loadConfig(String classpathLocation) {
        try {
            Enumeration<URL> resources = ClassMapper.class.getClassLoader().getResources(classpathLocation);
            if (!resources.hasMoreElements()) {
                throw new InvalidConfigException("Cannot find config file " + classpathLocation);
            } else {
                while (resources.hasMoreElements()) {
                    InputStream is = resources.nextElement().openStream();
                    try {
                        Properties p = new Properties();
                        p.load(is);
                        for (Map.Entry<Object, Object> entry : p.entrySet()) {
                            String key = entry.getKey().toString().toLowerCase();
                            String value = entry.getValue().toString();
                            if (key.startsWith("lib.")) {
                                key = key.substring(4);
                                LibraryId libraryId;
                                try {
                                    libraryId = new ShortLibraryId(Short.parseShort(key));
                                } catch (NumberFormatException e) {
                                    libraryId = new LongLibraryId(key);
                                }
                                if (!classMapper.isLibraryRegistered(libraryId)) {
                                    try {
                                        Class<?> clazz = Class.forName(value);
                                        if (Library.class.isAssignableFrom(clazz)) {
                                            Library library = clazz.asSubclass(Library.class).newInstance();
                                            classMapper.registerLibrary(libraryId, library.getClasses());
                                        } else {
                                            throw new InvalidConfigException("Invalid ktserializer class isn't a library: " + clazz.getName());
                                        }
                                    } catch (ClassNotFoundException e) {
                                        throw new InvalidConfigException("Unable to find class: " + value + ": " + e.getMessage(), e);
                                    } catch (InstantiationException e) {
                                        throw new InvalidConfigException("Unable to instantiate class: " + value + ": " + e.getMessage(), e);
                                    } catch (IllegalAccessException e) {
                                        throw new InvalidConfigException("Unable to instantiate class: " + value + ": " + e.getMessage(), e);
                                    }
                                }
                            } else if (key.equals("allowdynaclasses")) {
                                setUnmappedClassesAllowed(Boolean.parseBoolean(value));
                            }
                            String id = p.getProperty("id");
                            loadedCfgs.add(id);
                            cfgLocations.put(id, classpathLocation);
                        }
                    } finally {
                        IOUtils.close(is);
                    }
                }
            }
        } catch (IOException e) {
            throw new InvalidConfigException(e);
        } catch (NumberFormatException e) {
            throw new InvalidConfigException(e);
        } catch (ClassCastException e) {
            throw new InvalidConfigException(e);
        }
    }

    public void checkConfigLoaded(@Nullable Boolean dynaClassesAllowed, String... cfgIds) {
        if (cfgIds != null) {
            for (String id : cfgIds) {
                if (!loadedCfgs.contains(id)) {
                    throw new RuntimeException("KTSerialiser configuration file id " + id + " hasn't been loaded");
                }
            }
        }
        if (dynaClassesAllowed != null && !dynaClassesAllowed.equals(this.unmappedClassesAllowed)) {
            throw new RuntimeException("DynaClassesAllowed config mismatch (was expecting " + dynaClassesAllowed + ")");
        }
    }
}
