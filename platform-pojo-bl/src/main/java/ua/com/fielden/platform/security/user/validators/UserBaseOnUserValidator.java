package ua.com.fielden.platform.security.user.validators;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.utils.EntityUtils.areEqual;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.User.system_users;

/**
 * Ensures the validity of changes to property <code>basedOnUser</code> in {@link User}.
 * 
 * @author TG Team
 *
 */
public class UserBaseOnUserValidator extends AbstractBeforeChangeEventHandler<User> {
    public static final String SELF_REFERENCE_IS_NOT_PERMITTED = "Self reference is not permitted.";
    public static final String SYSTEM_BUILT_IN_ACCOUNTS_CANNOT_HAVE_BASED_ON_USER = "User [%s] is an application built-in account and cannot have a base user.";
    public static final String ONLY_BASE_USER_CAN_BE_USED_FOR_INHERITANCE = "User [%s] is not a base user and thus cannot be used for inheritance.";
    
    
    @Override
    public Result handle(final MetaProperty<User> property, final User basedOnUser, final Set<Annotation> mutatorAnnotations) {
        final User user = property.getEntity();
        
        if (basedOnUser != null && areEqual(basedOnUser, user)) {
            return failure(SELF_REFERENCE_IS_NOT_PERMITTED);
        }

        if (basedOnUser != null && system_users.isOneOf(user)) {
            return failure(format(SYSTEM_BUILT_IN_ACCOUNTS_CANNOT_HAVE_BASED_ON_USER, user.getKey()));
        }

        if (basedOnUser != null && !basedOnUser.isBase()) {
            return failure(format(ONLY_BASE_USER_CAN_BE_USED_FOR_INHERITANCE, basedOnUser.getKey()));
        }
        
        return successful(basedOnUser);
    }

}
