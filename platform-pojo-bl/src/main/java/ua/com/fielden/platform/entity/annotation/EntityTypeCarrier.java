package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Marker for a property field used as the entity type carrier in synthetic entities.
/// When an entity type is used as the type for an entity centre, the property annotated with this marker
/// will serve as the carrier for the entity type of the master to be opened.
/// Only one property field per class should be annotated with this marker.
///
/// @author TG Team
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface EntityTypeCarrier {
}
