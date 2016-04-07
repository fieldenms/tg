package ua.com.fielden.platform.security.user;

import static java.lang.String.format;
import static ua.com.fielden.platform.property.validator.StringValidator.regexProp;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.Unique;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.annotation.mutator.StrParam;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.property.validator.EmailValidator;
import ua.com.fielden.platform.property.validator.StringValidator;

/**
 * Represents the system-wide concept of a user. So, this is a system user, which should be used by system security as well as for implementing any specific customer personnel
 * requirements.
 *
 * @author TG Team
 */
@KeyTitle("Application User")
@KeyType(String.class)
@MapEntityTo
@CompanionObject(IUser.class)
public class User extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    /**
     * This is an enumeration for listing all system in-built accounts.
     */
    public enum system_users {
        SU;

        public boolean matches(final User user) {
            if (user == null) {
                return false;
            }
            return name().equalsIgnoreCase(user.getKey());
        }

        public boolean matches(final String username) {
            if (username == null) {
                return false;
            }
            return name().equalsIgnoreCase(username);
        }

        public static boolean isOneOf(final User user) {
            return SU.matches(user);
        }

        public static boolean isOneOf(final String username) {
            return SU.matches(username);
        }
    }
    
    public static final String passwordResetUuidSeperator = "-";
    public static final String usernameRegex = "^[^-]+$"; // passwordResetUuidSeperator should not be permitted for user names
    
    @IsProperty
    @MapTo
    @BeforeChange(@Handler(value = StringValidator.class, str = {@StrParam(name = regexProp, value = usernameRegex)}))
    private String key;
    
    @IsProperty
    @Invisible
    @MapTo(length = 255)
    @Title(desc = "A hash code of the actual password that only the user should know")
    private String password;

    @IsProperty(value = UserAndRoleAssociation.class, linkProperty = "user")
    @Title(value = "Roles", desc = "The associated with this user roles.")
    private final Set<UserAndRoleAssociation> roles = new HashSet<UserAndRoleAssociation>();

    @IsProperty
    @Title(value = "Is base user?", desc = "Indicates whether this is a base user, which is used for application configuration and creation of other application users.")
    @MapTo
    private boolean base = false;

    @IsProperty
    @Title(value = "Base user", desc = "A user on which the current user is based. This mainly relates to the application configuration and security user roles.")
    @MapTo
    private User basedOnUser;

    @IsProperty
    @MapTo
    @Title(value = "Email", desc = "User email, which is used for password resets")
    @Unique
    @BeforeChange(@Handler(EmailValidator.class))
    private String email;

    @IsProperty
    @MapTo
    @Invisible
    @Unique // UUID gets generated and hashed, thus should be algorithmically unique, but just in case let's enforce it at the model level
    @Title(value = "Reset UUID", desc = "The hash of the password reset request UUID")
    private String resetUuid;

    @IsProperty
    @MapTo
    @Invisible
    @Unique // salt gets generated randomly for every user and needs to be unique
    @Title(value = "Salt", desc = "Random password hashing salt to protect agains the rainbow table attack.")
    private String salt;

    @Observable
    public User setSalt(final String salt) {
        this.salt = salt;
        return this;
    }

    public String getSalt() {
        return salt;
    }
    
    @Observable
    public User setResetUuid(final String resetUuid) {
        this.resetUuid = resetUuid;
        return this;
    }

    public String getResetUuid() {
        return resetUuid;
    }
    
    @Observable
    public User setEmail(final String email) {
        this.email = email;
        return this;
    }

    public String getEmail() {
        return email;
    }
    
    
    public String getKey() {
        return key;
    }

    @Observable
    @Override
    public User setKey(final String value) {
        if (isPersisted() && (system_users.SU.matches(getKey()) && !system_users.SU.matches(value))) {
            throw Result.failure(format("User %s is an application built-in account and cannot be renamed.", getKey()));
        }

        this.key = value;

        if (system_users.isOneOf(value)) {
            setBase(true);
        }

        return this;
    }

    public String getPassword() {
        return password;
    }

    @Observable
    public User setPassword(final String password) {
        this.password = password;
        return this;
    }

    public Set<UserAndRoleAssociation> getRoles() {
        return Collections.unmodifiableSet(roles);
    }

    @Observable
    public User setRoles(final Set<UserAndRoleAssociation> roles) {
        this.roles.clear();
        this.roles.addAll(roles);
        return this;
    }

    /**
     * A convenient method for extracting {@link UserRole} instances from a set of {@link UserAndRoleAssociation}.
     *
     * @return
     */
    public Set<UserRole> roles() {
        return this.roles.stream().map(item -> item.getUserRole()).collect(Collectors.toSet());
    }

    
    public boolean isBase() {
        return base;
    }

    @Observable
    public User setBase(final boolean base) {
        this.base = base;
        if (base) {
            setBasedOnUser(null);
        } else if (system_users.isOneOf(this)) {
            throw Result.failure(format("User %s is an application built-in account and should remain a base user.", getKey()));
        }
        getProperty("basedOnUser").setRequired(!base);
        return this;
    }

    public User getBasedOnUser() {
        return basedOnUser;
    }

    @Observable
    public User setBasedOnUser(final User basedOnUser) {
        if (basedOnUser == this) {
            throw new Result(this, new IllegalArgumentException("Self reference is not permitted."));
        }

        if (basedOnUser != null && system_users.isOneOf(this)) {
            throw Result.failure(format("User %s is an application built-in account and cannot have a base user.", getKey()));
        }

        if (basedOnUser != null && !basedOnUser.isBase()) {
            throw Result.failure(format("User %s is not a base user and thus cannot be used for inheritance.", basedOnUser.getKey()));
        }

        this.basedOnUser = basedOnUser;
        if (basedOnUser != null) {
            setBase(false);
        }
        return this;
    }

    @Override
    protected Result validate() {
        final Result superResult = super.validate();
        if (!superResult.isSuccessful()) {
            return superResult;
        }

        return superResult;
    }
}