package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Secrete;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.annotation.Unique;

/**
 * This is entity that represents a sensitive user information for authentication and password reset.
 * Instances of this entity should never be marshallable to the client-side -- neither as standalone instance nor as part of other entities.
 * <p>
 * Originally, the information that is now represented by this entity was a part of {@link User}. 
 * This made it too easy too leak authentication related information outside the application due to ubiquitous use of {@link User} instances in business related logic and entities.
 *
 * @author TG Team
 *
 */
@KeyTitle("User")
@KeyType(User.class)
@MapEntityTo
@CompanionObject(IUserSecret.class)
public class UserSecret extends AbstractPersistentEntity<User> {

    public static final String SECRET_RESET_UUID_SEPERATOR = "-";
    public static final int RESER_UUID_EXPIRATION_IN_MUNUTES = 15;

    @IsProperty
    @MapTo
    @Title("User")
    @SkipEntityExistsValidation
    private User key;

    @IsProperty(length = 255)
    @MapTo
    @Title(desc = "A hash code of the actual password that only the user should know")
    @Secrete
    private String password;

    @IsProperty
    @MapTo
    @Unique // salt gets generated randomly for every user and needs to be unique
    @Title(value = "Salt", desc = "Random password hashing salt to protect agains the rainbow table attack.")
    @Secrete
    private String salt;

    @IsProperty
    @MapTo
    @Unique // UUID gets generated and hashed, thus should be algorithmically unique, but just in case let's enforce it at the model level
    @Secrete
    @Title(value = "Reset UUID", desc = "The hash of the password reset request UUID")
    private String resetUuid;

    @Observable
    @Override
    public UserSecret setKey(final User key) {
        this.key = key;
        return this;
    }

    @Override
    public User getKey() {
        return key;
    }

    @Observable
    public UserSecret setPassword(final String password) {
        this.password = password;
        return this;
    }

    public String getPassword() {
        return password;
    }

    @Observable
    public UserSecret setSalt(final String salt) {
        this.salt = salt;
        return this;
    }

    public String getSalt() {
        return salt;
    }

    @Observable
    public UserSecret setResetUuid(final String resetUuid) {
        this.resetUuid = resetUuid;
        return this;
    }

    public String getResetUuid() {
        return resetUuid;
    }

}
