package ua.com.fielden.platform.swing.review;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import ua.com.fielden.platform.reflection.AnnotationReflector;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.Reflector;

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

    /**
     * Creates new {@link DynamicCriteriaPropertyAnalyser} instance for the specified dot notation property and it's declaring class.
     * 
     * @param declaringType
     * @param dotNotationExp
     */
    public DynamicCriteriaPropertyAnalyser(final Class<?> declaringType, final String dotNotationExp) {
        setAnalyseProperty(declaringType, dotNotationExp);
    }

    /**
     * Default constructor. Sets all parameters to null.
     */
    public DynamicCriteriaPropertyAnalyser() {
        propertyNames = null;
        propertyTypes = null;
        propertyFields = null;
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
            if (AnnotationReflector.isAnnotationPresent(propertyField, annotationClass)) {
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
