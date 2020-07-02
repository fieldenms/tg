/**
 *
 */
package ua.com.fielden.platform.entity.validation.test_entities;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.annotation.mutator.PropParam;
import ua.com.fielden.platform.entity.annotation.mutator.StrParam;
import ua.com.fielden.platform.entity.validation.MaxValueValidator;
import ua.com.fielden.platform.types.Money;

/**
 * Entity for testing of validators {@link MaxValueValidator} that uses {@code @PropParam}.
 * 
 * @author TG Team
 */
@KeyType(String.class)
public class EntityWithMaxValidationWithPropParam extends AbstractEntity<String> {

    @IsProperty
    @BeforeChange({@Handler(value = MaxValueValidator.class, prop = {@PropParam(name = "limitPropName", propName = "maxLimitProp")})})
    private Integer intProp;
    
    @IsProperty
    @BeforeChange({@Handler(value = MaxValueValidator.class, str = {@StrParam(name = "limit", value = "42")}, prop = {@PropParam(name = "limitPropName", propName = "maxLimitProp")})})
    private Money moneyProp;

    @IsProperty
    @Dependent({"intProp"})
    private BigDecimal maxLimitProp;
    
    @Observable
    public EntityWithMaxValidationWithPropParam setMaxLimitProp(final BigDecimal decimalProp) {
        this.maxLimitProp = decimalProp;
        return this;
    }

    public BigDecimal getMaxLimitProp() {
        return maxLimitProp;
    }

    @Observable
    public EntityWithMaxValidationWithPropParam setIntProp(final Integer intProp) {
        this.intProp = intProp;
        return this;
    }

    public Integer getIntProp() {
        return intProp;
    }

    @Observable
    public EntityWithMaxValidationWithPropParam setMoneyProp(final Money moneyProp) {
        this.moneyProp = moneyProp;
        return this;
    }

    public Money getMoneyProp() {
        return moneyProp;
    }

}
