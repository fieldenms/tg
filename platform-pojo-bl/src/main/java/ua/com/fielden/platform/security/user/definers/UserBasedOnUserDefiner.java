package ua.com.fielden.platform.security.user.definers;

import ua.com.fielden.platform.entity.meta.IAfterChangeEventHandler;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.security.user.User;

/**
 * A definer to ensure reset of property <code>base</code> in {@link User} upon assignment of <code>basedOnUser</code>.
 * 
 * @author TG Team
 *
 */
public class UserBasedOnUserDefiner implements IAfterChangeEventHandler<User> {

    @Override
    public void handle(final MetaProperty<User> property, final User baseOnUser) {
        final User user = (User) property.getEntity();
        
        if (!user.isInitialising() && baseOnUser != null) {
            user.setBase(false);
        }
    }

}
