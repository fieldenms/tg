package ua.com.fielden.platform.reflection.test_entities;

import java.math.BigDecimal;

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
public class EntityWithNumericProps extends AbstractEntity<String> {

    @IsProperty(precision = 18, scale = 4, trailingZeros = false)
    private BigDecimal numericBigDecimal;
    
    @IsProperty
    private Integer numericInteger;
    
    @IsProperty
    private Long numericLong;
    
    @IsProperty
    private Money numericMoney;

    @Observable
    public EntityWithNumericProps setNumericMoney(final Money numericMoney) {
        this.numericMoney = numericMoney;
        return this;
    }

    public Money getNumericMoney() {
        return numericMoney;
    }

    @Observable
    public EntityWithNumericProps setNumbericLong(final Long numericLong) {
        this.numericLong = numericLong;
        return this;
    }

    public Long getNumbericLong() {
        return numericLong;
    }

    @Observable
    public EntityWithNumericProps setNumericInteger(final Integer integer) {
        this.numericInteger = integer;
        return this;
    }

    public Integer getNumericInteger() {
        return numericInteger;
    }

    @Observable
    public void setNumericBigDecimal(final BigDecimal collection) {
        this.numericBigDecimal = collection;
    }

    public BigDecimal getNumericBigDecimal() {
        return numericBigDecimal;
    }

}
