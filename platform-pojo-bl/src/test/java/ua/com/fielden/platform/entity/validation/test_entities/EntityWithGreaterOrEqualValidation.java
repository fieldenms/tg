/**
 *
 */
package ua.com.fielden.platform.entity.validation.test_entities;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.mutator.BeforeChange;
import ua.com.fielden.platform.entity.annotation.mutator.Handler;
import ua.com.fielden.platform.entity.annotation.mutator.StrParam;
import ua.com.fielden.platform.entity.validation.GreaterOrEqualValidator;
import ua.com.fielden.platform.entity.validation.GreaterValidator;
import ua.com.fielden.platform.entity.validation.MaxValueValidator;
import ua.com.fielden.platform.types.Money;

/**
 * Entity for testing of validators {@link MaxValueValidator} and {@link GreaterValidator}.
 *
 * @author TG Team
 */
@KeyType(String.class)
public class EntityWithGreaterOrEqualValidation extends AbstractEntity<String> {

    @IsProperty
    @BeforeChange(@Handler(value = GreaterOrEqualValidator.class, str = { @StrParam(name = "limit", value = "0") }))
    private Integer intProp;

    @IsProperty
    @BeforeChange(@Handler(value = GreaterOrEqualValidator.class, str = { @StrParam(name = "limit", value = "0.50") }))
    private BigDecimal decimalProp;

    @IsProperty
    @BeforeChange(@Handler(value = GreaterOrEqualValidator.class, str = { @StrParam(name = "limit", value = "-1.50") }))
    private Money moneyProp;

    @Observable
    public EntityWithGreaterOrEqualValidation setMoneyProp(final Money moneyProp) {
        this.moneyProp = moneyProp;
        return this;
    }

    public Money getMoneyProp() {
        return moneyProp;
    }

    @Observable
    public EntityWithGreaterOrEqualValidation setDecimalProp(final BigDecimal decimalProp) {
        this.decimalProp = decimalProp;
        return this;
    }

    public BigDecimal getDecimalProp() {
        return decimalProp;
    }

    @Observable
    public EntityWithGreaterOrEqualValidation setIntProp(final Integer intProp) {
        this.intProp = intProp;
        return this;
    }

    public Integer getIntProp() {
        return intProp;
    }

}
