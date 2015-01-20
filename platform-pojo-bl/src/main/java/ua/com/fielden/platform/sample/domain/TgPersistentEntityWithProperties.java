package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.Max;
import ua.com.fielden.platform.types.Money;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgPersistentEntityWithProperties.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgPersistentEntityWithProperties extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Integer prop", desc = "Integer prop")
    private Integer integerProp;

    @IsProperty
    @MapTo
    @Title(value = "Entity prop", desc = "Entity prop")
    private TgPersistentEntityWithProperties entityProp;

    @IsProperty
    @MapTo
    @Title(value = "BigDecimal prop", desc = "BigDecimal prop")
    private BigDecimal bigDecimalProp;

    @IsProperty
    @MapTo
    @Title(value = "String prop", desc = "String prop")
    private String stringProp;

    @IsProperty
    @MapTo
    @Title(value = "Boolean prop", desc = "Boolean prop")
    private Boolean booleanProp;

    @Observable
    public TgPersistentEntityWithProperties setBooleanProp(final Boolean booleanProp) {
        this.booleanProp = booleanProp;
        return this;
    }

    public Boolean getBooleanProp() {
        return booleanProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setStringProp(final String stringProp) {
        this.stringProp = stringProp;
        return this;
    }

    public String getStringProp() {
        return stringProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setBigDecimalProp(final BigDecimal bigDecimalProp) {
        this.bigDecimalProp = bigDecimalProp;
        return this;
    }

    public BigDecimal getBigDecimalProp() {
        return bigDecimalProp;
    }

    @IsProperty
    @MapTo
    @Title(value = "Money prop", desc = "Money prop")
    private Money moneyProp;

    @Observable
    public TgPersistentEntityWithProperties setMoneyProp(final Money moneyProp) {
        this.moneyProp = moneyProp;
        return this;
    }

    public Money getMoneyProp() {
        return moneyProp;
    }

    @Observable
    @EntityExists(TgPersistentEntityWithProperties.class)
    public TgPersistentEntityWithProperties setEntityProp(final TgPersistentEntityWithProperties entityProp) {
        this.entityProp = entityProp;
        return this;
    }

    public TgPersistentEntityWithProperties getEntityProp() {
        return entityProp;
    }

    @Observable
    @Max(9999)
    // @GreaterOrEqual(-600)
    public TgPersistentEntityWithProperties setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
        return this;
    }

    public Integer getIntegerProp() {
        return integerProp;
    }

}