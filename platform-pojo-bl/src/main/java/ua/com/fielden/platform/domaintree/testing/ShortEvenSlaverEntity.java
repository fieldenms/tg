package ua.com.fielden.platform.domaintree.testing;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Entity for "domain tree representation" testing.
 *
 * @author TG Team
 *
 */
@KeyTitle(value = "Key title", desc = "Key desc")
@KeyType(DynamicEntityKey.class)
public class ShortEvenSlaverEntity extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    protected ShortEvenSlaverEntity() {
    }

    ////////// Range types //////////
    @IsProperty
    @CompositeKeyMember(1)
    private SlaveEntity key1;
    @IsProperty
    @CompositeKeyMember(2)
    private EvenSlaverEntity key2;

    //////////Entity type //////////
    @IsProperty
    private ShortEvenSlaverEntity entityProp;

    public SlaveEntity getKey1() {
        return key1;
    }

    @Observable
    public void setKey1(final SlaveEntity key1) {
        this.key1 = key1;
    }

    public EvenSlaverEntity getKey2() {
        return key2;
    }

    @Observable
    public void setKey2(final EvenSlaverEntity key2) {
        this.key2 = key2;
    }

    public ShortEvenSlaverEntity getEntityProp() {
        return entityProp;
    }

    @Observable
    public void setEntityProp(final ShortEvenSlaverEntity entityProp) {
        this.entityProp = entityProp;
    }
}
