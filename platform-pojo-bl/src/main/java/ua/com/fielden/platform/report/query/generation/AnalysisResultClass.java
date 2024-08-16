package ua.com.fielden.platform.report.query.generation;

import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.startModification;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;

/**
 * Class for generating other classes those holds the analysis query result.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
public class AnalysisResultClass extends AbstractEntity<String> {

    /**
     * Overridden in order to support generic api for retrieving values from this analysis result class.
     * 
     */
    @Override
    public <T> T get(final String propertyName) {
        return super.get(getAnalysisPropertyName(propertyName));
    }

    /**
     * Generates the analysis query result class for the specified analysis domain tree manager.
     * 
     * @param enhancedType
     * @param adtm
     * @return
     */
    @SuppressWarnings("unchecked")
    public static AnalysisResultClassBundle<AbstractEntity<?>> generateAnalysisQueryClass(//
    final Class<? extends AbstractEntity<?>> enhancedType, //
            final List<String> distributionProperties, //
            final List<String> aggregationProperties, //
            final List<String> usedDistributions, //
            final List<String> usedAggregations) {
        final EntityDescriptor distributionED = new EntityDescriptor(enhancedType, distributionProperties);
        final EntityDescriptor aggregationED = new EntityDescriptor(enhancedType, aggregationProperties);
        final List<NewProperty> newProperties = createNewProperties(enhancedType, usedDistributions, distributionED);
        newProperties.addAll(createNewProperties(enhancedType, usedAggregations, aggregationED));

        try {
            final Class<?> generatedClass = startModification(AnalysisResultClass.class).addProperties(newProperties.toArray(new NewProperty[0])).endModification();
            return new AnalysisResultClassBundle<>(null, (Class<AbstractEntity<?>>) generatedClass, null);
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the name of the analysis property. If the group parameter is true, then it will be distribution property, otherwise it will be aggregation property.
     * 
     * @param group
     * @param counter
     * @return
     */
    public static String getAnalysisPropertyName(final String propertyName) {
        return "_" + propertyName.replace(".", "_");
    }

    /**
     * Creates list of properties for analysis query
     * 
     * @param type
     * @param propertyNames
     * @param ed
     * @param group
     * @return
     */
    private static List<NewProperty> createNewProperties(final Class<? extends AbstractEntity<?>> type, final List<String> propertyNames, final EntityDescriptor ed) {

        final List<NewProperty> newProperties = new ArrayList<>();

        for (final String propertyName : propertyNames) {
            final String newPropertyName = getAnalysisPropertyName(propertyName);
            final Class<?> newPropertyType = StringUtils.isEmpty(propertyName) ? type : PropertyTypeDeterminator.determinePropertyType(type, propertyName);
            newProperties.add(new NewProperty(newPropertyName, newPropertyType, ed.getTitle(propertyName), ed.getDesc(propertyName), new Annotation[] { new IsPropertyAnnotation().newInstance() }));
        }

        return newProperties;
    }
}
