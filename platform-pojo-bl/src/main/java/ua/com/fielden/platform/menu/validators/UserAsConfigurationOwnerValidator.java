package ua.com.fielden.platform.menu.validators;

import static java.lang.String.*;
import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;

/**
 * A validator for property <code>owner</owner> in entity {@link WebMenuItemInvisibility} and other that pertain to UI configurations of centres, locators and masters.
 *
 * @author TG Team
 *
 */
public class UserAsConfigurationOwnerValidator implements IBeforeChangeEventHandler<User> {

    @Override
    public Result handle(final MetaProperty<User> property, final User newValue, final Set<Annotation> mutatorAnnotations) {
        return newValue.isBase() ? Result.successful(newValue) : Result.failure(format("User [%s] is not a base user.", newValue));
    }

}
