package mx.kenzie.fern;

import sun.misc.Unsafe;
import sun.reflect.ReflectionFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.List;

class FernUnsafe {
    
    private static boolean valid;
    private static ReflectionFactory factory;
    private static Unsafe unsafe;
    
    static {
        try {
            valid = true;
            factory = ReflectionFactory.getReflectionFactory();
            unsafe = AccessController.doPrivileged((PrivilegedExceptionAction<Unsafe>) () -> {
                final Field field = Unsafe.class.getDeclaredField("theUnsafe");
                field.setAccessible(true);
                return (Unsafe) field.get(null);
            });
            // New Java security workaround.
            final Field field = Class.class.getDeclaredField("module");
            long offset = unsafe.objectFieldOffset(field);
            unsafe.putObject(FernUnsafe.class, offset, Object.class.getModule());
        } catch (Throwable ex) {
            valid = false;
        }
    }
    
    static boolean isValid() {
        return valid;
    }
    
    static <Type> Type createForSerialisation(final Class<Type> thing) {
        if (thing.isArray()) return (Type) Array.newInstance(thing, 0);
        try {
            return (Type) unsafe.allocateInstance(thing);
        } catch (InstantiationException e) {
            return null;
        }
    }
    
    static List<Field> getFields(final Class<?> type) {
        final List<Field> list = new ArrayList<>();
        if (type == null) return list;
        if (type.isInterface()) return list;
        Class<?> cls = type;
        do {
            for (final Field field : cls.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) continue;
                if (Modifier.isTransient(field.getModifiers())) continue;
                field.setAccessible(true);
                list.add(field);
            }
        } while ((cls = cls.getSuperclass()) != null && cls != Object.class);
        return list;
    }
    
    static void setValue(Object target, Field field, Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }
    }
    
    static Object getValue(Object target, Field field) {
        try {
            return field.get(target);
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
}
