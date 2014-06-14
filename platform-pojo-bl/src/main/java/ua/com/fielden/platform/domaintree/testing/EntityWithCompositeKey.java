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
public class EntityWithCompositeKey extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @CompositeKeyMember(1)
    @IsProperty
    private Integer key1;

    @CompositeKeyMember(2)
    @IsProperty
    private Integer integerProp = null;

    public Integer getIntegerProp() {
        return integerProp;
    }

    @Observable
    public void setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
    }

    public Integer getKey1() {
        return key1;
    }

    @Observable
    public void setKey1(final Integer key1) {
        this.key1 = key1;
    }
}
