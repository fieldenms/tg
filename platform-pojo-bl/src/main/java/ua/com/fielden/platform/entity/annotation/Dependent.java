package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Should be used to indicate the "dependent" properties for the property represented by the field. For e.g. :
 * 
 * <pre>
 * 	&#064;IsProperty
 * 	&#064;Dependent('dependentProperty') 
 * 	private PropertyType1 propertyWithSingleDependence;
 * 	...
 * 
 *   	&#064;IsProperty
 * 	&#064;Dependent( { 'dependentProperty1', 'dependentProperty2', ... } ) 
 * 	private PropertyType2 propertyWithMultipleDependencies;
 * 	...
 * 
 * </pre>
 * 
 * @author Jhou
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Dependent {
    /**
     * This setting should be used to specify dependent property's names.
     * 
     * @return
     */
    String[] value();
}
