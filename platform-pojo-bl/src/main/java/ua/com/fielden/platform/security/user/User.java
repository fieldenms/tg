package ua.com.fielden.platform.security.user;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController2;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.error.Result;

/**
 * Represents the system-wide concept of a user. So, this is a system user, which should be used by system security as well as for implementing any specific customer personnel
 * requirements.
 * <p>
 * It is recommended that user password is encoded before setting it. It is envisaged that user password are encoded with application wide RSA private key, and thus can be decoded
 * using application wide public key.
 * <p>
 * If provided, user specific public key is used by the authentication mechanism to ensure authenticity of the request. This key cannot be used for decoding user password, because
 * a corresponding private key is not known at the application server end.
 *
 * @author TG Team
 */
@KeyTitle("Application User")
@KeyType(String.class)
@MapEntityTo(value = "CRAFT", keyColumn = "USER_NAME")
@DefaultController(IUserDao.class)
@DefaultController2(IUserDao2.class)
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

    @IsProperty
    @Invisible
    @MapTo("USER_PASSWORD")
    private String password;
    @IsProperty(value = UserAndRoleAssociation.class, linkProperty = "user")
    @MapTo("ID_CRAFT")
    private Set<UserAndRoleAssociation> roles = new HashSet<UserAndRoleAssociation>();
    @IsProperty
    @Invisible
    @MapTo("USER_PUBLIC_KEY")
    private String publicKey;
    @IsProperty
    @Title(value = "Is base user?", desc = "Indicates whether this is a base user, which is used for application configuration and creation of other application users.")
    @MapTo("IS_BASE")
    private boolean base = false;
    @IsProperty
    @Title(value = "Base user", desc = "A user on which the current user is based. This mainly relates to the application configuration and security user roles.")
    @MapTo("ID_BASE_CRAFT")
    private User basedOnUser;

    protected User() {
	this(null, null);
    }

    /**
     * Principle constructor.
     *
     * @param name
     *            -- is user's key
     * @param desc
     */
    protected User(final String name, final String desc) {
	super(null, name, desc);
    }

    @Observable
    @NotNull
    @Override
    public User setKey(final String value) {
	if (isPersisted() && (system_users.SU.matches(getKey()) && !system_users.SU.matches(value))) {
	    throw new Result(this, new IllegalArgumentException("User " + getKey() + " is an application built-in account and cannot be renamed."));
	}

	super.setKey(value);

	if (system_users.isOneOf(value)) {
	    setBase(true);
	}

	return this;
    }

    public String getPassword() {
	return password;
    }

    @Observable
    @NotNull
    public User setPassword(final String password) {
	this.password = password;
	return this;
    }

    public Set<UserAndRoleAssociation> getRoles() {
	return roles;
    }

    /**
     * A convenient method for extracting {@link UserRole} instances from a set of {@link UserAndRoleAssociation}.
     *
     * @return
     */
    public List<UserRole> roles() {
	final SortedSet<UserRole> result = new TreeSet<UserRole>();
	for (final UserAndRoleAssociation assoc : roles) {
	    result.add(assoc.getUserRole());
	}
	return new ArrayList<UserRole>(result);
    }

    @Observable
    public User setRoles(final Set<UserAndRoleAssociation> roles) {
	this.roles = roles;
	return this;
    }

    public String getPublicKey() {
	return publicKey;
    }

    @Observable
    public User setPublicKey(final String publicKey) {
	this.publicKey = publicKey;
	return this;
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
	    throw new Result(this, new IllegalArgumentException("User " + getKey() + " is an application built-in account and should remain a base user."));
	}
	getProperty("basedOnUser").setRequired(!base);
	return this;
    }

    public User getBasedOnUser() {
	return basedOnUser;
    }

    @Observable
    @EntityExists(User.class)
    public User setBasedOnUser(final User basedOnUser) {
	if (basedOnUser == this) {
	    throw new Result(this, new IllegalArgumentException("Self reference is not permitted."));
	}

	if (basedOnUser != null && system_users.isOneOf(this)) {
	    throw new Result(this, new IllegalArgumentException("User " + getKey() + " is an application built-in account and cannot have a base user."));
	}

	if (basedOnUser != null && !basedOnUser.isBase()) {
	    throw new Result(this, new IllegalArgumentException("User " + basedOnUser.getKey() + " is not a base user and thus cannot be used for inheritance."));
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