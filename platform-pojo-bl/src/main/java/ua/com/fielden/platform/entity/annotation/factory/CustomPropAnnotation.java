package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CustomProp;

/**
 * A factory for convenient instantiation of {@link Calculated} annotations, which mainly should be used for dynamic property creation.
 * 
 * @author TG Team
 * 
 */
public class CustomPropAnnotation {
    
    public CustomProp newInstance() {
        return new CustomProp() {

            @Override
            public Class<CustomProp> annotationType() {
                return CustomProp.class;
            }
            
        };
    }
    
}
