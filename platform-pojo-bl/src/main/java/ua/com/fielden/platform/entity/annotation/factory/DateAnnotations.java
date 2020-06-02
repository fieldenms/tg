package ua.com.fielden.platform.entity.annotation.factory;

import java.lang.annotation.Annotation;

import ua.com.fielden.platform.entity.annotation.DateOnly;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.TimeOnly;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

/**
 * Factory for date annotations.
 * 
 * @author TG Team
 *
 */
public class DateAnnotations {
    
    /**
     * Instantiates {@link DateOnly} annotation.
     * 
     * @return
     */
    public static DateOnly newDateOnlyAnnotation() {
        return new DateOnly() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DateOnly.class;
            }
        };
    }
    
    /**
     * Instantiates {@link TimeOnly} annotation.
     * 
     * @return
     */
    public static TimeOnly newTimeOnlyAnnotation() {
        return new TimeOnly() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return TimeOnly.class;
            }
        };
    }
    
    /**
     * Instantiates marker annotation for UTC date properties.
     * 
     * @return
     */
    public static PersistentType newUtcAnnotation() {
        return new PersistentType() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return PersistentType.class;
            }
            
            @Override
            public String value() {
                return "";
            }
            
            @Override
            public Class userType() {
                return IUtcDateTimeType.class;
            }
        };
    }
    
}