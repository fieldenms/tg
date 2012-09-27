package ua.com.fielden.platform.criteria.generator.impl;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.criteria.enhanced.CriteriaProperty;
import ua.com.fielden.platform.criteria.enhanced.FirstParam;
import ua.com.fielden.platform.criteria.enhanced.SecondParam;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.swing.review.development.EntityQueryCriteria;
import ua.com.fielden.platform.utils.Pair;

/**
 * Implements basic reflection functionality for query criteria class (i.e. retrieving title and description for specified property, generating property names etc.)
 *
 * @author TG Team
 *
 */
public class CriteriaReflector {

    private static final String IS = "is", NOT = "not", FROM = "from", TO = "to";

    /**
     * Enhances the dot-notated property name with appropriate suffix to indicate that it is a range property.
     *
     * @param propertyName
     * @return
     */
    public static String from(final String propertyName){
	return !StringUtils.isEmpty(propertyName) ? propertyName + "_" + FROM : FROM;
    }

    /**
     * Enhances the dot-notated property name with appropriate suffix to indicate that it is a range property.
     *
     * @param propertyName
     * @return
     */
    public static String to(final String propertyName) {
	return !StringUtils.isEmpty(propertyName) ? propertyName + "_" + TO : TO;
    }

    /**
     * Enhances the dot-notated property name with appropriate suffix to indicate that it is a boolean property.
     *
     * @param propertyName
     * @return
     */
    public static String is(final String propertyName) {
	return !StringUtils.isEmpty(propertyName) ? propertyName + "_" + IS : IS;
    }

    /**
     * Enhances the dot-notated property name with appropriate suffix to indicate that it is a boolean property.
     *
     * @param propertyName
     * @return
     */
    public static String not(final String propertyName) {
	return !StringUtils.isEmpty(propertyName) ? propertyName + "_" + NOT : NOT;
    }

    /**
     * Returns a pair of title and description for specified dot-notated property name and root type.
     */
    public static Pair<String, String> getCriteriaTitleAndDesc(final Class<?> root, final String propertyName){
	final String realPropertyName = "".equals(propertyName) ? AbstractEntity.KEY : propertyName;
	final Pair<String, String> titleAndDesc = TitlesDescsGetter.getTitleAndDesc(realPropertyName, root);
	return new Pair<String, String>(TitlesDescsGetter.removeHtmlTag(titleAndDesc.getKey()), TitlesDescsGetter.removeHtmlTag(titleAndDesc.getValue()));
    }

    /**
     * Generates the criteria property name. The generated property name must be unique.
     * New generated criteria property name consists of two parts: root type name, property name.
     * For example: if root = EntityType.class, property = property.anotherProperty.nestedProperty, then generated property name will be - entityType_property_anotherProperty_nestedProperty.
     *
     * @param root - the type from which the property was taken.
     * @param propertyName - the name of the property for which criteria property name must be generated.
     * @param suffix - the additional suffix if the generated property has a pair.
     * @return
     */
    public static String generateCriteriaPropertyName(final Class<?> root, final String propertyName){
	return root.getSimpleName().substring(0, 1).toLowerCase() + root.getSimpleName().substring(1) + "_" + propertyName.replaceAll(Reflector.DOT_SPLITTER, "_");
    }


    /**
     * Returns the list of criteria properties for specified {@link EntityQueryCriteria} class.
     *
     * @param criteriaClass
     * @return
     */
    public static List<Field> getCriteriaProperties(final Class<?> criteriaClass) {
	return Finder.findProperties(criteriaClass, IsProperty.class, CriteriaProperty.class);
    }

    /**
     * Returns true if the specified criteria property is annotated with {@link SecondParam}.
     *
     * @param criteriaClass
     * @param propertyName
     * @return
     */
    public static boolean isSecondParam(final Class<?> criteriaClass, final String propertyName){
	return AnnotationReflector.isPropertyAnnotationPresent(SecondParam.class, criteriaClass, propertyName);
    }

    /**
     * Returns true if the specified criteria property is annotated with {@link FirstParam}.
     *
     * @param criteriaClass
     * @param propertyName
     * @return
     */
    public static boolean isFirstParam(final Class<?> criteriaClass, final String propertyName){
	return AnnotationReflector.isPropertyAnnotationPresent(FirstParam.class, criteriaClass, propertyName);
    }

    /**
     * Returns the name of the second property related to the specified one.
     *
     * @param criteriaClass
     * @param propertyName
     * @return
     */
    public static String getSecondParamFor(final Class<?> criteriaClass, final String propertyName){
	final FirstParam firstParam = AnnotationReflector.getPropertyAnnotation(FirstParam.class, criteriaClass, propertyName);
	if(firstParam != null){
	    return firstParam.secondParam();
	}
	return null;
    }

    /**
     * Returns the name of the first property related to the specified one.
     *
     * @param criteriaClass
     * @param propertyName
     * @return
     */
    public static String getFirstParamFor(final Class<?> criteriaClass, final String propertyName){
	final SecondParam secondParam = AnnotationReflector.getPropertyAnnotation(SecondParam.class, criteriaClass, propertyName);
	if(secondParam != null){
	    return secondParam.firstParam();
	}
	return null;
    }

    /**
     * Returns the parameters of the {@link CriteriaProperty} annotation for the specified property of the {@link EntityQueryCriteria} class.
     *
     * @param criteriaClass
     * @param propertyName
     * @return
     */
    public static String getCriteriaProperty(final Class<?> criteriaClass, final String propertyName){
	final CriteriaProperty annotation = AnnotationReflector.getPropertyAnnotation(CriteriaProperty.class, criteriaClass, propertyName);
	if(annotation != null){
	    return annotation.propertyName();
	}
	return null;
    }
}
