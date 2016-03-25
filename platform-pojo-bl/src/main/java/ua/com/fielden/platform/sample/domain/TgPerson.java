package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DeactivatableDependencies;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.security.user.User;

/**
 * Represents a person.
 *
 * @author TG team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Initials", desc = "Person's initials, must represent the person uniquely - e.g. a number may be required if there are many people with the same initials.")
@DescTitle(value = "Full Name", desc = "Person's full name - e.g. the first name followed by the middle initial followed by the surname.")
@MapEntityTo("CRAFT")
@CompanionObject(ITgPerson.class)
@DeactivatableDependencies({TgAuthoriser.class, TgOriginator.class})
public class TgPerson extends ActivatableAbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Invisible
    @MapTo
    @Title(value = "User", desc = "An application user associated with the current person.")
    private User user;

    @Observable
    public TgPerson setUser(final User user) {
        this.user = user;
        return this;
    }

    public User getUser() {
        return user;
    }

    /** A convenient method to identify whether the current person instance has an application user. */
    public boolean isUser() {
        return getUser() != null;
    }

    @Override
    @Observable
    public TgPerson setActive(final boolean active) {
        super.setActive(active);
        return this;
    }

}
