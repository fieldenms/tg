package ua.com.fielden.platform.criteria.generator.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.types.Money;

/**
 * Entity for testing purposes.
 * 
 * @author TG Team
 *
 */

@KeyType(String.class)
@KeyTitle("key")
@DescTitle("desc")
public class TopLevelEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = 2041371879621946573L;

    @IsProperty
    private Integer integerProp;

    @IsProperty
    private Money moneyProp;

    @IsProperty
    private boolean booleanProp;

    @IsProperty
    private String stringProp;

    @IsProperty
    private SecondLevelEntity entityProp;

    public Integer getIntegerProp() {
	return integerProp;
    }

    @Observable
    public void setIntegerProp(final Integer integerProp) {
	this.integerProp = integerProp;
    }

    public Money getMoneyProp() {
	return moneyProp;
    }

    @Observable
    public void setMoneyProp(final Money moneyProp) {
	this.moneyProp = moneyProp;
    }

    public boolean isBooleanProp() {
	return booleanProp;
    }

    @Observable
    public void setBooleanProp(final boolean booleanProp) {
	this.booleanProp = booleanProp;
    }

    public String getStringProp() {
	return stringProp;
    }

    @Observable
    public void setStringProp(final String stringProp) {
	this.stringProp = stringProp;
    }

    public SecondLevelEntity getEntityProp() {
	return entityProp;
    }

    @Observable
    public void setEntityProp(final SecondLevelEntity entityProp) {
	this.entityProp = entityProp;
    }


}
