package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Annotates an entity type to indicate that its companion object is dynamically generated instead of existing in source code.
///
/// @see CompanionObject
///
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface CompanionIsGenerated {}
