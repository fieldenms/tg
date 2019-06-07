package ua.com.fielden.platform.reflection;

import static java.lang.String.format;
import static ua.com.fielden.platform.reflection.Finder.findFieldByNameOptionally;
import static ua.com.fielden.platform.reflection.Reflector.MAXIMUM_CACHE_SIZE;
import static ua.com.fielden.platform.types.tuples.T2.t2;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Secrete;
import ua.com.fielden.platform.entity.annotation.TransactionEntity;
import ua.com.fielden.platform.entity.validation.annotation.ValidationAnnotation;
import ua.com.fielden.platform.reflection.exceptions.ReflectionException;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.Pair;

/**
 * This is a helper class to provide methods related to {@link Annotation}s determination and related entity/property/method analysis based on them.
 *
 * @author TG Team
 *
 */
public final class AnnotationReflector {
    private static final Logger LOGGER = Logger.getLogger(AnnotationReflector.class);

    /** A global lazy static cache of annotations, which is used for annotation information retrieval. */
    private static final Cache<Class<?>, Map<String, Map<Class<? extends Annotation>, Annotation>>> METHOD_ANNOTATIONS = CacheBuilder.newBuilder().weakKeys().initialCapacity(1000).maximumSize(MAXIMUM_CACHE_SIZE).concurrencyLevel(50).build();
    private static final Cache<Class<?>, Map<String, Map<Class<? extends Annotation>, Annotation>>> FIELD_ANNOTATIONS = CacheBuilder.newBuilder().weakKeys().initialCapacity(1000).maximumSize(MAXIMUM_CACHE_SIZE).concurrencyLevel(50).build();
    
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
     * Similar to {@link #getAnnotation(Class, Class)}, but instead of an actual annotation returns <code>true</code> if annotation is present, <code>false</code> otherwise.
     *
     * @param annotationType
     * @param forType
     * @return
     */
    public static boolean isAnnotationPresentForClass(final Class<? extends Annotation> annotationType, final Class<?> forType) {
        return getAnnotationForClass(annotationType, forType) != null;
    }

    /**
     * Returns this element's annotation for the specified type if such an annotation is present, else null.
     *
     * @param annotationClass
     *            the Class object corresponding to the annotation type
     * @return this element's annotation for the specified annotation type if present on this element, else null
     * @throws NullPointerException
     *             if the given annotation class is null
     */
    public static <T extends Annotation> T getAnnotation(final AnnotatedElement annotatedElement, final Class<T> annotationClass) {
        if (annotatedElement instanceof Class) {
            return getAnnotationForClass(annotationClass, (Class<?>) annotatedElement);
        } else if (annotatedElement instanceof Field) {
            final Field field = (Field) annotatedElement;
            return (T) getFieldAnnotations(field).get(annotationClass);
        } else if (annotatedElement instanceof Method) {
            final Method method = (Method) annotatedElement;
            return (T) getMethodAnnotations(method).get(annotationClass);
        } else {
            throw new ReflectionException(format("Reflecting on annotations for [%s] is not supported.", annotatedElement));
        }
    }

    private static Map<Class<? extends Annotation>, Annotation> getFieldAnnotations(final Field field) {
        final Class<?> klass = field.getDeclaringClass();
        final String name = field.getName();

        final Map<String, Map<Class<? extends Annotation>, Annotation>> cachedMethods;
        try {
            cachedMethods = FIELD_ANNOTATIONS.get(klass, HashMap::new);
        } catch (final ExecutionException ex) {
            LOGGER.error(ex);
            throw new ReflectionException(format("Could not get annotation for field [%s].", field), ex);
        }

        return annotationExtractionHelper(field, name, cachedMethods);
    }

    private static Map<Class<? extends Annotation>, Annotation> getMethodAnnotations(final Method method) {
        final Class<?> klass = method.getDeclaringClass();
        final String name = method.getName();

        final Map<String, Map<Class<? extends Annotation>, Annotation>> cachedMethods;
        try {
            cachedMethods = METHOD_ANNOTATIONS.get(klass, HashMap::new);
        } catch (final ExecutionException ex) {
            LOGGER.error(ex);
            throw new ReflectionException(format("Could not get annotation for method [%s].", method), ex);
        }

        return annotationExtractionHelper(method, name, cachedMethods);
    }

    private static Map<Class<? extends Annotation>, Annotation> annotationExtractionHelper(final AnnotatedElement el, final String name, final Map<String, Map<Class<? extends Annotation>, Annotation>> cachedMethods) {
        final Map<Class<? extends Annotation>, Annotation> cached = cachedMethods.get(name);
        if (cached == null) {
            final Map<Class<? extends Annotation>, Annotation> newCached = new HashMap<>();
            for (final Annotation ann : el.getAnnotations()) {
                newCached.put(ann.annotationType(), ann);
            }
            cachedMethods.put(name, newCached);
            return newCached;
        }
        return cached;
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
     * Returns a list of methods (including private, protected and public) annotated with the specified annotation. This method processes the whole class hierarchy.
     * <p>
     * Important : overridden methods resolves as different. (e.g.: both overridden "getKey()" from {@link AbstractUnionEntity} and original "getKey()" from {@link AbstractEntity}
     * will be returned for {@link AbstractUnionEntity} descendant)
     *
     *
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
     * Important : overridden methods resolves as different. (e.g.: both overridden "getKey()" from {@link AbstractUnionEntity} and original "getKey()" from {@link AbstractEntity}
     * will be returned for {@link AbstractUnionEntity} descendant)
     *
     * @param type
     * @param annotation
     * @return
     */
    public static List<Method> getMethods(final Class<?> type) {
        return getMethodsAnnotatedWith(type, Optional.empty());
    }

    /**
     * Return a list of validation annotations as determined by {@link ValidationAnnotation} enumeration associated with the specified mutator.
     *
     * @param mutator
     * @return
     */
    public static Set<Annotation> getValidationAnnotations(final Method mutator) {
        final Set<Annotation> validationAnnotations = new HashSet<>();
        for (final Annotation annotation : getAnnotations(mutator)) { // and through all annotation on the method
            for (final ValidationAnnotation annotationKey : ValidationAnnotation.values()) { // iterate through all validation annotations
                if (annotation.annotationType() == annotationKey.getType()) { // to find matches
                    validationAnnotations.add(annotation);
                    break;
                }
            }
        }
        return validationAnnotations;
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
    public static Class<? extends Comparable<?>> getKeyType(final Class<?> type) {
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
    public static <T extends Annotation> T getAnnotationForClass(final Class<T> annotationType, final Class<?> forType) {
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
     *
     * Searches for a property annotation of the specified entity type. Returns <code>null</code> if property or annotation is not found. Support don-notation for property name
     * except the <code>key</code> and <code>desc</code> properties, which is not really a limitation.
     * <p>
     * For example, <code>vehicle.eqClass</code> for WorkOrder will be recognised correctly, however <code>vehicle.eqClass.key</code> would not.
     *
     *
     * @param <T>
     * @param annotationType
     * @param forType
     * @param dotNotationExp
     * @return
     */
    public static <T extends Annotation> T getPropertyAnnotation(final Class<T> annotationType, final Class<?> forType, final String dotNotationExp) {
        // if (AbstractEntity.KEY.equals(dotNotationExp) || AbstractEntity.DESC.equals(dotNotationExp)) {
        // return getAnnotation(annotationType, forType);
        // }
        // if (dotNotationExp.endsWith("." + AbstractEntity.KEY) || dotNotationExp.endsWith("." + AbstractEntity.DESC)) {
        // final String containingPropertyName = dotNotationExp.endsWith("." + AbstractEntity.KEY) ? dotNotationExp.substring(0, dotNotationExp.length() - 4) :
        // dotNotationExp.substring(
        // 0, dotNotationExp.length() - 5);
        // return getAnnotation(annotationType, PropertyTypeDeterminator.determinePropertyType(forType, containingPropertyName));
        if (dotNotationExp.endsWith(AbstractEntity.KEY) && KeyType.class.equals(annotationType) || //
        dotNotationExp.endsWith(AbstractEntity.KEY) && KeyTitle.class.equals(annotationType) || //
        dotNotationExp.endsWith(AbstractEntity.DESC) && DescTitle.class.equals(annotationType)) {
            final Pair<Class<?>, String> transformed = PropertyTypeDeterminator.transform(forType, dotNotationExp);
            return getAnnotationForClass(annotationType, transformed.getKey());
        } else {
            return findFieldByNameOptionally(forType, dotNotationExp).map(field -> getAnnotation(field, annotationType)).orElse(null);
        }
    }
    
    /**
     * The same as {@link #getPropertyAnnotation(Class, Class, String)}, but with an {@link Optional} result;
     * 
     * @param annotationType
     * @param forType
     * @param dotNotationExp
     * @return
     */
    public static <T extends Annotation> Optional<T> getPropertyAnnotationOptionally(final Class<T> annotationType, final Class<?> forType, final String dotNotationExp) {
        return Optional.ofNullable(getPropertyAnnotation(annotationType, forType, dotNotationExp));
    }

    /**
     * Returns <code>true</code> if {@link Calculated} annotation represents <i>contextual</i> calculated property, <code>false</code> otherwise. <i>Contextual</i> calculated
     * properties are generated using {@link IDomainTreeEnhancer} and can be dependent on type higher than direct parent type.
     *
     * @param root
     * @param property
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
     * @param superType
     * @param dotNotationExp
     * @return
     */
    public static boolean isAnnotationPresentInHierarchy(final Class<? extends Annotation> annotationType, final Class<?> superType, final String dotNotationExp) {
        //	String propertyName = dotNotationExp;
        //	while (!StringUtils.isEmpty(propertyName)) {
        //	    final Field resField = Finder.findFieldByName(superType, propertyName);
        //	    if (resField.isAnnotationPresent(annotationType)) {
        //		return true;
        //	    }
        //	    final int lastPointIndex = propertyName.lastIndexOf(PropertyTypeDeterminator.PROPERTY_SPLITTER); // "."
        //	    propertyName = propertyName.substring(0, lastPointIndex < 0 ? 0 : lastPointIndex);
        //	}
        //	return false;
        if (isPropertyAnnotationPresent(annotationType, superType, dotNotationExp)) {
            return true;
        } else {
            if (PropertyTypeDeterminator.isDotNotation(dotNotationExp)) {
                return isAnnotationPresentInHierarchy(annotationType, superType, PropertyTypeDeterminator.penultAndLast(dotNotationExp).getKey());
            } else {
                return false;
            }
        }
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
