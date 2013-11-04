package test;

import java.lang.reflect.Method;
import java.util.Arrays;

import ua.com.fielden.platform.entity.annotation.Observable;

public class Test {

    /**
     * @param args
     */
    public static void main(final String[] args) {
	System.out.println("========== B.class.getDeclaredMethods() ================");
	for (final Method m : Arrays.asList(B.class.getDeclaredMethods())) {
	    System.out.println("" + m + " == " + m.getAnnotation(Observable.class) + " is bridge: " + m.isBridge());
	}

	System.out.println("========== A.class.getDeclaredMethods() ================");
	for (final Method m : Arrays.asList(A.class.getDeclaredMethods())) {
	    System.out.println("" + m + " == " + m.getAnnotation(Observable.class) + " is bridge: " + m.isBridge());
	}
    }

}
