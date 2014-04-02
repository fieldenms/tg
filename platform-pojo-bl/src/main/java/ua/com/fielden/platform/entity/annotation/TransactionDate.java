package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.joda.time.DateTime;

/**
 * Should be used to annotate properties of type {@link Date} or {@link DateTime}. The intent is to automatically assign <code>now</code> date/time value to such properties upon
 * initial saving of the corresponding entity is the value is <code>null</code>.
 * 
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface TransactionDate {
}
