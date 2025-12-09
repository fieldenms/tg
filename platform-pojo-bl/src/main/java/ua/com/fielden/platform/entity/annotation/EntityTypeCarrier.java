package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Marks a property field as the *entity type carrier* in synthetic entities.
///
/// When an entity type is used as the type for an entity centre, the property annotated with this marker
/// acts as the carrier for the master entity type to be opened.
///
/// Only one property per class should be annotated with this marker.
///
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface EntityTypeCarrier {
}
