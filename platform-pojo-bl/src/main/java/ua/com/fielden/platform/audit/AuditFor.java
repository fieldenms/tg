package ua.com.fielden.platform.audit;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.lang.annotation.*;

/// This annotation is applied to audit types to indicate their corresponding audited entities.
///
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface AuditFor {

    /// The audited entity type.
    ///
    Class<? extends AbstractEntity<?>> value();

    /// The version of this persistent audit type, [#NO_VERSION] otherwise.
    ///
    int version() default NO_VERSION;

    int NO_VERSION = 0;

}
