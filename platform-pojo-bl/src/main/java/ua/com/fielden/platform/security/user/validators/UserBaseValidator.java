package ua.com.fielden.platform.security.user.validators;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;

/**
 * Ensures the validity of changes to property <code>base</code> of type {@link User}.
 * 
 * @author TG Team
 *
 */
public class UserBaseValidator extends AbstractBeforeChangeEventHandler<Boolean> {

    @Override
    public Result handle(final MetaProperty<Boolean> property, final Boolean newValue, final Set<Annotation> mutatorAnnotations) {
        final User user = (User) property.getEntity();
        if (!newValue && User.system_users.isOneOf(user)) {
            return failure(format("User [%s] is an application built-in account and should remain a base user.", user.getKey()));

        }
        return successful(newValue);
    }

}
