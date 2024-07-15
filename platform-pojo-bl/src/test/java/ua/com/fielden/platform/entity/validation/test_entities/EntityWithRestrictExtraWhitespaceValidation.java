package ua.com.fielden.platform.entity.validation.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validation.RestrictExtraWhitespaceValidator;

/**
 * Entity for testing of validator {@link RestrictExtraWhitespaceValidator}.
 *
 * @author TG Team
 */
@KeyType(String.class)
public class EntityWithRestrictExtraWhitespaceValidation extends AbstractEntity<String> {

    @IsProperty
    @BeforeChange(@Handler(RestrictExtraWhitespaceValidator.class))
    private String stringProp;

    @Observable
    public EntityWithRestrictExtraWhitespaceValidation setStringProp(final String prop) {
        this.stringProp = prop;
        return this;
    }

    public String getStringProp() {
        return stringProp;
    }

}