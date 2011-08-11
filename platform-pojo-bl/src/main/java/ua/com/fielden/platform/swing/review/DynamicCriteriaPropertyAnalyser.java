package ua.com.fielden.platform.swing.review;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.ResultOnly;
import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.treemodel.IPropertyFilter;

/**
 * This class provides convenient API to determine whether property is available to add to the criteria or fetch model.
 * 
 * @author oleh
 * 
 */
public class DynamicCriteriaPropertyAnalyser {

    protected String propertyNames[];
    protected Field propertyFields[];
    protected Class<?> propertyTypes[];

    protected IPropertyFilter propertyFilter;

    /**
     * Creates new {@link DynamicCriteriaPropertyAnalyser} instance for the specified dot notation property and it's declaring class.
     * 
     * @param declaringType
     * @param dotNotationExp
     */
    public DynamicCriteriaPropertyAnalyser(final Class<?> declaringType, final String dotNotationExp, final IPropertyFilter propertyFilter) {
	setPropertyFilter(propertyFilter);
	setAnalyseProperty(declaringType, dotNotationExp);
    }

    /**
     * Default constructor. Sets all parameters to null.
     */
    public DynamicCriteriaPropertyAnalyser() {
	propertyNames = null;
	propertyTypes = null;
	propertyFields = null;
	propertyFilter = null;
    }

    /**
     * Set the property to analyse. The analysing property must be specified with property name path split with dot, also class, where property is declared, must be specified.
     * 
     * @param declaringType
     * @param dotNotationExp
     */
    public void setAnalyseProperty(final Class<?> declaringType, final String dotNotationExp) {
	if (declaringType == null || dotNotationExp == null) {
	    propertyNames = null;
	    propertyFields = null;
	    propertyTypes = null;
	    return;
	}
	if (isEmpty(dotNotationExp)) {
	    propertyNames = null;
	    propertyFields = null;
	    propertyTypes = new Class<?>[1];
	    propertyTypes[0] = declaringType;
	} else {
	    propertyNames = dotNotationExp.split(Reflector.DOT_SPLITTER);
	    propertyFields = new Field[propertyNames.length];
	    propertyTypes = new Class<?>[propertyNames.length + 1];
	    propertyTypes[0] = declaringType;
	    for (int wordIndex = 1; wordIndex < propertyTypes.length; wordIndex++) {
		final Class<?> propertyType = PropertyTypeDeterminator.determineClass(propertyTypes[wordIndex - 1], propertyNames[wordIndex - 1], true, true);
		propertyFields[wordIndex - 1] = Finder.getFieldByName(propertyTypes[wordIndex - 1], propertyNames[wordIndex - 1]);
		//		if (Collection.class.isAssignableFrom(propertyType)) {
		//		    propertyType = PropertyTypeDeterminator.determineCollectionElementClass(propertyFields[wordIndex - 1]);
		//		}
		propertyTypes[wordIndex] = propertyType;
	    }
	}
    }

    /**
     * Returns the analysing property.
     * 
     * @return
     */
    public String getAnalysingProperty() {
	if (propertyNames != null) {
	    String dotNotation = "";
	    for (final String propertyName : propertyNames) {
		dotNotation += "." + propertyName;
	    }
	    return dotNotation.substring(1);
	}
	if (propertyTypes == null) {
	    return null;
	}
	return "";
    }

    /**
     * Returns the class where analysing property is declared.
     * 
     * @return
     */
    public Class<?> getDeclaredClass() {
	if (propertyTypes == null) {
	    return null;
	}
	return propertyTypes[0];
    }

    /**
     * Set the {@link IPropertyFilter} instance. This property filter will be used during property visibility determination process.
     * 
     * @param propertyFilter
     */
    public void setPropertyFilter(final IPropertyFilter propertyFilter) {
	this.propertyFilter = propertyFilter;
	setAnalyseProperty(getDeclaredClass(), getAnalysingProperty());
    }

    /**
     * Returns value that indicates whether specified {@code propertyName} is among list of {@code unionProperties} fields.
     * 
     * @param propertyName
     * @param unionProperties
     * @return
     */
    protected boolean isPropertyAmongUnion(final String propertyName, final List<Field> unionProperties) {
	for (final Field field : unionProperties) {
	    if (field.getName().equals(propertyName)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Determines whether this property is visible in the entity tree.
     * 
     * @return
     */
    private boolean isPropertyIncluded() {
	if (propertyTypes == null) {
	    return true;
	}
	for (int typeIndex = 0; typeIndex < propertyTypes.length - 1; typeIndex++) {
	    if (!AbstractEntity.class.isAssignableFrom(propertyTypes[0])) {
		return false;
	    }
	}
	for (int typeIndex = 1; typeIndex < propertyTypes.length; typeIndex++) {
	    for (int previousTypeIndex = typeIndex - 1; previousTypeIndex >= 0; previousTypeIndex--) {
		if (propertyTypes[typeIndex].equals(propertyTypes[previousTypeIndex])
			&& Finder.getKeyMembers(propertyTypes[typeIndex - 1]).contains(propertyFields[typeIndex - 1])) {
		    return false;
		}
	    }
	}
	return true;
    }

    /**
     * Also uses {@link IPropertyFilter} to determines whether this property is visible.
     * 
     * @param propertyFilter
     * @return
     */
    public boolean isPropertyVisible() {
	if (!isPropertyIncluded()) {
	    return false;
	}
	if (propertyTypes == null) {
	    return true;
	}
	if (propertyTypes.length <= 1 || propertyFilter == null) {
	    return true;
	}
	for (int fieldIndex = 0; fieldIndex < propertyFields.length - 1; fieldIndex++) {
	    for (final Class<?> declaringClass : getDeclaringClasses(fieldIndex)) {
		if (propertyFilter.shouldExcludeProperty(declaringClass, propertyFields[fieldIndex])
			|| !propertyFilter.shouldBuildChildrenFor(declaringClass, propertyFields[fieldIndex])) {
		    return false;
		}
	    }
	}
	for (final Class<?> declaringClass : getDeclaringClasses(propertyFields.length - 1)) {
	    if (propertyFilter.shouldExcludeProperty(declaringClass, propertyFields[propertyFields.length - 1])) {
		return false;
	    }
	}
	return true;
    }

    /**
     * Determines whether analysing property is union property or not.
     * 
     * @return
     */
    public boolean isPropertyUnion() {
	if (propertyTypes == null) {
	    return false;
	}
	if (propertyFields.length > 1) {
	    return AbstractEntity.class.isAssignableFrom(propertyTypes[propertyTypes.length - 1])
		    && AbstractUnionEntity.class.isAssignableFrom(propertyTypes[propertyTypes.length - 2]);
	} else {
	    return false;
	}
    }

    /**
     * Determines whether criteria property can be removed or not.
     * 
     * @return
     */
    public boolean canRemoveCriteraProperty() {
	return !isMarkedWithAnnotation(CritOnly.class);
    }

    /**
     * Returns true if criteria property is visible in the criteria tree but can not be checked.
     * 
     * @return
     */
    public boolean isCriteriaPropertyAvailable() {
	if (isMarkedWithAnnotation(ResultOnly.class) || isComplexKey()) {
	    return false;
	}
	return true;
    }

    /**
     * Returns value that indicates whether criteria sub-properties for analysing property are available or not.
     * 
     * @return
     */
    public boolean isCriteriaProertyChildrenAvailable() {
	if (isMarkedWithAnnotation(ResultOnly.class)) {
	    return false;
	}
	return true;
    }

    /**
     * Returns true if criteria property is visible in the criteria tree but can not be checked.
     * 
     * @return
     */
    public boolean isFetchPropertyAvailable() {
	if (isMarkedWithAnnotation(CritOnly.class) || isSyntheticEntity() || isCollectionalProperty() || isComplexKey()) {
	    return false;
	}
	return true;
    }

    /**
     * Returns value that indicates whether property type is synthetic or not.
     * 
     * @return
     */
    private boolean isSyntheticEntity() {
	return getPropertyType() != null && Reflector.isSynthetic(getPropertyType());
    }

    /**
     * Returns value that indicates whether fetch sub-properties for analysing property are available or not.
     * 
     * @return
     */
    public boolean isFetchPropertyChildrenAvailable() {
	if (isMarkedWithAnnotation(CritOnly.class) || isCollectionalProperty()) {
	    return false;
	}
	return true;
    }

    /**
     * Returns value that indicates whether this property is Collectional or not.
     * 
     * @return
     */
    private boolean isCollectionalProperty() {
	if (propertyTypes == null || propertyFields == null) {
	    return false;
	}
	for (final Field propertyField : propertyFields) {
	    if (Collection.class.isAssignableFrom(propertyField.getType())) {
		return true;
	    }
	}
	return false;

    }

    /**
     * Returns value that indicates whether this property was marked with specified annotation.
     * 
     * @param annotationClass
     * @return
     */
    public boolean isMarkedWithAnnotation(final Class<? extends Annotation> annotationClass) {
	if (propertyTypes == null || propertyFields == null) {
	    return false;
	}
	for (final Field propertyField : propertyFields) {
	    if (propertyField.isAnnotationPresent(annotationClass)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Returns value that indicates whether the type of the last property name is AbstractEntity and whether it's key is AbstractEntity or DynamicEntityKey.
     * 
     * @return
     */
    private boolean isComplexKey() {
	if (propertyTypes == null) {
	    return false;
	}
	final Class<?> clazz = propertyTypes[propertyTypes.length - 1];
	if (AnnotationReflector.isAnnotationPresent(KeyType.class, clazz)) {
	    final Class<?> keyType = AnnotationReflector.getAnnotation(KeyType.class, clazz).value();
	    if (AbstractEntity.class.isAssignableFrom(keyType) || DynamicEntityKey.class.isAssignableFrom(keyType)) {
		return true;
	    }
	}
	return false;
    }

    /**
     * Returns the type of the analysing property.
     * 
     * @return
     */
    public Class<?> getPropertyType() {
	if (propertyTypes == null) {
	    return null;
	}
	return propertyTypes[propertyTypes.length - 1];
    }

    /**
     * Returns the declaring class for the property name specified with {@code propertyNameIndex}.
     * 
     * @param propertyNameIndex
     * @return
     */
    protected List<Class<?>> getDeclaringClasses(final int propertyNameIndex) {
	final List<Class<?>> declaringClasses = new ArrayList<Class<?>>();
	if (propertyNames == null || propertyNameIndex > propertyNames.length - 1) {
	    return null;
	}
	final Class<?> declaringClass = propertyTypes[propertyNameIndex];
	if (AbstractUnionEntity.class.isAssignableFrom(declaringClass)) {
	    final List<Field> unionProperties = AbstractUnionEntity.unionProperties((Class<AbstractUnionEntity>) declaringClass, propertyFilter);
	    final List<String> commonProperties = AbstractUnionEntity.commonProperties((Class<AbstractUnionEntity>) declaringClass, propertyFilter);
	    if (!isPropertyAmongUnion(propertyNames[propertyNameIndex], unionProperties) && commonProperties.contains(propertyNames[propertyNameIndex])) {
		for (final Field unionField : unionProperties) {
		    declaringClasses.add(unionField.getType());
		}
		return declaringClasses;
	    }
	}
	declaringClasses.add(declaringClass);
	return declaringClasses;
    }

    /**
     * Returns the property name that stands on the index position of the analysing property.
     * 
     * @param index
     * @return
     */
    public String getPropertyName(final int index) {
	return (propertyNames != null && index >= 0 && index < propertyNames.length) ? propertyNames[index] : "";
    }

    /**
     * Returns the length of the property name path length. (Property name path - that is a list of separate strings split by dot).
     * 
     * @return
     */
    public int getProertyNamePathLength() {
	return propertyNames.length;
    }

}
