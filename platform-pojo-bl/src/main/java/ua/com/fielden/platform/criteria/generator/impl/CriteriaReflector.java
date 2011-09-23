package ua.com.fielden.platform.criteria.generator.impl;

import java.lang.reflect.Field;
import java.util.List;

import ua.com.fielden.platform.criteria.enhanced.CriteriaProperty;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.utils.Pair;

/**
 * Implements basic reflection functionality for query criteria class (i.e. retrieving title and description for specified property, generating property names etc.)
 * 
 * @author TG Team
 *
 */
public class CriteriaReflector {


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
     * New generated criteria property name consists of three parts: root type name, property name and suffix.
     * For example: if root = EntityType.class, property = property.anotherProperty.nestedProperty and suffix = _from, then generated property name will be - EntityType_poperty_anotherProperty_nestedProperty_from.
     * 
     * @param root - the type from which the property was taken.
     * @param propertyName - the name of the property for which criteria property name must be generated.
     * @param suffix - the additional suffix if the generated property has a pair.
     * @return
     */
    public static String generateCriteriaPropertyName(final Class<?> root, final String propertyName, final String suffix){
	return root.getSimpleName() + ("".equals(propertyName) ? "" : "_") + propertyName.replaceAll(Reflector.DOT_SPLITTER, "_") + suffix;
    }


    /**
     * 
     * @param criteriaClass
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static List<Field> getCriteriaProperties(final Class<? extends AbstractEntity> criteriaClass) {
	return Finder.findProperties(criteriaClass, IsProperty.class, CriteriaProperty.class);
    }
}
