package ua.com.fielden.platform.report.query.generation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.factory.IsPropertyAnnotation;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.api.NewProperty;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;

/**
 * Class for generating other classes those holds the analysis query result.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class AnalysisResultClass extends AbstractEntity<String> {

    private static final long serialVersionUID = -5634158449611172336L;

    /**
     * Overridden in order to support generic api for retrieving values from this analysis result class.
     *
     */
    @Override
    public Object get(final String propertyName) {
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
    public static AnalysisResultClassBundle<AbstractEntity<?>> generateAnalysisQueryClass(final Class<? extends AbstractEntity<?>> enhancedType, final IAbstractAnalysisDomainTreeManager adtm){
	final Class<? extends AbstractEntity<?>> root = (Class<? extends AbstractEntity<?>>)DynamicEntityClassLoader.getOriginalType(enhancedType);
	final EntityDescriptor distributionED = new EntityDescriptor(enhancedType, adtm.getFirstTick().checkedProperties(root));
	final EntityDescriptor aggregationED = new EntityDescriptor(enhancedType, adtm.getSecondTick().checkedProperties(root));
	final List<String> distributionProperties = adtm.getFirstTick().usedProperties(root);
	final List<String> aggregationProperties = adtm.getSecondTick().usedProperties(root);
	final List<NewProperty> newProperties = createNewProperties(enhancedType, distributionProperties, distributionED);
	newProperties.addAll(createNewProperties(enhancedType, aggregationProperties, aggregationED));

	final DynamicEntityClassLoader cl = new DynamicEntityClassLoader(ClassLoader.getSystemClassLoader());

	try {
	    final Class<?> generatedClass = cl.startModification(AnalysisResultClass.class.getName()).addProperties(newProperties.toArray(new NewProperty[0])).endModification();
	    return new AnalysisResultClassBundle<>((Class<AbstractEntity<?>>)generatedClass, cl.getCachedByteArray(generatedClass.getName()), null);
	} catch (final ClassNotFoundException e) {
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
    public static String getAnalysisPropertyName(final String propertyName){
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
    @SuppressWarnings("serial")
    private static List<NewProperty> createNewProperties(final Class<? extends AbstractEntity<?>> type, final List<String> propertyNames, final EntityDescriptor ed) {

	final List<NewProperty> newProperties = new ArrayList<>();

	for(final String propertyName : propertyNames){
	    final String newPropertyName = getAnalysisPropertyName(propertyName);
	    final Class<?> newPropertyType = StringUtils.isEmpty(propertyName) ? type : PropertyTypeDeterminator.determinePropertyType(type, propertyName);
	    final List<Annotation> annotations = new ArrayList<Annotation>() {{
		add(new IsPropertyAnnotation().newInstance());
	    }};
	    newProperties.add(new NewProperty(newPropertyName, newPropertyType, false, ed.getTitle(propertyName), ed.getDesc(propertyName), annotations.toArray(new Annotation[0])));
	}

	return newProperties;
    }
}
