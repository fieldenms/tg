package ua.com.fielden.platform.reflection;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.types.tuples.T2;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.reflection.Finder.findFieldByNameOptionally;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.*;
import static ua.com.fielden.platform.reflection.Reflector.MAXIMUM_CACHE_SIZE;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.utils.EntityUtils.splitPropPath;

/**
 * This is a helper class to provide methods related to {@link Annotation}s determination and related entity/property/method analysis based on them.
 *
 * @author TG Team
 *
 */
public final class AnnotationReflector {
    private static final Logger LOGGER = getLogger(AnnotationReflector.class);

    /** A global lazy static cache of annotations, which is used for annotation information retrieval. */
    private static final Cache<Class<?>, Cache<String, Map<Class<? extends Annotation>, Annotation>>> METHOD_ANNOTATIONS = CacheBuilder.newBuilder().weakKeys().initialCapacity(1000).maximumSize(MAXIMUM_CACHE_SIZE).concurrencyLevel(50).build();
    private static final Cache<Class<?>, Cache<String, Map<Class<? extends Annotation>, Annotation>>> FIELD_ANNOTATIONS = CacheBuilder.newBuilder().weakKeys().initialCapacity(1000).maximumSize(MAXIMUM_CACHE_SIZE).concurrencyLevel(50).build();

    public static T2<Long, Long> cleanUp() {
        METHOD_ANNOTATIONS.cleanUp();
        FIELD_ANNOTATIONS.cleanUp();
        return t2(METHOD_ANNOTATIONS.size(), FIELD_ANNOTATIONS.size());
    }

    /**
     * Let's hide default constructor, which is not needed for a static class.
     */
    private AnnotationReflector() {
    }

    /**
     * Returns a collection of annotations that are associated with {@code method}.
     */
    private static Collection<Annotation> getAnnotations(final Method method) {
        return getMethodAnnotations(method).values();
    }

    /**
     * Returns true if an annotation for the specified type is present on this element, else false.
     */
    public static boolean isAnnotationPresent(final AnnotatedElement annotatedElement, final Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotatedElement, annotationClass) != null;
    }

    /**
     * Similar to {@link #getAnnotationForClass(Class, Class)}, but instead of an actual annotation returns {@code true} if annotation is present, {@code false} otherwise.
     *
     * @param annotationType
     * @param forType
     * @return
     */
    public static boolean isAnnotationPresentForClass(final Class<? extends Annotation> annotationType, final Class<?> forType) {
        return getAnnotationForClass(annotationType, forType) != null;
    }

    /// Returns the element's annotation of the specified type if such an annotation is present, else `null`.
    ///
    public static <A extends Annotation> @Nullable A getAnnotation(final AnnotatedElement annotatedElement, final Class<A> annotationType) {
        if (annotatedElement == null) {
            throw new InvalidArgumentException("Argument [annotatedElement] cannot be null.");
        }
        return switch (annotatedElement) {
            case Class<?> klass -> getAnnotationForClass(annotationType, klass);
            case Field field -> (A) getFieldAnnotations(field).get(annotationType);
            case Method method -> (A) getMethodAnnotations(method).get(annotationType);
            default -> throw new ReflectionException(format("Reflecting on annotations for [%s] is not supported.", annotatedElement.getClass().getTypeName()));
        };
    }

    /// Returns the element's annotation of the specified type if such an annotation is present, else throws an exception.
    ///
    public static <A extends Annotation> A requireAnnotation(final AnnotatedElement annotatedElement, final Class<A> annotationType) {
        final var annot = getAnnotation(annotatedElement, annotationType);
        if (annot == null) {
            throw new InvalidArgumentException(format(
                    "Required annotation @%s is missing for [%s].",
                    annotationType.getCanonicalName(),
                    switch (annotatedElement) {
                        case Class<?> it -> it.getCanonicalName();
                        case Member it -> "%s.%s".formatted(it.getDeclaringClass().getCanonicalName(), it.getName());
                        default -> annotatedElement.toString();
                    }));
        }
        return annot;
    }

    public static Map<Class<? extends Annotation>, Annotation> getFieldAnnotations(final Field field) {
        final Class<?> klass = field.getDeclaringClass();
        final String name = field.getName();

        final Cache<String, Map<Class<? extends Annotation>, Annotation>> cachedFieldAnnotations;
        try {
            cachedFieldAnnotations = FIELD_ANNOTATIONS.get(klass, () -> CacheBuilder.newBuilder().weakValues().build());
        } catch (final ExecutionException ex) {
            LOGGER.error(ex);
            throw new ReflectionException(format("Could not get annotation for field [%s].", field), ex);
        }

        return annotationExtractionHelper(field, name, cachedFieldAnnotations);
    }

    public static Map<Class<? extends Annotation>, Annotation> getFieldAnnotations(final Class<?> enclosingType, final CharSequence fieldPath) {
        return getFieldAnnotations(Finder.findFieldByName(enclosingType, fieldPath));
    }

    public static Map<Class<? extends Annotation>, Annotation> getMethodAnnotations(final Method method) {
        final Class<?> klass = method.getDeclaringClass();
        final String name = method.getName();

        final Cache<String, Map<Class<? extends Annotation>, Annotation>> cachedMethodAnnotations;
        try {
            cachedMethodAnnotations = METHOD_ANNOTATIONS.get(klass, () -> CacheBuilder.newBuilder().weakValues().build());
        } catch (final ExecutionException ex) {
            LOGGER.error(ex);
            throw new ReflectionException(format("Could not get annotation for method [%s].", method), ex);
        }

        return annotationExtractionHelper(method, name, cachedMethodAnnotations);
    }

    private static Map<Class<? extends Annotation>, Annotation> annotationExtractionHelper(final AnnotatedElement el, final String name, final Cache<String, Map<Class<? extends Annotation>, Annotation>> cachedAnnotations) {
        try {
            return cachedAnnotations.get(name, () -> {
                    final Map<Class<? extends Annotation>, Annotation> newCached = new HashMap<>();
                    for (final Annotation ann : el.getAnnotations()) {
                        newCached.put(ann.annotationType(), ann);
                    }
                    return newCached;
            });
        } catch (final ExecutionException ex) {
            throw new ReflectionException("Could not get annotations for [%s]".formatted(name), ex);
        }
    }

    /**
     * The same as {@link #getAnnotation(AnnotatedElement, Class)}, but with an {@link Optional} result.
     *
     * @param annotatedElement
     * @param annotationClass
     * @return
     */
    public static <T extends Annotation> Optional<T> getAnnotationOptionally(final AnnotatedElement annotatedElement, final Class<T> annotationClass) {
        return Optional.ofNullable(getAnnotation(annotatedElement, annotationClass));
    }

    // //////////////////////////////////METHOD RELATED ////////////////////////////////////////
    /**
     *
     * Returns a list of methods (including private, protected, and public) annotated with the specified annotation.
     * This method processes the whole class hierarchy.
     * <p>
     * Important: overridden methods resolve as different (e.g., both overridden methods `getKey()` from {@link AbstractUnionEntity} and the original `getKey()` from {@link AbstractEntity}
     * will be returned for {@link AbstractUnionEntity} descendant).
     *
     * @param type
     * @param annotation -- optional annotation argument; if empty arugment is provided then all methods of the specified type are returned
     * @return
     */
    public static List<Method> getMethodsAnnotatedWith(final Class<?> type, final Optional<Class<? extends Annotation>> annotation) {
        final List<Method> methods = new ArrayList<>();
        Class<?> klass = type;
        while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
            // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
            final List<Method> allMethods = new ArrayList<>(Arrays.asList(klass.getDeclaredMethods()));
            if (AbstractUnionEntity.class.isAssignableFrom(klass) && AbstractUnionEntity.class != klass) { // add common methods in case of AbstractUnionEntity descendant:
                allMethods.addAll(AbstractUnionEntity.commonMethods((Class<? extends AbstractUnionEntity>) klass));
            }
            for (final Method method : allMethods) {
                if (!method.isBridge() && (!annotation.isPresent() || AnnotationReflector.isAnnotationPresent(method, annotation.get()))) {
                    methods.add(method);
                }
            }
            // move to the upper class in the hierarchy in search for more methods
            klass = klass.getSuperclass();
        }
        return methods;
    }

    /**
     * Determines whether class has methods annotated with the specified annotation.
     *
     * @param type
     * @param annotation
     * @return
     */
    public static boolean isClassHasMethodAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
        return !getMethodsAnnotatedWith(type, Optional.of(annotation)).isEmpty();
    }

    /**
     *
     * Returns a whole list of methods (including private, protected and public). This method processes the whole class hierarchy.
     * <p>
     * Important : overridden methods resolve as different. (e.g.: both overridden "getKey()" from {@link AbstractUnionEntity} and original "getKey()" from {@link AbstractEntity}
     * would be returned for a {@link AbstractUnionEntity} descendant)
     *
     * @param type
     * @return
     */
    public static List<Method> getMethods(final Class<?> type) {
        return getMethodsAnnotatedWith(type, Optional.empty());
    }

    /**
     * Return validation annotations associated with the given mutator and defined by {@link ValidationAnnotation}.
     */
    public static Set<Annotation> getValidationAnnotations(final Method mutator) {
        return getAnnotations(mutator).stream()
                .filter(at -> ValidationAnnotation.getValueByType(at) != null)
                .collect(toImmutableSet());
    }

    // //////////////////////////////////CLASS RELATED ////////////////////////////////////////

    /**
     *
     * Deduces a key type for on an entity type based on {@link KeyType} annotation.
     * <p>
     * The method traverses the whole entity type hierarchy to find the most top {@link KeyType} annotation present. The method returns null if the annotation not found on any of
     * the types in the hierarchy.
     *
     * @param type
     * @return
     */
    public static Class<? extends Comparable<?>> getKeyType(@Nullable final Class<?> type) {
        final KeyType keyType = getAnnotationForClass(KeyType.class, type);
        return keyType != null ? keyType.value() : null;
    }

    /**
     * Traverses forType hierarchy bottom-up in search of the specified annotation. Returns null if not present, annotation instance otherwise.
     *
     * @param annotationType
     * @param forType
     * @return
     */
    public static <T extends Annotation> @Nullable T getAnnotationForClass(final Class<T> annotationType, final Class<?> forType) {
        Class<?> runningType = forType;
        T annotation = null;
        while (annotation == null && runningType != null && runningType != Object.class) { // need to iterated thought entity hierarchy
            annotation = runningType.getAnnotation(annotationType);
            runningType = runningType.getSuperclass();
        }
        return annotation;
    }

    /**
     * Determines if the entity type represents a "transaction entity". See {@link TransactionEntity} for more details.
     *
     * @param forType
     * @return
     */
    public static boolean isTransactionEntity(final Class<?> forType) {
        return isAnnotationPresentForClass(TransactionEntity.class, forType);
    }

    /**
     * Determines a "transaction date" property name for "transaction entity". See {@link TransactionEntity} for more details.
     *
     * @param forType
     * @return
     */
    public static String getTransactionDateProperty(final Class<?> forType) {
        return getAnnotationForClass(TransactionEntity.class, forType).value();
    }

    // //////////////////////////////////PROPERTY RELATED ////////////////////////////////////////

    /**
     * If an annotation of the given type is present on a property at the given location, returns the annotation,
     * otherwise returns {@code null}.
     * <p>
     * Property location interpretation takes into account the following special cases:
     * <ol>
     *   <li> The last property in the path is {@code key} and the annotation type is {@link KeyType}.
     *      <ul>
     *        <li> Annotation {@link KeyType} will be located on the type that owns property {@code key}.
     *      </ul>
     *   <li> The last property in the path is {@code key} and the annotation type is {@link KeyTitle}.
     *      <ul>
     *        <li> Annotation {@link KeyTitle} will be located on the type that owns property {@code key}.
     *      </ul>
     *   <li> The last property in the path is {@code desc} and annotation type is {@link DescTitle}.
     *      <ul>
     *        <li> Annotation {@link DescTitle} will be located on the type that owns property {@code desc}.
     *      </ul>
     * </ol>
     *
     * @param annotationType  annotation type
     * @param forType  type that determines the property's location
     * @param dotNotationExp  property path
     * @return  the annotation, if found, otherwise {@code null}
     */
    public static <A extends Annotation> @Nullable A getPropertyAnnotation(final Class<A> annotationType, final Class<?> forType, final String dotNotationExp) {

        final var lastProp = splitPropPath(dotNotationExp).getLast();
        if (lastProp.equals(AbstractEntity.KEY) && KeyType.class == annotationType ||
            lastProp.equals(AbstractEntity.KEY) && KeyTitle.class == annotationType ||
            lastProp.equals(AbstractEntity.DESC) && DescTitle.class == annotationType)
        {
            return getAnnotationForClass(annotationType, transform(forType, dotNotationExp).getKey());
        }
        else {
            return findFieldByNameOptionally(forType, dotNotationExp).map(field -> getAnnotation(field, annotationType)).orElse(null);
        }
    }

    /// Same as [#getPropertyAnnotation(Class, Class, String)] but throws instead of returning null.
    public static <A extends Annotation> A requirePropertyAnnotation(
            final Class<A> annotationType,
            final Class<?> forType,
            final CharSequence propertyPath)
    {
        final var annotation = getPropertyAnnotation(annotationType, forType, propertyPath.toString());
        if (annotation == null) {
            throw new ReflectionException(format("Missing annotation @%s on property [%s] in [%s]",
                                                 annotationType.getTypeName(), propertyPath, forType.getTypeName()));
        }
        return annotation;
    }

    /**
     * The same as {@link #getPropertyAnnotation(Class, Class, String)}, but with an {@link Optional} result.
     */
    public static <A extends Annotation> Optional<A> getPropertyAnnotationOptionally(final Class<A> annotationType, final Class<?> forType, final String dotNotationExp) {
        return Optional.ofNullable(getPropertyAnnotation(annotationType, forType, dotNotationExp));
    }

    /**
     * Returns {@code true} if {@link Calculated} annotation represents <i>contextual</i> calculated property, <code>false</code> otherwise. <i>Contextual</i> calculated
     * properties are generated using {@link IDomainTreeEnhancer} and can be dependent on type higher than direct parent type.
     *
     * @param calculatedAnnotation
     * @return
     */
    public static boolean isContextual(final Calculated calculatedAnnotation) {
        return !calculatedAnnotation.rootTypeName().equals(Calculated.NOTHING) && //
        !calculatedAnnotation.contextPath().equals(Calculated.NOTHING) && //
        !calculatedAnnotation.origination().equals(Calculated.NOTHING);
    }

    /**
     * Similar to {@link #getPropertyAnnotation(Class, Class, String)}, but instead of an actual annotation returns <code>true</code> if annotation is present, <code>false</code>
     * otherwise.
     *
     * @param annotationType
     * @param forType
     * @param dotNotationExp
     * @return
     */
    public static <T extends Annotation> boolean isPropertyAnnotationPresent(final Class<T> annotationType, final Class<?> forType, final String dotNotationExp) {
        return getPropertyAnnotation(annotationType, forType, dotNotationExp) != null;
    }

    /**
     * Returns true if any property in dotNotationExp parameter has an annotation specified with annotationType.
     *
     * @param annotationType
     * @param forType
     * @param dotNotationExp
     * @return
     */
    public static boolean isAnnotationPresentInHierarchy(final Class<? extends Annotation> annotationType, final Class<?> forType, final String dotNotationExp) {
        if (isPropertyAnnotationPresent(annotationType, forType, dotNotationExp)) {
            return true;
        } else if (PropertyTypeDeterminator.isDotExpression(dotNotationExp)) {
            return isAnnotationPresentInHierarchy(annotationType, forType, PropertyTypeDeterminator.penultAndLast(dotNotationExp).getKey());
        } else {
            return false;
        }
    }

    /**
     * Returns an optional annotation value of type {@code annotationType} for a property specified by {@code dotNotationExp} in a type hierarchy starting with {@code forType}.
     *
     * @param annotationType
     * @param forType
     * @param dotNotationExp
     * @return
     */
    public static <T extends Annotation> Optional<T> getPropertyAnnotationInHierarchy (final Class<T> annotationType, final Class<?> forType, final String dotNotationExp) {
        final T annotation = getPropertyAnnotation(annotationType, forType, dotNotationExp);
        if (annotation != null) {
            return of(annotation);
        } else if (isDotExpression(dotNotationExp)) {
            return getPropertyAnnotationInHierarchy(annotationType, forType, penultAndLast(dotNotationExp).getKey());
        }
        return empty();
    }

    /**
     * Returns <code>true</code> when the properties represents 'secrete' property, <code>false</code> otherwise.
     *
     * @param entityType
     * @param propName
     * @return
     */
    public static boolean isSecreteProperty(final Class<?> entityType, final String propName) {
        boolean isSecrete = false;
        try {
            isSecrete = getPropertyAnnotation(Secrete.class, entityType, propName) != null;
        } catch (final Exception ex) {
            // in most cases this exception will be thrown when entity is the DynamicEntityQueryCriteria
        }
        return isSecrete;
    }
}
