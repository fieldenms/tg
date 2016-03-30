package ua.com.fielden.platform.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
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

import org.apache.log4j.Logger;

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
import ua.com.fielden.platform.utils.Pair;

/**
 * This is a helper class to provide methods related to {@link Annotation}s determination and related entity/property/method analysis based on them.
 *
 * @author TG Team
 *
 */
public final class AnnotationReflector {
    private final static Logger logger = Logger.getLogger(AnnotationReflector.class);

    /** A global lazy static cache of annotations, which is used for annotation information retrieval. */
    private final static Map<FieldOrMethodKey, Map<Class<? extends Annotation>, Annotation>> annotations = new HashMap<>();

    /**
     * Let's hide default constructor, which is not needed for a static class.
     */
    private AnnotationReflector() {
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

    /** Clear cached annotations. */
    public static void clearAnnotationsCache() {
        annotations.clear();
    }

    private static Collection<Annotation> getAnnotations(final Method method) {
        final FieldOrMethodKey methodKey = new FieldOrMethodKey(method);
        final Map<Class<? extends Annotation>, Annotation> cached = annotations.get(methodKey);
        if (cached == null) {
            final Map<Class<? extends Annotation>, Annotation> newCached = new HashMap<>();
            for (final Annotation ann : method.getAnnotations()) {
                newCached.put(ann.annotationType(), ann);
            }
            annotations.put(methodKey, newCached);
        }
        return annotations.get(methodKey).values();
        //	return Arrays.asList(method.getAnnotations()); // make some caching
    }

    private static class FieldOrMethodKey {
        private final String klassName;
        private final String name;
        private final Boolean isField;

        public FieldOrMethodKey(final AccessibleObject accessibleObject) {
            isField = accessibleObject instanceof Field;
            if (isField) {
                final Field field = (Field) accessibleObject;
                klassName = field.getDeclaringClass().getName();
                name = field.getName();
            } else {
                final Method method = (Method) accessibleObject;
                klassName = method.getDeclaringClass().getName();
                name = method.getName();
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((isField == null) ? 0 : isField.hashCode());
            result = prime * result + ((klassName == null) ? 0 : klassName.hashCode());
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final FieldOrMethodKey other = (FieldOrMethodKey) obj;
            if (isField == null) {
                if (other.isField != null) {
                    return false;
                }
            } else if (!isField.equals(other.isField)) {
                return false;
            }
            if (klassName == null) {
                if (other.klassName != null) {
                    return false;
                }
            } else if (!klassName.equals(other.klassName)) {
                return false;
            }
            if (name == null) {
                if (other.name != null) {
                    return false;
                }
            } else if (!name.equals(other.name)) {
                return false;
            }
            return true;
        }
    }

    private static Map<Class<? extends Annotation>, Annotation> cacheAllAnnotations(final AccessibleObject accesibleObject, final FieldOrMethodKey fieldOrMethodKey) {
        // When at least one annotation on accesibleObject is requested -- all existing annotations will be cached.
        // It guarantees that when all annotations are requested afterwards -- no additional caching will be performed.
        final Map<Class<? extends Annotation>, Annotation> newCached = new HashMap<>();
        for (final Annotation ann : accesibleObject.getAnnotations()) {
            newCached.put(ann.annotationType(), ann);
        }
        annotations.put(fieldOrMethodKey, newCached);
        return newCached;
    }

    private static final Empty emptyAnnotation = new Empty();
    private static <T extends Annotation> Annotation cacheAnnotation(final AccessibleObject accesibleObject, final Class<T> annotationClass, final Map<Class<? extends Annotation>, Annotation> annByAccObjectNotNull) {
        final T ann = accesibleObject.getAnnotation(annotationClass);
        final Annotation annNotNull = ann == null ? emptyAnnotation : ann;
        annByAccObjectNotNull.put(annotationClass, annNotNull);
        return annNotNull;
    }

    /**
     * Returns this element's annotation for the specified type if such an annotation is present, else null.
     *
     * @param annotationClass
     *            the Class object corresponding to the annotation type
     * @return this element's annotation for the specified annotation type if present on this element, else null
     * @throws NullPointerException
     *             if the given annotation class is null
     * @since 1.5
     */
    public static <T extends Annotation> T getAnnotation(final AnnotatedElement annotatedElement, final Class<T> annotationClass) {
        if (annotatedElement instanceof Class) {
            return getAnnotationForClass(annotationClass, (Class<?>) annotatedElement);
        } else {
            // return annotatedElement.getAnnotation(annotationClass); // make some caching
            final AccessibleObject accesibleObject = (AccessibleObject) annotatedElement;
            final FieldOrMethodKey fieldOrMethodKey = new FieldOrMethodKey(accesibleObject);
            final Map<Class<? extends Annotation>, Annotation> annByAccObject = annotations.get(fieldOrMethodKey);
            final Map<Class<? extends Annotation>, Annotation> annByAccObjectNotNull = annByAccObject == null ? cacheAllAnnotations(accesibleObject, fieldOrMethodKey)
                    : annByAccObject;
            final Annotation annByAccObjectAndAnnClass = annByAccObjectNotNull.get(annotationClass);
            final Annotation annByAccObjectAndAnnClassNotNull = annByAccObjectAndAnnClass == null ? cacheAnnotation(accesibleObject, annotationClass, annByAccObjectNotNull)
                    : annByAccObjectAndAnnClass;
            return annByAccObjectAndAnnClassNotNull instanceof Empty ? null : (T) annByAccObjectAndAnnClassNotNull;
        }
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
        final List<Method> methods = new ArrayList<Method>();
        Class<?> klass = type;
        while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
            // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
            final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(klass.getDeclaredMethods()));
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
        return getMethodsAnnotatedWith(type, Optional.of(annotation)).size() != 0;
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
    public static Class<? extends Comparable> getKeyType(final Class<?> type) {
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
    private static <T extends Annotation> T getAnnotationForClass(final Class<T> annotationType, final Class<?> forType) {
        Class<?> runningType = forType;
        while (runningType != null && !runningType.equals(Object.class)) { // need to iterated thought entity hierarchy
            if (runningType.isAnnotationPresent(annotationType)) {
                return runningType.getAnnotation(annotationType);
            }
            runningType = runningType.getSuperclass();
        }
        return null;
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
            return AnnotationReflector.getAnnotation(Finder.findFieldByName(forType, dotNotationExp), annotationType);
        }
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
