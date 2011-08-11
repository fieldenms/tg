package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking property accessor, which could potentially be represented as a proxy instead of an actual instance. For example, Hibernate could use proxy and load the
 * actual property value only when it is being accessed.
 * 
 * This annotation should indicated to the application that a special logic should be applied for correct handling of proxies. For example, in case of Hibernate an entity owner of
 * the property should be associated with a Hibernate session when using proxy for obtaining the actual property value. In many cases, an owner instance could be detached from a
 * session, which leads to a runtime error.
 * 
 * @author 01es
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
public @interface Proxy {

}
