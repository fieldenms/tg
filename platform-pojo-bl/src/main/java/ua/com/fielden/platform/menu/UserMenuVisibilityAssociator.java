package ua.com.fielden.platform.menu;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCollectionModification;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DenyIntrospection;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.security.user.User;

/**
 * Master entity for associating visible menu items with specific user.
 *
 * @author TG Team
 *
 */
@CompanionObject(UserMenuVisibilityAssociatorCo.class)
@DenyIntrospection
public class UserMenuVisibilityAssociator extends AbstractFunctionalEntityForCollectionModification<Long> {

    @IsProperty(User.class)
    @Title(value = "Users", desc = "Users those see the choosen menu item")
    private final Set<User> users = new LinkedHashSet<User>();

    @Observable
    protected UserMenuVisibilityAssociator setUsers(final Set<User> users) {
        this.users.clear();
        this.users.addAll(users);
        return this;
    }

    public Set<User> getUsers() {
        return Collections.unmodifiableSet(users);
    }

}
