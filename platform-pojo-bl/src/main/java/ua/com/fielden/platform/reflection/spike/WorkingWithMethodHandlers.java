package ua.com.fielden.platform.reflection.spike;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class WorkingWithMethodHandlers {

    public static void main(String[] args) throws Throwable {
        // toString()
        MethodType mtToString = MethodType.methodType(String.class);

        MethodHandle mhToString = getMethodHandle(WorkingWithMethodHandlers.class, "toString", mtToString);
        System.out.println(mhToString.invoke(new WorkingWithMethodHandlers()));
        
        
        // A setter method
        MethodType mtSetter = MethodType.methodType(void.class, Object.class);

        // compare() from Comparator<String>
        MethodType mtStringComparator = MethodType.methodType(int.class, String.class, String.class);
    }

    public static MethodHandle getMethodHandle(final Class<?> methodOwnerType, final String methodName, final MethodType mt) throws Throwable {
        MethodHandles.Lookup lk = MethodHandles.lookup();
        try {
            return lk.findVirtual(methodOwnerType, methodName, mt);
        } catch (final NoSuchMethodException | IllegalAccessException mhx) {
            throw new AssertionError().initCause(mhx);
        }
    }

    @Override
    public String toString() {
        return "Experimental class";
    }

}
