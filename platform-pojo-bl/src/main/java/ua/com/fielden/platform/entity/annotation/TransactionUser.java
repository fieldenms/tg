package ua.com.fielden.platform.entity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import ua.com.fielden.platform.security.user.User;

/**
 * Should be used to annotate properties of type {@link User} or {@link String} (in the latter case user's name is used). The intent is to automatically assign <code>user</code>
 * making the entity save request to such properties upon initial saving of a corresponding entity if the value is <code>null</code>.
 * 
 * @author TG Team
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.FIELD })
public @interface TransactionUser {
}
