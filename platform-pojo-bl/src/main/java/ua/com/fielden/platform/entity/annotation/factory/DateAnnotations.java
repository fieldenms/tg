package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.entity.annotation.DateOnly;
import ua.com.fielden.platform.entity.annotation.DependentTimeZoneMode;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.TimeOnly;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

import java.lang.annotation.Annotation;

/// Factory for date annotations.
///
public class DateAnnotations {
    
    /// Instantiates [DateOnly] annotation.
    ///
    public static DateOnly newDateOnlyAnnotation() {
        return new DateOnly() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DateOnly.class;
            }
        };
    }
    
    /// Instantiates [TimeOnly] annotation.
    ///
    public static TimeOnly newTimeOnlyAnnotation() {
        return new TimeOnly() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return TimeOnly.class;
            }
        };
    }
    
    /// Instantiates marker annotation for UTC date properties.
    ///
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

    /// Instantiates [DependentTimeZoneMode] annotation.
    ///
    public static DependentTimeZoneMode newDependentTimeZoneModeAnnotation() {
        return new DependentTimeZoneMode() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DependentTimeZoneMode.class;
            }
        };
    }

}