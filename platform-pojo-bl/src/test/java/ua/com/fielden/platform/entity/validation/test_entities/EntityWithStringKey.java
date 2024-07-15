package ua.com.fielden.platform.entity.validation.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validators.RudeValidator;

/**
 * Test entity type with key of type {@link String}.
 *
 * @author TG Team
 */
@KeyType(String.class)
public class EntityWithStringKey extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @BeforeChange(@Handler(RudeValidator.class))
    private String key;

    @Override
    @Observable
    public EntityWithStringKey setKey(final String key) {
        this.key = key;
        return this;
    }

    @Override
    public String getKey() {
        return key;
    }

}
