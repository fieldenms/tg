package ua.com.fielden.platform.swing.review;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.utils.Pair;

/**
 * Extends {@link DynamicCriteriaPropertyAnalyser} and implements additional algorithm for retrieving information about property type, name or fields etc.
 *
 * @author oleh
 *
 */
public class DynamicPropertyAnalyser extends DynamicCriteriaPropertyAnalyser {

    /**
     * Just calls the appropriate super constructor.
     *
     * @param declaringType
     * @param dotNotationExp
     */
    public DynamicPropertyAnalyser(final Class<?> declaringType, final String dotNotationExp) {
	super(declaringType, dotNotationExp);
    }

    /**
     * Returns the annotation of the property field (see {@link #getPropertyField()} for more information).
     *
     * @param <T>
     * @param annotationType
     *            - specified annotation type.
     * @return
     */
    public <T extends Annotation> T getPropertyFieldAnnotation(final Class<T> annotationType) {
	final Field propertyField = getPropertyField();
	return propertyField == null ? null : AnnotationReflector.getAnnotation(propertyField, annotationType);
    }

    /**
     * The type of the high level collection which contain this property. If this property is not in collection hierarchy it should be null.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public Pair<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>> getCollectionContainerAndItsParentType() {
	if (propertyFields == null) {
	    return null;
	}
	for (int i = 0; i < propertyFields.length; i++) {
	    if (Collection.class.isAssignableFrom(propertyFields[i].getType())) {
		return new Pair<Class<? extends AbstractEntity<?>>, Class<? extends AbstractEntity<?>>>((Class<? extends AbstractEntity<?>>) propertyTypes[i + 1], (Class<? extends AbstractEntity<?>>) propertyTypes[i]);
	    }
	}
	return null;
    }

    /**
     * The name of the high level collection which contain this property in context of collection parent. If this property is not in collection hierarchy it should be null.
     *
     * @return
     */
    public String getCollectionNameInItsParentTypeContext() {
	if (propertyFields == null) {
	    return null;
	}
	for (int i = 0; i < propertyFields.length; i++) {
	    if (Collection.class.isAssignableFrom(propertyFields[i].getType())) {
		return propertyFields[i].getName();
	    }
	}
	return null;
    }

    /**
     * Returns true if property is inside more than one nested collection hierarchy.
     *
     * @return
     */
    public boolean isInNestedCollections() {
	int collCount = 0;
	for (int i = 0; i < propertyFields.length; i++) {
	    if (Collection.class.isAssignableFrom(propertyFields[i].getType())) {
		collCount++;
	    }
	}
	return collCount > 1;
    }

    /**
     * Determines next cases:
     * <ul>
     * <li>whether union is in collection</li>
     * <li>whether collection is in union</li>
     * <li>whether unions are nested</li>
     * <li>whether collections are nested</li>
     * </ul>
     *
     * @return
     */
    public boolean isUnionCollectionIntersects() {
	if (propertyFields == null) {
	    return false;
	}
	int collCount = 0;
	int unionCount = 0;
	for (int i = 0; i < propertyFields.length; i++) {
	    if (Collection.class.isAssignableFrom(propertyFields[i].getType())) {
		collCount++;
	    } else if (AbstractUnionEntity.class.isAssignableFrom(propertyFields[i].getType())) {
		unionCount++;
	    }
	}
	return (collCount + unionCount) > 1;
    }

    /**
     *
     *
     * @return
     */
    public boolean isInUnionHierarchy() {
	if(propertyFields == null) {
	    return false;
	}
	if (!AbstractUnionEntity.class.isAssignableFrom(getPropertyField().getType())) {
	    for (int index = 0; index < propertyFields.length - 1; index++) {
		if (AbstractUnionEntity.class.isAssignableFrom(propertyFields[index].getType())) {
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     * Returns the parent of the analysing property that is union entity.
     *
     * @return
     */
    public String getUnionParent() {
	if (isInUnionHierarchy()) {
	    String parentName = "";
	    for (int index = getUnionEntityIndex(); index >= 0; index--) {
		parentName = "." + propertyFields[index].getName() + parentName;
	    }
	    return parentName.isEmpty() ? "" : parentName.substring(1);
	}
	return null;
    }

    /**
     * Returns the {@link AbstractEntity} property name that is in union.
     *
     * @return
     */
    public String getUnionGroup(){
	if(isInUnionHierarchy()){
	    String parentName = "";
	    for (int index = getUnionEntityIndex() + 1; index >= 0; index--) {
		parentName = "." + propertyFields[index].getName() + parentName;
	    }
	    return parentName.isEmpty() ? "" : parentName.substring(1);
	}
	return null;
    }

    /**
     * Returns the index of the union entity.
     *
     * @return
     */
    private int getUnionEntityIndex() {
	for (int index = 0; index < propertyFields.length - 1 ; index++) {
	    if (AbstractUnionEntity.class.isAssignableFrom(propertyFields[index].getType())) {
		    return index;
	    }
	}
	return -1;
    }

    /**
     * Returns
     * <p>
     * 1) The name of this property inside collectional parent hierarchy. If this property is not in collection hierarchy it should be null.
     * <p>
     * 2) The name of collectional parent property. If this property is not in collection hierarchy it should be null.
     *
     * @return
     */
    public Pair<String, String> getNamesWithinCollectionalHierarchy() {
	int indexToStart = 0;
	String collectionName = "";
	for (; indexToStart < propertyFields.length; indexToStart++) {
	    if (Collection.class.isAssignableFrom(propertyFields[indexToStart].getType())) {
		break;
	    }
	    collectionName += "." + propertyNames[indexToStart];
	}
	indexToStart++;
	String propertyName = "";
	for (; indexToStart < propertyFields.length; indexToStart++) {
	    propertyName += "." + propertyNames[indexToStart];
	}
	// if property is of AbstractEntity type, then in context of collection it should be used with ".key":
	// if property is empty (collection itself has been chosen), then in context of collection it should be "key":
	final String name = propertyName.isEmpty() ? AbstractEntity.KEY : (AbstractEntity.class.isAssignableFrom(getPropertyType()) ? (propertyName.substring(1) + "." + AbstractEntity.KEY)
		: propertyName.substring(1));
	return new Pair<String, String>(name, collectionName.isEmpty() ? "" : collectionName.substring(1));
    }

    /**
     * Returns the name of the property needed to create criteria query.
     *
     * @return
     */
    public String getCriteriaFullName() {
	Class<?> keyType = getPropertyType();
	String propertyName = getAnalysingProperty();
	while (AbstractEntity.class.isAssignableFrom(keyType)) {
	    if (isEmpty(propertyName)) {
		propertyName += "key";
	    } else {
		propertyName += ".key";
	    }
	    keyType = AnnotationReflector.getKeyType(keyType);
	}
	return propertyName;
    }

    /**
     * Returns the {@link Field} instance that represents analysing property.
     *
     * @return
     */
    private Field getPropertyField() {
        if (propertyFields != null) {
            return propertyFields[propertyFields.length - 1];
        }
        return null;
    }
}