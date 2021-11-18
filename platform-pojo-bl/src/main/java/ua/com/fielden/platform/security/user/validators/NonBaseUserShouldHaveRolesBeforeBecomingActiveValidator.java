package ua.com.fielden.platform.security.user.validators;

import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.error.Result.warning;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.impl.AbstractBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;

/**
 * Ensures the validity of changes to property <code>active</code> in {@link User}.
 * 
 * @author TG Team
 *
 */
public class NonBaseUserShouldHaveRolesBeforeBecomingActiveValidator extends AbstractBeforeChangeEventHandler<Boolean> {

    @Override
    public Result handle(final MetaProperty<Boolean> property, final Boolean newValue, final Set<Annotation> mutatorAnnotations) {
        final User user = (User) property.getEntity();
        if (newValue && !user.isBase() && user.getRoles().size() == 0) {
            return warning("Don't forget to assigned roles once user is saved.");

        }
        return successful(newValue);
    }

}