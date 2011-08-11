package ua.com.fielden.platform.swing.review;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.treemodel.IPropertyFilter;
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
     * @param propertyFilter
     */
    public DynamicPropertyAnalyser(final Class<?> declaringType, final String dotNotationExp, final IPropertyFilter propertyFilter) {
	super(declaringType, dotNotationExp, propertyFilter);
    }

    /**
     * Creates {@link DynamicPropertyAnalyser} instance with specified {@link IPropertyFilter}.
     *
     * @param propertyFilter
     */
    public DynamicPropertyAnalyser(final IPropertyFilter propertyFilter) {
	super();
	setPropertyFilter(propertyFilter);
    }

    /**
     * Returns the title and description for the property.
     *
     * @return
     */
    public Pair<String, String> getTitleAndDesc() {
	if (propertyTypes != null) {
	    final String prefix = getCriteiraFullNamePrefix();
	    return TitlesDescsGetter.getTitleAndDesc(isEmpty(getAnalysingProperty()) ? prefix : getPropertyField().getName(), isEmpty(getAnalysingProperty()) ? getPropertyType()
		    : getDeclaringClasses(propertyNames.length - 1).get(0));
	}
	return new Pair<String, String>("property", "property description");
    }

    /**
     * Returns the prefix that is needed for composing full property name.
     *
     * @return
     */
    private String getCriteiraFullNamePrefix() {
	try {
	    final String fullNamePrefix = getCriteriaFullName().replace(getAnalysingProperty(), "");
	    if (isEmpty(fullNamePrefix)) {
		return fullNamePrefix;
	    } else {
		return fullNamePrefix.startsWith(".") ? fullNamePrefix.substring(1) : fullNamePrefix;
	    }
	} catch (final NullPointerException e) {
	}
	return null;
    }

    /**
     * Returns the {@link Field} instance that represents analysing property.
     *
     * @return
     */
    public Field getPropertyField() {
	if (propertyFields != null) {
	    return propertyFields[propertyFields.length - 1];
	}
	return null;
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
	return propertyField == null ? null : propertyField.getAnnotation(annotationType);
    }

    /**
     * The type of the high level collection which contain this property. If this property is not in collection hierarchy it should be null.
     *
     * @return
     */
    @SuppressWarnings("rawtypes")
    public Pair<Class<? extends AbstractEntity>, Class<? extends AbstractEntity>> getCollectionContainerAndItsParentType() {
	if (propertyFields == null) {
	    return null;
	}
	for (int i = 0; i < propertyFields.length; i++) {
	    if (Collection.class.isAssignableFrom(propertyFields[i].getType())) {
		return new Pair<Class<? extends AbstractEntity>, Class<? extends AbstractEntity>>((Class<? extends AbstractEntity>) propertyTypes[i + 1], (Class<? extends AbstractEntity>) propertyTypes[i]);
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
	final String name = propertyName.isEmpty() ? "" : (AbstractEntity.class.isAssignableFrom(getPropertyType()) ? (propertyName.substring(1) + ".key")
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
	while (AbstractEntity.class.isAssignableFrom(keyType) && simpleKeyType(keyType)) {
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
     * Determines whether analysing property has simple key or complex key (i.e is key is {@link AbstractEntity} or {@link DynamicEntityKey}).
     *
     * @param keyType
     * @return
     */
    private boolean simpleKeyType(final Class<?> keyType) {
	return AnnotationReflector.isAnnotationPresent(KeyType.class, keyType)
		&& !DynamicEntityKey.class.isAssignableFrom(AnnotationReflector.getAnnotation(KeyType.class, keyType).value());
    }

    /**
     * Returns {@link Field} for the full property name. See {@link #getCriteriaFullName()} for more information.
     *
     * @return
     */
    public Field getCriteriaPropertyField() {
	final String prefix = getCriteiraFullNamePrefix();
	Field propertyField = null;
	try {
	    propertyField = isEmpty(prefix) ? getPropertyField() : Finder.findFieldByName(getPropertyType(), prefix);
	} catch (final NullPointerException e) {

	}
	return propertyField;
    }

    /**
     * Returns the annotation of the criteria property.
     *
     * @param annotationType
     *            - specified annotation type.
     *
     * @return
     */
    public <T extends Annotation> T getCriteriaAnnotation(final Class<T> annotationType) {
	final Field propertyField = getCriteriaPropertyField();
	return propertyField == null ? null : propertyField.getAnnotation(annotationType);
    }

    /**
     * Returns the value that indicates whether analysing property contains AbstractUnionEntity instance in dot-notation.
     *
     * @return
     */
    public boolean isAbstractUnionProperty() {
	if (propertyTypes == null || propertyTypes.length == 0) {
	    return false;
	}
	for (final Class<?> propertyType : propertyTypes) {
	    if (propertyType != null && AbstractUnionEntity.class.isAssignableFrom(propertyType)) {
		return true;
	    }
	}
	return false;
    }

}