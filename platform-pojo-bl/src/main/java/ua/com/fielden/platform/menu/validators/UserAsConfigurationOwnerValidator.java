package ua.com.fielden.platform.menu.validators;

import static java.lang.String.format;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;

import java.lang.annotation.Annotation;
import java.util.Set;

import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.validation.IBeforeChangeEventHandler;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.menu.WebMenuItemInvisibility;
import ua.com.fielden.platform.security.user.User;

/**
 * A validator for property <code>owner</owner> in entity {@link WebMenuItemInvisibility} and other that pertain to UI configurations of centres, locators and masters.
 *
 * @author TG Team
 *
 */
public class UserAsConfigurationOwnerValidator implements IBeforeChangeEventHandler<User> {

    public static final String ERR_USER_IS_A_BASE_USER = "User [%s] is a base user.";

    @Override
    public Result handle(final MetaProperty<User> property, final User newValue, final Set<Annotation> mutatorAnnotations) {
        return newValue.isBase() ? failure(format(ERR_USER_IS_A_BASE_USER, newValue)) : successful(newValue);
    }

}