package ua.com.fielden.platform.menu.validators;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;

/**
 * A validator for {@link WebMenuItemInvisibility}'s owner property.
 *
 * @author TG Team
 *
 */
public class MenuItemOwnerValidator implements IBeforeChangeEventHandler<User> {

    @Override
    public Result handle(final MetaProperty<User> property, final User newValue, final User oldValue, final Set<Annotation> mutatorAnnotations) {
        if (newValue == null) {
            return Result.failure(newValue, "The owner shouldn't be null.");
        }
        if (newValue != null && !newValue.isBase()) {
            return Result.failure(newValue, "Only base users are allowed to be used for a base configuration.");
        }
        return Result.successful(newValue);
    }

}
