package ua.com.fielden.platform.entity.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for defining properties that should not accept any new value if it already has a not <code>null</code> value.
 * The modus operandi of this annotation is controlled by attribute <code>persystentOnly</code>.
 * If its value is <code>true</code> (the default) then the final semantics is enforced only for properties that had been persisted.
 * Otherwise, the final semantics are enforced immediately after assigning a <code>non-null</code> value to a property.
 * 
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface Final {
    public static final String ERR_REASSIGNMENT = "Reassigning a value for property [%s] in entity %s is not permitted.";
    
    /** 
     * Controls when the final semantics are enforced -- before or after (default) the assigned property value is persisted.
     * 
     * @return
     */
    boolean persistentOnly() default true;
}
