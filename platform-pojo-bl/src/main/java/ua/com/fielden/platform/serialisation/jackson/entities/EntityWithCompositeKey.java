package ua.com.fielden.platform.serialisation.jackson.entities;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Entity class used for testing.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
public class EntityWithCompositeKey extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @MapTo
    @Title(value = "Key 1", desc = "Desc")
    @CompositeKeyMember(1)
    private EmptyEntity key1;

    @IsProperty
    @MapTo
    @Title(value = "Key 2", desc = "Desc")
    @CompositeKeyMember(2)
    private BigDecimal key2;

    @Observable
    public EntityWithCompositeKey setKey2(final BigDecimal key2) {
        this.key2 = key2;
        return this;
    }

    public BigDecimal getKey2() {
        return key2;
    }

    @Observable
    public EntityWithCompositeKey setKey1(final EmptyEntity key1) {
        this.key1 = key1;
        return this;
    }

    public EmptyEntity getKey1() {
        return key1;
    }

}
