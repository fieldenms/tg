package ua.com.fielden.platform.entity.validation.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.validators.RudeValidator;

/**
 * Test entity type with a key member of type {@link String}.
 *
 * @author TG Team
 */
@KeyType(DynamicEntityKey.class)
public class EntityWithStringKeyMember extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @CompositeKeyMember(1)
    @BeforeChange(@Handler(RudeValidator.class))
    private String name;

    @Observable
    public EntityWithStringKeyMember setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

}