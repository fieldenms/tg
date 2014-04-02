package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation should be used to annotate either an entity object or its properties where some custom message should be provided when either key or the annotated property has
 * value <code>null</code>.
 * 
 * Such custom message can then be used as required whenever necessary... For example, it is used as part of property editors.
 * 
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.FIELD })
public @interface WhenNullMessage {
    String value();
}
