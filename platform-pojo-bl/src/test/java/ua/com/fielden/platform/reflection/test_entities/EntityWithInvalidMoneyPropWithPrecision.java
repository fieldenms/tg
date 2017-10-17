package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.types.Money;

/**
 * A entity for validating definitions of numeric properties.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
public class EntityWithInvalidMoneyPropWithPrecision extends AbstractEntity<String> {

    
    @IsProperty(precision = 10)
    private Money numericMoney;
    

    @Observable
    public EntityWithInvalidMoneyPropWithPrecision setNumericMoney(final Money value) {
        this.numericMoney = value;
        return this;
    }

    public Money getNumericMoney() {
        return numericMoney;
    }
}
