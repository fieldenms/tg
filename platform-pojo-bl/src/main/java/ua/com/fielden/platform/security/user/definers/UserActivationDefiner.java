package ua.com.fielden.platform.security.user.definers;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.security.user.User;

import static ua.com.fielden.platform.error.Result.informativeEx;
import static ua.com.fielden.platform.error.Result.warningEx;

/// A definer to ensure the requiredness of [User] properties upon its activation.
///
public class UserActivationDefiner implements IAfterChangeEventHandler<Boolean> {

    public static final String
            WARN_MISSING_ACTIVE_ROLES = "Missing active roles.",
            WARN_EXT_MISSING_ACTIVE_ROLES = """
                                           Missing active roles.
                                           Don't forget to assign at least one active role to this user.
                                           """;

    @Override
    public void handle(final MetaProperty<Boolean> property, final Boolean active) {
        final User user = property.getEntity();
        user.getPropertyIfNotProxy(User.EMAIL).ifPresent(p -> p.setRequired(active));
        user.getPropertyIfNotProxy(User.ACTIVE_ROLES).filter(p -> active && user.getActiveRoles().isEmpty())
            .ifPresent(p -> property.setDomainValidationResult(user.isInitialising()
                                                               ? informativeEx(WARN_MISSING_ACTIVE_ROLES, WARN_EXT_MISSING_ACTIVE_ROLES)
                                                               : warningEx(WARN_MISSING_ACTIVE_ROLES, WARN_EXT_MISSING_ACTIVE_ROLES)));
    }

}