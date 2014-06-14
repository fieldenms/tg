package ua.com.fielden.platform.domaintree.testing;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.types.Money;

/**
 * Entity for "included properties logic" testing.
 *
 * @author TG Team
 *
 */
@KeyTitle(value = "Key title", desc = "Key desc")
@KeyType(DynamicEntityKey.class)
public class SlaveEntityForIncludedPropertiesLogic extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @CompositeKeyMember(1)
    @IsProperty
    private MasterEntityForIncludedPropertiesLogic keyPartProp;

    @IsProperty
    private Money moneyProp = null;

    @CompositeKeyMember(2)
    @IsProperty
    private Integer integerProp = null;

    public MasterEntityForIncludedPropertiesLogic getKeyPartProp() {
        return keyPartProp;
    }

    @Observable
    public void setKeyPartProp(final MasterEntityForIncludedPropertiesLogic keyPartProp) {
        this.keyPartProp = keyPartProp;
    }

    public Money getMoneyProp() {
        return moneyProp;
    }

    @Observable
    public void setMoneyProp(final Money moneyProp) {
        this.moneyProp = moneyProp;
    }

    public Integer getIntegerProp() {
        return integerProp;
    }

    @Observable
    public void setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
    }
}
