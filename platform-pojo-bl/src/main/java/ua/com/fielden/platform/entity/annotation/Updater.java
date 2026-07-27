package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Annotates a retriever to indicate that its purpose is the update of existing data, rather than initial population of data.
///
/// @author TG Team
///
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface Updater {
}
