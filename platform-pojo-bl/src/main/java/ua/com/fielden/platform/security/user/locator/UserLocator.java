package ua.com.fielden.platform.security.user.locator;

import static ua.com.fielden.platform.entity.NoKey.NO_KEY;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.NoKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.security.user.User;

/**
 * Locator entity object for {@link User}.
 *
 * @author TG Team
 *
 */
@KeyType(NoKey.class)
@CompanionObject(UserLocatorCo.class)
public class UserLocator extends AbstractFunctionalEntityWithCentreContext<NoKey> {

    protected UserLocator() {
        setKey(NO_KEY);
    }

    @IsProperty
    @SkipEntityExistsValidation(skipActiveOnly = true)
    private User user;

    @Observable
    public UserLocator setUser(final User value) {
        this.user = value;
        return this;
    }

    public User getUser() {
        return user;
    }

}