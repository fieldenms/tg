package ua.com.fielden.platform.entity.ioc;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matcher;
import org.aopalliance.intercept.MethodInterceptor;

import java.lang.reflect.Method;

/**
 * This is a base class that ensures the most common IoC behaviour, such as avoiding interception of synthetic methods.
 * It should be extended by all platform IoC modules.
 */
public class AbstractPlatformIocModule extends AbstractModule {

    @Override
    protected void bindInterceptor(final Matcher<? super Class<?>> classMatcher, final Matcher<? super Method> methodMatcher, final MethodInterceptor... interceptors) {
        super.bindInterceptor(classMatcher, method -> !method.isSynthetic() && methodMatcher.matches(method), interceptors);
    }

}
