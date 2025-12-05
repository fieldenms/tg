package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.mutator.AfterChange;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.annotation.mutator.StrParam;
import ua.com.fielden.platform.entity.validation.ActivePropertyValidator;
import ua.com.fielden.platform.entity.validation.UserAlmostUniqueEmailValidator;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.property.validator.EmailValidator;
import ua.com.fielden.platform.property.validator.StringValidator;
import ua.com.fielden.platform.security.user.definers.UserActivationDefiner;
import ua.com.fielden.platform.security.user.definers.UserBaseDefiner;
import ua.com.fielden.platform.security.user.definers.UserBasedOnUserDefiner;
import ua.com.fielden.platform.security.user.definers.UserSsoOnlyDefiner;
import ua.com.fielden.platform.security.user.validators.UserBaseOnUserValidator;
import ua.com.fielden.platform.security.user.validators.UserBaseValidator;
import ua.com.fielden.platform.utils.Pair;

import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.property.validator.StringValidator.regexProp;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

/// Represents a concept of a system user, used for authentication and authorisation of users, be that a human or another information system.
///
@KeyTitle("Application User")
@KeyType(String.class)
@MapEntityTo
@CompanionObject(IUser.class)
public class User extends ActivatableAbstractEntity<String> {
    private static final Pair<String, String> entityTitleAndDesc = getEntityTitleAndDesc(User.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    public static final String ACTIVE_ROLES = "activeRoles";
    public static final String INACTIVE_ROLES = "inactiveRoles";
    public static final String BASED_ON_USER = "basedOnUser";
    public static final String BASE = "base";
    public static final String BASED_ON_USER__BASE = "basedOnUser.base";
    public static final String EMAIL = "email";
    public static final String SSO_ONLY = "ssoOnly";

    public static final String USER_NAME_REGEX = "^(?!.*" + UserSecret.SECRET_RESET_UUID_SEPERATOR + ").*$"; // match anything, but SECRET_RESET_UUID_SEPERATOR by using negative lookahead assertion

    /**
     * This is an enumeration for listing all system in-built accounts.
     */
    public enum system_users {
        SU, UNIT_TEST_USER, VIRTUAL_USER;

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
            return SU.matches(user) || UNIT_TEST_USER.matches(user) || VIRTUAL_USER.matches(user);
        }

        public static boolean isOneOf(final String username) {
            return SU.matches(username) || UNIT_TEST_USER.matches(username) || VIRTUAL_USER.matches(username);
        }
    }
    
    @IsProperty
    @MapTo
    @BeforeChange(@Handler(value = StringValidator.class, str = {@StrParam(name = regexProp, value = USER_NAME_REGEX)}))
    private String key;
    
    @IsProperty(SynUserAndRoleAssociationActive.class)
    @Title(value = "Active Roles", desc = "")
    private final Set<SynUserAndRoleAssociationActive> activeRoles = new TreeSet<>();

    @IsProperty(SynUserAndRoleAssociationInactive.class)
    @Title(value = "Inactive Roles", desc = "")
    private final Set<SynUserAndRoleAssociationInactive> inactiveRoles = new TreeSet<>();

    @IsProperty
    @Title(value = "Is base user?", desc = "Indicates whether this is a base user, which is used for application configuration and creation of other application users.")
    @MapTo
    @BeforeChange(@Handler(UserBaseValidator.class))
    @AfterChange(UserBaseDefiner.class)
    @Dependent({"email", "active"})
    private boolean base = false;

    @IsProperty
    @Title(value = "Base User", desc = "A user on which the current user is based (aka a profile user). This relates to the application configurations such as visibility of menu items and entity centre configurations.")
    @MapTo
    @BeforeChange(@Handler(UserBaseOnUserValidator.class))
    @AfterChange(UserBasedOnUserDefiner.class)
    private User basedOnUser;

    @IsProperty
    @MapTo
    @Title(value = "Email", desc = "User email, which is used for password resets")
    @BeforeChange({@Handler(EmailValidator.class), @Handler(UserAlmostUniqueEmailValidator.class)})
    @Dependent("base")
    private String email;

    @IsProperty
    @MapTo
    @Title(value = "Active?", desc = "Designates whether an entity instance is active or not.")
    @BeforeChange(@Handler(ActivePropertyValidator.class))
    @AfterChange(UserActivationDefiner.class)
    private boolean active;

    @IsProperty
    @MapTo
    @Title(value = "SSO only?", desc = "Only relevant in the SSO authentication mode. Controls the ability for users to loging with Reduced Sign-On (value false) or Signle Sign-On only (value true).")
    @AfterChange(UserSsoOnlyDefiner.class)
    private boolean ssoOnly;

    @Observable
    public User setSsoOnly(final boolean ssoOnly) {
        this.ssoOnly = ssoOnly;
        return this;
    }

    public boolean isSsoOnly() {
        return ssoOnly;
    }

    @Override
    @Observable
    public User setActive(boolean active) {
        this.active = active;
        return this;
    }
    
    @Override
    public boolean isActive() {
        return active;
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
            throw failure("User %s is an application built-in account and cannot be renamed.".formatted(getKey()));
        }

        this.key = value;

        if (system_users.isOneOf(value)) {
            setBase(true);
        }

        return this;
    }

    @Observable
    protected User setInactiveRoles(final Set<SynUserAndRoleAssociationInactive> inactiveRoles) {
        this.inactiveRoles.clear();
        this.inactiveRoles.addAll(inactiveRoles);
        return this;
    }

    public Set<SynUserAndRoleAssociationInactive> getInactiveRoles() {
        return unmodifiableSet(inactiveRoles);
    }

    @Observable
    protected User setActiveRoles(final Set<SynUserAndRoleAssociationActive> activeRoles) {
        this.activeRoles.clear();
        this.activeRoles.addAll(activeRoles);
        return this;
    }

    public Set<SynUserAndRoleAssociationActive> getActiveRoles() {
        return unmodifiableSet(activeRoles);
    }

    /// A convenient method for extracting active [UserRole] instances from a set of active [UserAndRoleAssociation]s.
    ///
    public Set<UserRole> activeRoles() {
        return this.activeRoles.stream().map(UserAndRoleAssociation::getUserRole).collect(toSet());
    }

    public boolean isBase() {
        return base;
    }

    @Observable
    public User setBase(final boolean base) {
        this.base = base;
        return this;
    }

    public User getBasedOnUser() {
        return basedOnUser;
    }

    @Observable
    public User setBasedOnUser(final User basedOnUser) {
        this.basedOnUser = basedOnUser;
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
    
    @Override
    public boolean equals(final Object obj) {
        // if "this" and "that" users are persisted and not dirty then an id-based comparison should be used
        if (obj instanceof User that && this.isPersisted() && (!this.isInstrumented() || !this.isDirty())) {
            if (that.isPersisted() && (!that.isInstrumented() || !that.isDirty())) {
                return this.getId().equals(that.getId());
            }
            
        }
        return super.equals(obj);
    }
}