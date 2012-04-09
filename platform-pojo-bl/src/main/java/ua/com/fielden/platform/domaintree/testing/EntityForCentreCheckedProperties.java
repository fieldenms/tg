package ua.com.fielden.platform.domaintree.testing;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.types.Money;

/**
 * Entity for "domain tree representation" testing.
 *
 * @author TG Team
 *
 */
@KeyTitle(value = "Key title", desc = "Key desc")
@DescTitle(value = "Desc title", desc = "Desc desc")
@KeyType(String.class)
public class EntityForCentreCheckedProperties extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    protected EntityForCentreCheckedProperties() {
    }

    @IsProperty
    private Integer integerProp = null;

    public Integer getIntegerProp() {
        return integerProp;
    }
    @Observable
    public void setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
    }

    @IsProperty
    private BigDecimal bigDecimalProp = null;

    public BigDecimal getBigDecimalProp() {
        return bigDecimalProp;
    }
    @Observable
    public void setBigDecimalProp(final BigDecimal bigDecimalProp) {
        this.bigDecimalProp = bigDecimalProp;
    }

    @IsProperty
    private Money moneyProp;

    @Observable
    public EntityForCentreCheckedProperties setMoneyProp(final Money moneyProp) {
	this.moneyProp = moneyProp;
	return this;
    }

    public Money getMoneyProp() {
	return moneyProp;
    }

    @IsProperty
    private boolean booleanProp;

    @Observable
    public EntityForCentreCheckedProperties setBooleanProp(final boolean booleanProp) {
	this.booleanProp = booleanProp;
	return this;
    }

    public boolean getBooleanProp() {
	return booleanProp;
    }
}