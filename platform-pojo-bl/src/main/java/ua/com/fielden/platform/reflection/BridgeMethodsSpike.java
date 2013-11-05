package ua.com.fielden.platform.reflection;

import java.lang.reflect.Method;
import java.util.Arrays;

import ua.com.fielden.platform.entity.annotation.Observable;

public class BridgeMethodsSpike {

    public class A<T extends Comparable<?>> {
	@Observable
	public void setKey(final T test) {

	}
    }

    public class B extends A<String> {

	@Override
	@Observable
	public void setKey(final String test) {
	}
    }

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
