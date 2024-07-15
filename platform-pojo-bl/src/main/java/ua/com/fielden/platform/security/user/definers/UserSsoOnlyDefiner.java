package ua.com.fielden.platform.security.user.definers;

import com.google.inject.Inject;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.basic.config.IApplicationSettings.AuthMode;
import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.security.user.User;

/**
 * A definer to set editability of property {@code ssoOnly} in {@link User}.
 *
 * @author TG Team
 *
 */
public class UserSsoOnlyDefiner implements IAfterChangeEventHandler<Boolean> {

    private final boolean ssoMode;

    @Inject
    protected UserSsoOnlyDefiner(final IApplicationSettings appSettings) {
        this.ssoMode = appSettings.authMode() == AuthMode.SSO;
    }

    @Override
    public void handle(final MetaProperty<Boolean> property, final Boolean active) {
        property.getEntity().getPropertyIfNotProxy(User.SSO_ONLY).ifPresent(p -> p.setEditable(this.ssoMode));
    }

}