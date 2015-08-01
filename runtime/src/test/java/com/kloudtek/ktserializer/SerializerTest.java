/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class SerializerTest {
    private Serializer serializer = new Serializer();

    @Test
    public void simpleCustomSerializationDynaClass() throws InvalidSerializedDataException {
        SimpleTestObj o1 = new SimpleTestObj();
        Serializer serializer = this.serializer;
        byte[] serialized = serializer.serialize(o1);
        Serializable o2 = serializer.deserialize(serialized);
        assertEquals(o2, o1);
    }

    @Test
    public void simpleCustomSerializationRegClass() throws InvalidSerializedDataException {
        SimpleTestObj o1 = new SimpleTestObj();
        SimpleClassMapper classMapper = new SimpleClassMapper();
        classMapper.registerLibrary(1, Collections.singletonList(SimpleTestObj.class.getName()));
        Serializer serializer = new Serializer(classMapper);
        byte[] serialized = serializer.serialize(o1);
        Serializable o2 = serializer.deserialize(serialized);
        assertEquals(o2, o1);
    }

    @Test
    public void simpleCustomCompositeCustomSerialization() throws InvalidSerializedDataException {
        CompositeTestObject o1 = new CompositeTestObject();
        byte[] serialized = serializer.serialize(o1);
        Serializable o2 = serializer.deserialize(serialized);
        assertEquals(o2, o1);
    }

    @Test
    public void simpleCustomMultiLvlCompositeCustomSerialization() throws InvalidSerializedDataException {
        MultiLvlCompositeTestObject o1 = new MultiLvlCompositeTestObject();
        byte[] serialized = serializer.serialize(o1);
        Serializable o2 = serializer.deserialize(serialized);
        assertEquals(o2, o1);
    }

    @Test
    public void simpleLargeCustomSerialization() throws InvalidSerializedDataException {
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
        byte[] data = serializer.serialize(o1);
        assertEquals(data.length, 3);
        List<Serializable> o2 = serializer.deserializeList(data);
        assertEquals(o2, o1);
    }

    @Test
    public void simpleList() throws InvalidSerializedDataException {
        ArrayList<SimpleTestObj> l1 = new ArrayList<SimpleTestObj>();
        l1.add(new SimpleTestObj());
        byte[] data = serializer.serialize(l1);
        List<SimpleTestObj> l2 = serializer.deserializeList(SimpleTestObj.class, data, null);
        assertEquals(l2, l1);
    }

    @Test
    public void complexList() throws InvalidSerializedDataException {
        ArrayList<Serializable> o1 = new ArrayList<Serializable>();
        o1.add(new SimpleTestObj());
        o1.add(new CompositeTestObject());
        o1.add(new MultiLvlCompositeTestObject());
        o1.add(new LargeTestObj());
        o1.add(new LargeTestObj());
        byte[] data = serializer.serialize(o1);
        assertTrue(data.length > 10000);
        List<SimpleTestObj> o2 = serializer.deserializeList(SimpleTestObj.class, data, null);
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
