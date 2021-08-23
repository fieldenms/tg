package ua.com.fielden.platform.security.user.definers;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.security.user.User;

/**
 * A definer to ensure the requiredness of {@link User} properties upon changes to its property <code>base</code>.
 *
 * @author TG Team
 *
 */
public class UserBaseDefiner implements IAfterChangeEventHandler<Boolean> {

    @Override
    public void handle(final MetaProperty<Boolean> property, final Boolean base) {
        final User user = (User) property.getEntity();
        user.getPropertyIfNotProxy("basedOnUser").ifPresent(p -> p.setRequired(!base));
        user.getPropertyIfNotProxy("role").ifPresent(p -> {
            p.setRequired(base);
            p.setEditable(base);
        });

        if (!user.isInitialising()) {
            if (base) {
                user.setBasedOnUser(null);
            } else {
                user.setRole(null);
            }
        }
    }

}
