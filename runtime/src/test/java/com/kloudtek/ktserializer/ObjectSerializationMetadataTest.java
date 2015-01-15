/*
 * Copyright (c) 2015 Kloudtek Ltd
 */

package com.kloudtek.ktserializer;

import org.testng.annotations.Test;

import java.io.IOException;

import static org.testng.Assert.assertEquals;

public class ObjectSerializationMetadataTest {
    private TestClassMapper classMapper0 = new TestClassMapper(0, TestClass.class);

    @Test
    public void testMinIntVersion() throws IOException, InvalidSerializedDataException {
        test(65536, 7, TestClass.class, TestClass.class, classMapper0);
    }

    @Test
    public void testMaxIntVersion() throws IOException, InvalidSerializedDataException {
        test(Integer.MAX_VALUE, 7, TestClass.class, TestClass.class, classMapper0);
    }

    @Test
    public void testMaxShortVersion() throws IOException, InvalidSerializedDataException {
        test(65535, 5, TestClass.class, TestClass.class, classMapper0);
    }

    @Test
    public void testMinShortVersion() throws IOException, InvalidSerializedDataException {
        test(256, 5, TestClass.class, TestClass.class, classMapper0);
    }

    @Test
    public void testMaxByteVersion() throws IOException, InvalidSerializedDataException {
        test(255, 4, TestClass.class, TestClass.class, classMapper0);
    }

    @Test
    public void testMinByteVersion() throws IOException, InvalidSerializedDataException {
        test(1, 4, TestClass.class, TestClass.class, classMapper0);
    }

    @Test
    public void testNoVersion() throws IOException, InvalidSerializedDataException {
        test(0, 3, TestClass.class, TestClass.class, classMapper0);
    }

    @Test
    public void testUnmappedClass() throws IOException, InvalidSerializedDataException {
        test(0, 71, ITest.class, TestClass.class, null);
    }

    @Test
    public void testClassMappedMinByte() throws IOException, InvalidSerializedDataException {
        test(0, 3, ITest.class, TestClass.class, classMapper0);
    }

    @Test
    public void testClassMappedMaxByte() throws IOException, InvalidSerializedDataException {
        test(0, 3, ITest.class, TestClass.class, new TestClassMapper(255, TestClass.class));
    }

    @Test
    public void testClassMappedMinShort() throws IOException, InvalidSerializedDataException {
        test(0, 4, ITest.class, TestClass.class, new TestClassMapper(256, TestClass.class));
    }

    @Test
    public void testClassMappedMaxShort() throws IOException, InvalidSerializedDataException {
        test(0, 4, ITest.class, TestClass.class, new TestClassMapper(65535, TestClass.class));
    }

    @Test
    public void testClassMappedMinInt() throws IOException, InvalidSerializedDataException {
        test(0, 6, ITest.class, TestClass.class, new TestClassMapper(65536, TestClass.class));
    }

    @Test
    public void testIntVersionAndDynamicClass() throws IOException, InvalidSerializedDataException {
        test(Integer.MAX_VALUE, 75, ITest.class, TestClass.class, null);
    }

    @Test
    public void testIntVersionAndMaxShortClass() throws IOException, InvalidSerializedDataException {
        test(Integer.MAX_VALUE, 8, ITest.class, TestClass.class, new TestClassMapper(65535, TestClass.class));
    }

    private void test(int version, int size, Class<? extends Serializable> expected, Class<? extends Serializable> actual, ClassMapper classMapper) throws IOException, InvalidSerializedDataException {
        ObjectSerializationMetadata m1 = new ObjectSerializationMetadata(version, actual, classMapper);
        SerializationStream ss = new SerializationStream(new Serializer());
        m1.write(ss);
        byte[] data = ss.toByteArray();
        assertEquals(data.length, size);
        DeserializationStream ds = new DeserializationStream(data, new Serializer());
        ObjectSerializationMetadata m2 = new ObjectSerializationMetadata(ds, expected, classMapper);
        assertEquals(m2.getVersion().intValue(), version);
    }

    public class ITest implements Serializable {
    }

    public class TestClass extends ITest {
    }

    public class TestClassMapper implements ClassMapper {
        private int classId;
        private String className;

        public TestClassMapper(int classId, Class<?> classType) {
            this.classId = classId;
            this.className = classType.getName();
        }

        @Override
        public String get(int classId) {
            if (classId != this.classId) {
                throw new IllegalArgumentException("Not class id " + classId);
            }
            return className;
        }

        @Override
        public int get(String classType) {
            if (!classType.equals(className)) {
                throw new IllegalArgumentException(classType + " != " + className);
            }
            return classId;
        }
    }
}