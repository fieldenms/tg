package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.SkipEntityExistsValidation;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.GreaterOrEqual;
import ua.com.fielden.platform.security.user.UserSecret;

/**
 * Entity that is used for testing of .
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class EntityWithUserSecret extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title("Secret")
    @SkipEntityExistsValidation
    private UserSecret secret;

    @Observable
    @GreaterOrEqual(34)
    public EntityWithUserSecret setSecret(final UserSecret prop) {
        this.secret = prop;
        return this;
    }

    public UserSecret getSecret() {
        return secret;
    }

}
