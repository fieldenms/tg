package ua.com.fielden.platform.entity.validation.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validation.RestrictNonPrintableCharactersValidator;

/**
 * Entity for testing of validator {@link RestrictNonPrintableCharactersValidator}.
 *
 * @author TG Team
 */
@KeyType(String.class)
public class EntityWithRestrictNonPrintableCharactersValidation extends AbstractEntity<String> {

    @IsProperty
    @BeforeChange(@Handler(RestrictNonPrintableCharactersValidator.class))
    private String stringProp;

    @Observable
    public EntityWithRestrictNonPrintableCharactersValidation setStringProp(final String prop) {
        this.stringProp = prop;
        return this;
    }

    public String getStringProp() {
        return stringProp;
    }

}
