package ua.com.fielden.platform.security.user.definers;

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
        property.getEntity().getProperty(User.EMAIL).setRequired(active);
    }

}
