package ua.com.fielden.platform.criteria.generator.impl;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;
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
@DefaultController(ITopLevelEntity.class)
public class TopLevelEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = 2041371879621946573L;

    @IsProperty
    @Title(value = "integer property", desc = "integer property description")
    private Integer integerProp;

    @IsProperty
    @Title(value = "money property", desc = "money property description")
    private Money moneyProp;

    @IsProperty
    @Title(value = "boolean property", desc = "boolean property description")
    private boolean booleanProp;

    @IsProperty
    @Title(value = "string property", desc = "string property description")
    private String stringProp;

    @IsProperty
    @Title(value = "entity property", desc = "entity property description")
    private SecondLevelEntity entityProp;

    @IsProperty
    @Title(value = "single entity property", desc = "single entity property description")
    @CritOnly(Type.SINGLE)
    private LastLevelEntity critSingleEntity;

    @IsProperty
    @Title(value = "range entity property", desc = "range entity property description")
    @CritOnly(Type.RANGE)
    private LastLevelEntity critRangeEntity;

    @IsProperty
    @Title(value = "single integer property", desc = "single integer property description")
    @CritOnly(Type.SINGLE)
    private Integer critISingleProperty;

    @IsProperty
    @Title(value = "range integer property", desc = "range integer property description")
    @CritOnly(Type.RANGE)
    private Integer critIRangeProperty;

    @IsProperty
    @Title(value = "single string property", desc = "single string property description")
    @CritOnly(Type.SINGLE)
    private String critSSingleProperty;

    @IsProperty
    @Title(value = "range string property", desc = "range string property description")
    @CritOnly(Type.RANGE)
    private String critSRangeProperty;

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

    public LastLevelEntity getCritSingleEntity() {
	return critSingleEntity;
    }

    @Observable
    public void setCritSingleEntity(final LastLevelEntity critSingleEntity) {
	this.critSingleEntity = critSingleEntity;
    }

    public LastLevelEntity getCritRangeEntity() {
	return critRangeEntity;
    }

    @Observable
    public void setCritRangeEntiyt(final LastLevelEntity critRangeEntity) {
	this.critRangeEntity = critRangeEntity;
    }

    public Integer getCritISingleProperty() {
	return critISingleProperty;
    }

    @Observable
    public void setCritISingleProperty(final Integer critISingleProperty) {
	this.critISingleProperty = critISingleProperty;
    }

    public Integer getCritIRangeProperty() {
	return critIRangeProperty;
    }

    @Observable
    public void setCritIRangeProperty(final Integer critIRangeProperty) {
	this.critIRangeProperty = critIRangeProperty;
    }

    public String getCritSSingleProperty() {
	return critSSingleProperty;
    }

    @Observable
    public void setCritSSingleProperty(final String critSSingleProperty) {
	this.critSSingleProperty = critSSingleProperty;
    }

    public String getCritSRangeProperty() {
	return critSRangeProperty;
    }

    @Observable
    public void setCritSRangeProperty(final String critSRangeProperty) {
	this.critSRangeProperty = critSRangeProperty;
    }
}
