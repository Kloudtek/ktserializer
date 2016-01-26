/*
 * Copyright (c) 2016 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SerializerTest {
    private SerializationEngine serializer;

    @BeforeMethod
    public void init() {
        serializer = new SerializationEngine();
    }

    @Test
    public void simpleCustomSerializationDynaClass() throws InvalidSerializedDataException {
        serializer.setUnmappedClassesAllowed(true);
        SimpleTestObj o1 = new SimpleTestObj();
        byte[] serialized = serializer.serialize(o1);
        Serializable o2 = serializer.deserialize(serialized);
        assertEquals(o2, o1);
    }

    @Test
    public void simpleCustomSerializationSpecific() throws InvalidSerializedDataException {
        SimpleTestObj o1 = new SimpleTestObj();
        byte[] serialized = serializer.serializeSpecific(o1);
        Serializable o2 = serializer.deserializeSpecific(SimpleTestObj.class, serialized);
        assertEquals(o2, o1);
    }

    @Test
    public void testAutoLib() throws Exception {
        simpleLibTest(new TestLibraryAutoObj());
    }

    @Test
    public void testShortIdLib() throws Exception {
        serializer.getClassMapper().readLibraryConfig("shortidlib.properties");
        simpleLibTest(new TestLibraryShortIdObj());
    }

    @Test
    public void testLongIdLib() throws Exception {
        serializer.getClassMapper().readLibraryConfig("longidlib.properties");
        simpleLibTest(new TestLibraryLongIdObj());
    }

    private void simpleLibTest(Serializable obj) throws IOException, InvalidSerializedDataException {
        byte[] serialized = serializer.serialize(obj);
        Serializable dobj = serializer.deserialize(serialized);
        assertEquals(dobj, obj);
    }

    @Test(expectedExceptions = InvalidConfigException.class, expectedExceptionsMessageRegExp = "Duplicate class registration.*")
    public void conflictingLibraryConfigs() throws Exception {
        Serializer.readLibraryConfig("shortidlib.properties");
        Serializer.readLibraryConfig("shortidconflict.properties");
    }

    @Test
    public void simpleCustomSerializationRegClass() throws InvalidSerializedDataException {
        SimpleTestObj o1 = new SimpleTestObj();
        ClassMapper classMapper = new ClassMapper();
        classMapper.registerLibrary(new ShortLibraryId(1), SimpleTestObj.class);
        SerializationEngine serializer = new SerializationEngine(classMapper);
        byte[] serialized = serializer.serialize(o1);
        Serializable o2 = serializer.deserialize(serialized);
        assertEquals(o2, o1);
    }

    @Test
    public void simpleCustomCompositeCustomSerialization() throws InvalidSerializedDataException {
        serializer.setUnmappedClassesAllowed(true);
        CompositeTestObject o1 = new CompositeTestObject();
        byte[] serialized = serializer.serialize(o1);
        Serializable o2 = serializer.deserialize(serialized);
        assertEquals(o2, o1);
    }

    @Test
    public void simpleCustomMultiLvlCompositeCustomSerialization() throws InvalidSerializedDataException {
        serializer.setUnmappedClassesAllowed(true);
        MultiLvlCompositeTestObject o1 = new MultiLvlCompositeTestObject();
        byte[] serialized = serializer.serialize(o1);
        Serializable o2 = serializer.deserialize(serialized);
        assertEquals(o2, o1);
    }

    @Test
    public void simpleLargeCustomSerialization() throws InvalidSerializedDataException {
        serializer.setUnmappedClassesAllowed(true);
        LargeTestObj o1 = new LargeTestObj((byte) 5);
        byte[] serialized = serializer.serialize(o1);
        assertTrue(serialized.length > 5000);
        Serializable o2 = serializer.deserialize(serialized);
        assertEquals(o2, o1);
    }

//    @Test
//    public void simpleLargeCompressedCustomSerialization() throws InvalidSerializedDataException {
//        LargeTestObj o1 = new LargeTestObj((byte) 5);
//        byte[] serialized = serializer.serialize(o1, null);
//        assertTrue(serialized.length < 500);
//        Serializable o2 = serializer.deserialize(serialized);
//        assertEquals(o2, o1);
//    }

    @Test
    public void emptyList() throws InvalidSerializedDataException {
        ArrayList<Serializable> o1 = new ArrayList<Serializable>();
        byte[] data = serializer.serializeList(o1);
        assertEquals(data.length, 4);
        List<Serializable> o2 = serializer.deserializeList(data);
        assertEquals(o2, o1);
    }

    @Test
    public void simpleList() throws InvalidSerializedDataException {
        serializer.setUnmappedClassesAllowed(true);
        ArrayList<SimpleTestObj> l1 = new ArrayList<SimpleTestObj>();
        l1.add(new SimpleTestObj());
        byte[] data = serializer.serializeList(l1);
        List<SimpleTestObj> l2 = serializer.deserializeList(SimpleTestObj.class, data);
        assertEquals(l2, l1);
    }

    @Test
    public void complexList() throws InvalidSerializedDataException {
        serializer.setUnmappedClassesAllowed(true);
        ArrayList<Serializable> o1 = new ArrayList<Serializable>();
        o1.add(new SimpleTestObj());
        o1.add(new CompositeTestObject());
        o1.add(new MultiLvlCompositeTestObject());
        o1.add(new LargeTestObj());
        o1.add(new LargeTestObj());
        byte[] data = serializer.serializeList(o1);
        assertTrue(data.length > 10000);
        List<Serializable> o2 = serializer.deserializeList(Serializable.class, data);
        assertEquals(o2, o1);
    }

//    @Test
//    public void compressedComplexList() throws InvalidSerializedDataException {
//        ArrayList<Serializable> o1 = new ArrayList<Serializable>();
//        o1.add(new SimpleTestObj());
//        o1.add(new CompositeTestObject());
//        o1.add(new MultiLvlCompositeTestObject());
//        o1.add(new LargeTestObj());
//        byte[] data = serializer.serialize(o1);
//        assertTrue(data.length < 750);
//        List<SimpleTestObj> o2 = serializer.deserializeList(SimpleTestObj.class, data, null);
//        assertEquals(o2, o1);
//    }
}
