package ua.com.fielden.platform.web_api;

import static graphql.Assert.assertShouldNeverHappen;
import static graphql.Scalars.GraphQLBoolean;
import static graphql.schema.GraphQLTypeUtil.isNonNull;
import static graphql.schema.GraphQLTypeUtil.unwrapOne;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.GraphQLException;
import graphql.Internal;
import graphql.schema.GraphQLType;

/**
 * A re-usable class that can fetch from POJOs
 */
@Internal
public class TgPropertyFetchingImpl {
    private static final Logger log = LoggerFactory.getLogger(TgPropertyFetchingImpl.class);

    private final AtomicBoolean USE_SET_ACCESSIBLE = new AtomicBoolean(true);
    private final AtomicBoolean USE_LAMBDA_FACTORY = new AtomicBoolean(true);
    private final AtomicBoolean USE_NEGATIVE_CACHE = new AtomicBoolean(true);
    private final ConcurrentMap<CacheKey, CachedLambdaFunction> LAMBDA_CACHE = new ConcurrentHashMap<>();
    private final ConcurrentMap<CacheKey, CachedMethod> METHOD_CACHE = new ConcurrentHashMap<>();
    private final ConcurrentMap<CacheKey, Field> FIELD_CACHE = new ConcurrentHashMap<>();
    private final ConcurrentMap<CacheKey, CacheKey> NEGATIVE_CACHE = new ConcurrentHashMap<>();
    private final Class<?> singleArgumentType;

    public TgPropertyFetchingImpl(final Class<?> singleArgumentType) {
        this.singleArgumentType = singleArgumentType;
    }

    private final class CachedMethod {
        private final Method method;
        private final boolean takesSingleArgumentTypeAsOnlyArgument;

        CachedMethod(final Method method) {
            this.method = method;
            this.takesSingleArgumentTypeAsOnlyArgument = takesSingleArgumentTypeAsOnlyArgument(method);
        }
    }

    private static final class CachedLambdaFunction {
        private final Function<Object, Object> getter;

        CachedLambdaFunction(final Function<Object, Object> getter) {
            this.getter = getter;
        }
    }

    public Object getPropertyValue(final String propertyName, final Object object, final GraphQLType graphQLType, final boolean dfeInUse, final Supplier<Object> singleArgumentValue) {
        if (object instanceof Map) {
            return ((Map<?, ?>) object).get(propertyName);
        }

        final CacheKey cacheKey = mkCacheKey(object, propertyName);

        // let's try positive cache mechanisms first.  If we have seen the method or field before
        // then we invoke it directly without burning any cycles doing reflection.
        CachedLambdaFunction cachedFunction = LAMBDA_CACHE.get(cacheKey);
        if (cachedFunction != null) {
            return cachedFunction.getter.apply(object);
        }
        final CachedMethod cachedMethod = METHOD_CACHE.get(cacheKey);
        if (cachedMethod != null) {
            try {
                return invokeMethod(object, singleArgumentValue, cachedMethod.method, cachedMethod.takesSingleArgumentTypeAsOnlyArgument);
            } catch (final NoSuchMethodException ignored) {
                assertShouldNeverHappen("A method cached as '%s' is no longer available??", cacheKey);
            }
        }
        final Field cachedField = FIELD_CACHE.get(cacheKey);
        if (cachedField != null) {
            return invokeField(object, cachedField);
        }

        //
        // if we have tried all strategies before, and they have all failed then we negatively cache
        // the cacheKey and assume that it's never going to turn up.  This shortcuts the property lookup
        // in systems where there was a `foo` graphql property, but they never provided an POJO
        // version of `foo`.
        //
        // we do this second because we believe in the positive cached version will mostly prevail
        // but if we then look it up and negatively cache it then lest do that look up next
        //
        if (isNegativelyCached(cacheKey)) {
            return null;
        }

        //
        // ok we haven't cached it, and we haven't negatively cached it, so we have to find the POJO method which is the most
        // expensive operation here
        //

        final Optional<Function<Object, Object>> getterOpt = lambdaGetter(propertyName, object);
        if (getterOpt.isPresent()) {
            try {
                final Function<Object, Object> getter = getterOpt.get();
                final Object value = getter.apply(object);
                cachedFunction = new CachedLambdaFunction(getter);
                LAMBDA_CACHE.putIfAbsent(cacheKey, cachedFunction);
                return value;
            } catch (LinkageError | ClassCastException ignored) {
                //
                // if we get a linkage error then it maybe that class loader challenges
                // are preventing the Meta Lambda from working.  So let's continue with
                // old skool reflection and if it's all broken there then it will eventually
                // end up negatively cached
                log.debug("Unable to invoke fast Meta Lambda for `{}` - Falling back to reflection", object.getClass().getName(), ignored);

            }
        }

        //
        // try by public getters name -  object.getPropertyName()
        try {
            final MethodFinder methodFinder = (rootClass, methodName) -> findPubliclyAccessibleMethod(cacheKey, rootClass, methodName, dfeInUse, false);
            return getPropertyViaGetterMethod(object, propertyName, graphQLType, methodFinder, singleArgumentValue);
        } catch (final NoSuchMethodException ignored) {
        }
        //
        // try by public getters name -  object.getPropertyName() where its static
        try {
            // we allow static getXXX() methods because we always have.  It's strange in retrospect but
            // in order to not break things we allow statics to be used.  In theory this double code check is not needed
            // because you CANT have a `static getFoo()` and a `getFoo()` in the same class hierarchy but to make the code read clearer
            // I have repeated the lookup.  Since we cache methods, this happens only once and does not slow us down
            final MethodFinder methodFinder = (rootClass, methodName) -> findPubliclyAccessibleMethod(cacheKey, rootClass, methodName, dfeInUse, true);
            return getPropertyViaGetterMethod(object, propertyName, graphQLType, methodFinder, singleArgumentValue);
        } catch (final NoSuchMethodException ignored) {
        }
        //
        // try by accessible getters name -  object.getPropertyName()
        try {
            final MethodFinder methodFinder = (aClass, methodName) -> findViaSetAccessible(cacheKey, aClass, methodName, dfeInUse);
            return getPropertyViaGetterMethod(object, propertyName, graphQLType, methodFinder, singleArgumentValue);
        } catch (final NoSuchMethodException ignored) {
        }
        //
        // try by record like name - object.propertyName() /* TG change, this lookup was made first before 'is/get' pojo-like getters */
        try {
            final MethodFinder methodFinder = (rootClass, methodName) -> findRecordMethod(cacheKey, rootClass, methodName);
            return getPropertyViaRecordMethod(object, propertyName, methodFinder, singleArgumentValue);
        } catch (final NoSuchMethodException ignored) {
        }
        //
        // try by field name -  object.propertyName;
        try {
            return getPropertyViaFieldAccess(cacheKey, object, propertyName);
        } catch (final NoSuchMethodException ignored) {
        }
        // we have nothing to ask for, and we have exhausted our lookup strategies
        putInNegativeCache(cacheKey);
        return null;
    }

    private Optional<Function<Object, Object>> lambdaGetter(final String propertyName, final Object object) {
        if (USE_LAMBDA_FACTORY.get()) {
            return TgLambdaFetchingSupport.createGetter(object.getClass(), propertyName);
        }
        return Optional.empty();
    }

    private boolean isNegativelyCached(final CacheKey key) {
        if (USE_NEGATIVE_CACHE.get()) {
            return NEGATIVE_CACHE.containsKey(key);
        }
        return false;
    }

    private void putInNegativeCache(final CacheKey key) {
        if (USE_NEGATIVE_CACHE.get()) {
            NEGATIVE_CACHE.put(key, key);
        }
    }

    private interface MethodFinder {
        Method apply(Class<?> aClass, String s) throws NoSuchMethodException;
    }

    private Object getPropertyViaRecordMethod(final Object object, final String propertyName, final MethodFinder methodFinder, final Supplier<Object> singleArgumentValue) throws NoSuchMethodException {
        final Method method = methodFinder.apply(object.getClass(), propertyName);
        return invokeMethod(object, singleArgumentValue, method, takesSingleArgumentTypeAsOnlyArgument(method));
    }

    private Object getPropertyViaGetterMethod(final Object object, final String propertyName, final GraphQLType graphQLType, final MethodFinder methodFinder, final Supplier<Object> singleArgumentValue) throws NoSuchMethodException {
        if (isBooleanProperty(graphQLType)) {
            try {
                return getPropertyViaGetterUsingPrefix(object, propertyName, "get" /* TG change, was "is" */, methodFinder, singleArgumentValue);
            } catch (final NoSuchMethodException e) {
                return getPropertyViaGetterUsingPrefix(object, propertyName, "is" /* TG change, was "get" */, methodFinder, singleArgumentValue);
            }
        } else {
            return getPropertyViaGetterUsingPrefix(object, propertyName, "get", methodFinder, singleArgumentValue);
        }
    }

    private Object getPropertyViaGetterUsingPrefix(final Object object, final String propertyName, final String prefix, final MethodFinder methodFinder, final Supplier<Object> singleArgumentValue) throws NoSuchMethodException {
        final String getterName = prefix + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        final Method method = methodFinder.apply(object.getClass(), getterName);
        return invokeMethod(object, singleArgumentValue, method, takesSingleArgumentTypeAsOnlyArgument(method));
    }

    /**
     * Invoking public methods on package-protected classes via reflection
     * causes exceptions. This method searches a class's hierarchy for
     * public visibility parent classes with the desired getter. This
     * particular case is required to support AutoValue style data classes,
     * which have abstract public interfaces implemented by package-protected
     * (generated) subclasses.
     */
    private Method findPubliclyAccessibleMethod(final CacheKey cacheKey, final Class<?> rootClass, final String methodName, final boolean dfeInUse, final boolean allowStaticMethods) throws NoSuchMethodException {
        Class<?> currentClass = rootClass;
        while (currentClass != null) {
            if (Modifier.isPublic(currentClass.getModifiers())) {
                if (dfeInUse) {
                    //
                    // try a getter that takes singleArgumentType first (if we have one)
                    try {
                        final Method method = currentClass.getMethod(methodName, singleArgumentType);
                        if (isSuitablePublicMethod(method, allowStaticMethods)) {
                            METHOD_CACHE.putIfAbsent(cacheKey, new CachedMethod(method));
                            return method;
                        }
                    } catch (final NoSuchMethodException e) {
                        // ok try the next approach
                    }
                }
                final Method method = currentClass.getMethod(methodName);
                if (isSuitablePublicMethod(method, allowStaticMethods)) {
                    METHOD_CACHE.putIfAbsent(cacheKey, new CachedMethod(method));
                    return method;
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        assert rootClass != null;
        return rootClass.getMethod(methodName);
    }

    private boolean isSuitablePublicMethod(final Method method, final boolean allowStaticMethods) {
        final int methodModifiers = method.getModifiers();
        if (Modifier.isPublic(methodModifiers)) {
            //noinspection RedundantIfStatement
            if (Modifier.isStatic(methodModifiers) && !allowStaticMethods) {
                return false;
            }
            return true;
        }
        return false;
    }

    /*
       https://docs.oracle.com/en/java/javase/15/language/records.html

       A record class declares a sequence of fields, and then the appropriate accessors, constructors, equals, hashCode, and toString methods are created automatically.

       Records cannot extend any class - so we need only check the root class for a publicly declared method with the propertyName

       However, we won't just restrict ourselves strictly to true records.  We will find methods that are record like
       and fetch them - e.g. `object.propertyName()`

       We won't allow static methods for record like methods however
     */
    private Method findRecordMethod(final CacheKey cacheKey, final Class<?> rootClass, final String methodName) throws NoSuchMethodException {
        return findPubliclyAccessibleMethod(cacheKey, rootClass, methodName, false, false);
    }

    private Method findViaSetAccessible(final CacheKey cacheKey, final Class<?> aClass, final String methodName, final boolean dfeInUse) throws NoSuchMethodException {
        if (!USE_SET_ACCESSIBLE.get()) {
            throw new FastNoSuchMethodException(methodName);
        }
        Class<?> currentClass = aClass;
        while (currentClass != null) {
            final Predicate<Method> whichMethods = mth -> {
                if (dfeInUse) {
                    return hasZeroArgs(mth) || takesSingleArgumentTypeAsOnlyArgument(mth);
                }
                return hasZeroArgs(mth);
            };
            final Method[] declaredMethods = currentClass.getDeclaredMethods();
            final Optional<Method> m = Arrays.stream(declaredMethods)
                    .filter(mth -> methodName.equals(mth.getName()))
                    .filter(whichMethods)
                    .min(mostMethodArgsFirst());
            if (m.isPresent()) {
                try {
                    // few JVMs actually enforce this but it might happen
                    final Method method = m.get();
                    method.setAccessible(true);
                    METHOD_CACHE.putIfAbsent(cacheKey, new CachedMethod(method));
                    return method;
                } catch (final SecurityException ignored) {
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        throw new FastNoSuchMethodException(methodName);
    }

    private Object getPropertyViaFieldAccess(final CacheKey cacheKey, final Object object, final String propertyName) throws FastNoSuchMethodException {
        final Class<?> aClass = object.getClass();
        try {
            final Field field = aClass.getField(propertyName);
            FIELD_CACHE.putIfAbsent(cacheKey, field);
            return field.get(object);
        } catch (final NoSuchFieldException e) {
            if (!USE_SET_ACCESSIBLE.get()) {
                throw new FastNoSuchMethodException(cacheKey.toString());
            }
            // if not public fields then try via setAccessible
            try {
                final Field field = aClass.getDeclaredField(propertyName);
                field.setAccessible(true);
                FIELD_CACHE.putIfAbsent(cacheKey, field);
                return field.get(object);
            } catch (SecurityException | NoSuchFieldException ignored2) {
                throw new FastNoSuchMethodException(cacheKey.toString());
            } catch (final IllegalAccessException e1) {
                throw new GraphQLException(e);
            }
        } catch (final IllegalAccessException e) {
            throw new GraphQLException(e);
        }
    }

    private Object invokeMethod(final Object object, final Supplier<Object> singleArgumentValue, final Method method, final boolean takesSingleArgument) throws FastNoSuchMethodException {
        try {
            if (takesSingleArgument) {
                final Object argValue = singleArgumentValue.get();
                if (argValue == null) {
                    throw new FastNoSuchMethodException(method.getName());
                }
                return method.invoke(object, argValue);
            } else {
                return method.invoke(object);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new GraphQLException(e);
        }
    }

    private Object invokeField(final Object object, final Field field) {
        try {
            return field.get(object);
        } catch (final IllegalAccessException e) {
            throw new GraphQLException(e);
        }
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean isBooleanProperty(final GraphQLType graphQLType) {
        if (graphQLType == GraphQLBoolean) {
            return true;
        }
        if (isNonNull(graphQLType)) {
            return unwrapOne(graphQLType) == GraphQLBoolean;
        }
        return false;
    }

    public void clearReflectionCache() {
        LAMBDA_CACHE.clear();
        METHOD_CACHE.clear();
        FIELD_CACHE.clear();
        NEGATIVE_CACHE.clear();
    }

    public boolean setUseSetAccessible(final boolean flag) {
        return USE_SET_ACCESSIBLE.getAndSet(flag);
    }

    public boolean setUseLambdaFactory(final boolean flag) {
        return USE_LAMBDA_FACTORY.getAndSet(flag);
    }

    public boolean setUseNegativeCache(final boolean flag) {
        return USE_NEGATIVE_CACHE.getAndSet(flag);
    }

    private CacheKey mkCacheKey(final Object object, final String propertyName) {
        final Class<?> clazz = object.getClass();
        final ClassLoader classLoader = clazz.getClassLoader();
        return new CacheKey(classLoader, clazz.getName(), propertyName);
    }

    private static final class CacheKey {
        private final ClassLoader classLoader;
        private final String className;
        private final String propertyName;

        private CacheKey(final ClassLoader classLoader, final String className, final String propertyName) {
            this.classLoader = classLoader;
            this.className = className;
            this.propertyName = propertyName;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof final CacheKey cacheKey)) {
                return false;
            }
            return Objects.equals(classLoader, cacheKey.classLoader) && Objects.equals(className, cacheKey.className) && Objects.equals(propertyName, cacheKey.propertyName);
        }

        @Override
        public int hashCode() {
            int result = 1;
            result = 31 * result + Objects.hashCode(classLoader);
            result = 31 * result + Objects.hashCode(className);
            result = 31 * result + Objects.hashCode(propertyName);
            return result;
        }

        @Override
        public String toString() {
            return "CacheKey{" +
                    "classLoader=" + classLoader +
                    ", className='" + className + '\'' +
                    ", propertyName='" + propertyName + '\'' +
                    '}';
        }
    }

    // by not filling out the stack trace, we gain speed when using the exception as flow control
    private boolean hasZeroArgs(final Method mth) {
        return mth.getParameterCount() == 0;
    }

    private boolean takesSingleArgumentTypeAsOnlyArgument(final Method mth) {
        return mth.getParameterCount() == 1 &&
                mth.getParameterTypes()[0].equals(singleArgumentType);
    }

    private static Comparator<? super Method> mostMethodArgsFirst() {
        return Comparator.comparingInt(Method::getParameterCount).reversed();
    }

    private static class FastNoSuchMethodException extends NoSuchMethodException {
        public FastNoSuchMethodException(final String methodName) {
            super(methodName);
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
    }
}