package ua.com.fielden.platform.ioc;

import com.google.inject.matcher.Matcher;

import java.lang.reflect.Method;

public final class Matchers {

    public static Matcher<Method> notSyntheticMethod() {
        return method -> !method.isSynthetic();
    }

    private Matchers() {}

}
