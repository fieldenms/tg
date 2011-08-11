package ua.com.fielden.platform.domain.tree;

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
public class EntityWithCompositeKey extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for the entity factory from TG.
     */
    protected EntityWithCompositeKey() {
	setKey(new DynamicEntityKey(this));
    }

    @CompositeKeyMember(1)
    @IsProperty
    private MasterEntity keyPartProp;

    @CompositeKeyMember(2)
    @IsProperty
    private Integer integerProp = null;

    @CompositeKeyMember(3)
    @IsProperty
    private SlaveEntity keyPartPropFromSlave;

    public MasterEntity getKeyPartProp() {
        return keyPartProp;
    }
    @Observable
    public void setKeyPartProp(final MasterEntity keyPartProp) {
        this.keyPartProp = keyPartProp;
    }

    public Integer getIntegerProp() {
        return integerProp;
    }
    @Observable
    public void setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
    }
    public SlaveEntity getKeyPartPropFromSlave() {
        return keyPartPropFromSlave;
    }
    public void setKeyPartPropFromSlave(final SlaveEntity keyPartPropFromSlave) {
        this.keyPartPropFromSlave = keyPartPropFromSlave;
    }

}
