package io.github.pbelevich.newObjects;

import org.junit.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Pavel Belevich
 */
public class NewObjectsTest {

    class Class1 {

        double d;

    }

    class Class2 {

        long l;

    }

    static Unsafe unsafe;

    static {
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static long normalize(int value) {
        if(value >= 0) return value;
        return (~0L >>> 32) & value;
    }

    @Test
    public void testNewObjects() throws Exception {
        Object objects = unsafe.allocateInstance(Objects.class);
        assertEquals(Objects.class, objects.getClass());
    }

    static long getAddress(Object o) {
        Object[] array1 = new Object[] {o};
        long baseOffset1 = unsafe.arrayBaseOffset(Object[].class);
        return normalize(unsafe.getInt(array1, baseOffset1));
    }

    static Class2 getObject2(long address) {
        Class2[] array2 = new Class2[] {null};
        long baseOffset2 = unsafe.arrayBaseOffset(Object[].class);
        unsafe.putLong(array2, baseOffset2, address);
        return array2[0];
    }

    @Test
    public void testCasting() throws Exception {
        Class1 o1 = (Class1) unsafe.allocateInstance(Class1.class);

        long address = getAddress(o1);

        Class2 o2 = getObject2(address);

        assertTrue(((Object)o1) == ((Object)o2));

        o1.d = 42d;

        assertEquals(o2.l, Long.valueOf(Double.doubleToRawLongBits(o1.d)).longValue());
    }

}
