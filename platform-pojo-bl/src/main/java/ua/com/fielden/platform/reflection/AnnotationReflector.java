package ua.com.fielden.platform.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
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

    /**
     * Let's hide default constructor, which is not needed for a static class.
     */
    private AnnotationReflector() {
    }

    // //////////////////////////////////METHOD RELATED ////////////////////////////////////////
    /**
     *
     * Returns a list of methods (including private, protected and public) annotated with the specified annotation. This method processes the whole class hierarchy.
     * <p>
     * Important : overridden methods resolves as different. (e.g.: both overridden "getKey()" from {@link AbstractUnionEntity} and original "getKey()" from {@link AbstractEntity}
     * will be returned for {@link AbstractUnionEntity} descendant)
     *
     * @param type
     * @param annotation
     * @return
     */
    public static List<Method> getMethodsAnnotatedWith(final Class<?> type, final Class<? extends Annotation> annotation) {
	final List<Method> methods = new ArrayList<Method>();
	Class<?> klass = type;
	while (klass != Object.class) { // need to iterated thought hierarchy in order to retrieve methods from above the current instance
	    // iterate though the list of methods declared in the class represented by klass variable, and add those annotated with the specified annotation
	    final List<Method> allMethods = new ArrayList<Method>(Arrays.asList(klass.getDeclaredMethods()));
	    if (AbstractUnionEntity.class.isAssignableFrom(klass) && AbstractUnionEntity.class != klass) { // add common methods in case of AbstractUnionEntity descendant:
		allMethods.addAll(AbstractUnionEntity.commonMethods((Class<? extends AbstractUnionEntity>) klass));
	    }
	    for (final Method method : allMethods) {
		if (annotation == null || method.isAnnotationPresent(annotation)) {
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
	return getMethodsAnnotatedWith(type, annotation).size() != 0;
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
	return getMethodsAnnotatedWith(type, null);
    }

    /**
     * Return a list of validation annotations as determined by {@link ValidationAnnotation} enumeration associated with the specified mutator.
     *
     * @param mutator
     * @return
     */
    public static Set<Annotation> getValidationAnnotations(final Method mutator) {
	final Set<Annotation> validationAnnotations = new HashSet<Annotation>();
	for (final ValidationAnnotation annotationKey : ValidationAnnotation.values()) { // iterate through all validation annotations
	    for (final Annotation annotation : mutator.getAnnotations()) { // and through all annotation on the method
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
	final KeyType keyType = getAnnotation(KeyType.class, type);
	return keyType != null ? keyType.value() : null;
    }

    /**
     * Traverses forType hierarchy bottom-up in search of the specified annotation. Returns null if not present, annotation instance otherwise.
     *
     * @param annotationType
     * @param forType
     * @return
     */
    public static <T extends Annotation> T getAnnotation(final Class<T> annotationType, final Class<?> forType) {
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
     * Similar to {@link #getAnnotation(Class, Class)}, but instead of an actual annotation returns <code>true</code> if annotation is present, <code>false</code> otherwise.
     *
     * @param annotationType
     * @param forType
     * @return
     */
    public static boolean isAnnotationPresent(final Class<? extends Annotation> annotationType, final Class<?> forType) {
	return getAnnotation(annotationType, forType) != null;
    }

    /**
     * Determines if the entity type represents a "transaction entity". See {@link TransactionEntity} for more details.
     *
     * @param forType
     * @return
     */
    public static boolean isTransactionEntity(final Class<?> forType) {
	return isAnnotationPresent(TransactionEntity.class, forType);
    }

    /**
     * Determines a "transaction date" property name for "transaction entity". See {@link TransactionEntity} for more details.
     *
     * @param forType
     * @return
     */
    public static String getTransactionDateProperty(final Class<?> forType) {
	return getAnnotation(TransactionEntity.class, forType).value();
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
	    return getAnnotation(annotationType, transformed.getKey());
	} else {
	    return Finder.findFieldByName(forType, dotNotationExp).getAnnotation(annotationType);
	}
    }

    /**
     * Returns <code>true</code> if {@link Calculated} annotation represents <i>contextual</i> calculated property, <code>false</code> otherwise.
     * <i>Contextual</i> calculated properties are generated using {@link IDomainTreeEnhancer} and can be dependent on type higher than direct parent type.
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
     * Similar to {@link #getPropertyAnnotation(Class, Class, String)}, but instead of an actual annotation returns <code>true</code> if annotation is present, <code>false</code> otherwise.
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

}
