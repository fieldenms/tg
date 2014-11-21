package ua.com.fielden.platform.sample.domain;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.error.Result;
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
public class TgPerson extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Invisible
    @MapTo("USER_PASSWORD")
    private String password;

    @IsProperty
    @Title(value = "Username", desc = "Application user name")
    @MapTo("USER_NAME")
    private String username;

    @IsProperty
    @Invisible
    @MapTo("USER_PUBLIC_KEY")
    private String publicKey;

    @IsProperty
    @Title(value = "Is base user?", desc = "Indicates whether person is a base user, which is used for application configuration and creation of other application users.")
    @MapTo("IS_BASE")
    private boolean base = false;

    @IsProperty
    @Title(value = "Base user", desc = "A user on which the current person is based. This mainly relates to the application configuration and security user roles.")
    @MapTo("ID_BASE_CRAFT")
    private User basedOnUser;

    public String getUsername() {
        return username;
    }

    @Observable
    public TgPerson setUsername(final String username) {
        if (User.system_users.isOneOf(this.username) && !this.username.equals(username)) {
            throw new Result(this, new IllegalStateException("Person " + getKey() + " is associated with an application built-in user account, which cannot be changed."));
        }

        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    @Observable
    public TgPerson setPassword(final String password) {
        this.password = password;
        return this;
    }

    public String getPublicKey() {
        return publicKey;
    }

    @Observable
    public TgPerson setPublicKey(final String publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public boolean isBase() {
        return base;
    }

    @Observable
    public TgPerson setBase(final boolean base) {
        this.base = base;
        return this;
    }

    /** A convenient method to identify whether the current person instance is an application user. */
    public boolean isUser() {
        return !StringUtils.isEmpty(getUsername());
    }

    @Override
    protected Result validate() {
        final Result superResult = super.validate();
        if (!superResult.isSuccessful()) {
            return superResult;
        }

        // application user requires additional validation
        if (isUser()) {
            if (!isBase() && (getBasedOnUser() == null)) {
                return new Result(this, new IllegalStateException("Missing base user. Application users should have a base user specified."));
            }

            if (isBase() && (getBasedOnUser() != null)) {
                return new Result(this, new IllegalStateException("Base users cannot not be based on any other user."));
            }
        }

        return superResult;
    }

    public User getBasedOnUser() {
        return basedOnUser;
    }

    @Observable
    @EntityExists(User.class)
    public TgPerson setBasedOnUser(final User basedOnUser) {
        if ((basedOnUser != null) && !basedOnUser.isBase()) {
            throw new IllegalArgumentException("User " + basedOnUser.getKey() + " is not a base user and thus cannot be used for inheritance.");
        }
        this.basedOnUser = basedOnUser;
        return this;
    }

}
