package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Should be used to indicate if entity property should contain an upper cased value, which is most commonly used during UI development and is really applicable only to string
 * properties. For convenience it is considered that AbstractEntity property <code>key</code> is always upper cased.
 * 
 * @author 01es
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface UpperCase {
}
