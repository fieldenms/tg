/**
 *
 */
package ua.com.fielden.platform.entity.validation.test_entities;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
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
public class EntityWithGreaterAndMaxValidations extends AbstractEntity<String> {

    @IsProperty
    @BeforeChange({@Handler(value = GreaterValidator.class, str = {@StrParam(name = "limit", value = "0")}),
                   @Handler(value = MaxValueValidator.class, str = {@StrParam(name = "limit", value = "300")})})
    private Integer intProp;
    
    @IsProperty
    @BeforeChange({@Handler(value = GreaterValidator.class, str = {@StrParam(name = "limit", value = "0.50")}),
                   @Handler(value = MaxValueValidator.class, str = {@StrParam(name = "limit", value = "1.50")})})
    private BigDecimal decimalProp;
    
    @IsProperty
    @BeforeChange({@Handler(value = GreaterValidator.class, str = {@StrParam(name = "limit", value = "-1.50")}),
                   @Handler(value = MaxValueValidator.class, str = {@StrParam(name = "limit", value = "1.50")})})
    private Money moneyProp;

    @IsProperty
    @MapTo
    @BeforeChange({@Handler(value = GreaterValidator.class, str = {@StrParam(name = "limit", value = "0.00"), @StrParam(name = "customErrorMsg", value = "Custom error message for GreaterValidator: limit is %s.")}),
                   @Handler(value = GreaterOrEqualValidator.class, str = {@StrParam(name = "limit", value = "1.00"), @StrParam(name = "customErrorMsg", value = "Custom error message for GreaterOrEqualValidator: limit is %s.")}),
                   @Handler(value = MaxValueValidator.class, str = {@StrParam(name = "limit", value = "10.50"), @StrParam(name = "customErrorMsg", value = "Custom error message for MaxValueValidator: limit is %s.")})})
    private BigDecimal decimalPropWithGreaterAndGreaterOrEqValidators;

    @Observable
    public EntityWithGreaterAndMaxValidations setDecimalPropWithGreaterAndGreaterOrEqValidators(final BigDecimal decimalPropWithGreaterAndGreaterOrEqValidators) {
        this.decimalPropWithGreaterAndGreaterOrEqValidators = decimalPropWithGreaterAndGreaterOrEqValidators;
        return this;
    }

    public BigDecimal getDecimalPropWithGreaterAndGreaterOrEqValidators() {
        return decimalPropWithGreaterAndGreaterOrEqValidators;
    }

    

    
    @Observable
    public EntityWithGreaterAndMaxValidations setMoneyProp(final Money moneyProp) {
        this.moneyProp = moneyProp;
        return this;
    }

    public Money getMoneyProp() {
        return moneyProp;
    }

    @Observable
    public EntityWithGreaterAndMaxValidations setDecimalProp(final BigDecimal decimalProp) {
        this.decimalProp = decimalProp;
        return this;
    }

    public BigDecimal getDecimalProp() {
        return decimalProp;
    }

    @Observable
    public EntityWithGreaterAndMaxValidations setIntProp(final Integer intProp) {
        this.intProp = intProp;
        return this;
    }

    public Integer getIntProp() {
        return intProp;
    }

}
