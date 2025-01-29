package ua.com.fielden.platform.annotations.metamodel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates an entity type to indicate that its meta-model should <b>not</b> be generated.
 *
 * @author TG Team
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface WithoutMetaModel {}
