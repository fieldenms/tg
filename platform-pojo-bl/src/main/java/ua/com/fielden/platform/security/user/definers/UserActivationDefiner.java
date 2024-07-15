package ua.com.fielden.platform.security.user.definers;

import static ua.com.fielden.platform.error.Result.warning;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.security.user.User;

/**
 * A definer to ensure the requiredness of {@link User} properties upon its activation.
 * 
 * @author TG Team
 *
 */
public class UserActivationDefiner implements IAfterChangeEventHandler<Boolean> {

    @Override
    public void handle(final MetaProperty<Boolean> property, final Boolean active) {
        final User user = property.getEntity();
        user.getPropertyIfNotProxy(User.EMAIL).ifPresent(p -> p.setRequired(active));
        user.getPropertyIfNotProxy(User.ROLES).filter(p -> active && user.getRoles().isEmpty())
            .ifPresent(p -> property.setDomainValidationResult(warning("Don't forget to assign roles to this user.")));
    }

}