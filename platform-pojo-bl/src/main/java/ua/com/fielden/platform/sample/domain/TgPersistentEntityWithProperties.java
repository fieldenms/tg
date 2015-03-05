package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
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
    @Title(value = "Integer prop", desc = "Integer prop desc")
    private Integer integerProp;

    @IsProperty
    @MapTo
    @Title(value = "Entity prop", desc = "Entity prop desc")
    private TgPersistentEntityWithProperties entityProp;

    @IsProperty
    @MapTo
    @Title(value = "BigDecimal prop", desc = "BigDecimal prop desc")
    private BigDecimal bigDecimalProp;

    @IsProperty
    @MapTo
    @Title(value = "String prop", desc = "String prop desc")
    private String stringProp;

    @IsProperty
    @MapTo
    @Title(value = "Boolean prop", desc = "Boolean prop desc")
    private Boolean booleanProp;

    @IsProperty
    @MapTo
    @Title(value = "Date prop", desc = "Date prop desc")
    private Date dateProp;

    @IsProperty
    @MapTo
    @Title(value = "Producer initialised prop", desc = "Producer initialised prop desc")
    private TgPersistentEntityWithProperties producerInitProp;

    @IsProperty
    @MapTo
    @Title(value = "Domain initialised prop", desc = "The property that was initialised directly inside Entity type definition Java class")
    private String domainInitProp = "ok";

    @IsProperty
    @MapTo
    @Title(value = "Non-conflicting prop", desc = "Non-conflicting prop desc")
    private String nonConflictingProp;

    @IsProperty
    @MapTo
    @Title(value = "Conflicting prop", desc = "Conflicting prop desc")
    private String conflictingProp;

    @IsProperty
    @MapTo
    @Title(value = "Composite prop", desc = "Composite prop desc")
    private TgPersistentCompositeEntity compositeProp;

    @IsProperty
    @MapTo
    @Title(value = "Money prop", desc = "Money prop desc")
    private Money moneyProp;

    @IsProperty
    @CritOnly(Type.SINGLE)
    @MapTo
    @Title(value = "Crit-only entity prop", desc = "Crit-only entity prop desc")
    private TgPersistentEntityWithProperties critOnlyEntityProp;

    @Observable
    public TgPersistentEntityWithProperties setCritOnlyEntityProp(final TgPersistentEntityWithProperties critOnlyEntityProp) {
        this.critOnlyEntityProp = critOnlyEntityProp;
        return this;
    }

    public TgPersistentEntityWithProperties getCritOnlyEntityProp() {
        return critOnlyEntityProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setCompositeProp(final TgPersistentCompositeEntity compositeProp) {
        this.compositeProp = compositeProp;
        return this;
    }

    @Override
    @Observable
    public TgPersistentEntityWithProperties setDesc(final String desc) {
        super.setDesc(desc);
        return this;
    }

    public TgPersistentCompositeEntity getCompositeProp() {
        return compositeProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setConflictingProp(final String conflictingProp) {
        this.conflictingProp = conflictingProp;
        return this;
    }

    public String getConflictingProp() {
        return conflictingProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setNonConflictingProp(final String nonConflictingProp) {
        this.nonConflictingProp = nonConflictingProp;
        return this;
    }

    public String getNonConflictingProp() {
        return nonConflictingProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setDomainInitProp(final String domainInitProp) {
        this.domainInitProp = domainInitProp;
        return this;
    }

    public String getDomainInitProp() {
        return domainInitProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setProducerInitProp(final TgPersistentEntityWithProperties producerInitProp) {
        this.producerInitProp = producerInitProp;
        return this;
    }

    public TgPersistentEntityWithProperties getProducerInitProp() {
        return producerInitProp;
    }

    @Observable
    public TgPersistentEntityWithProperties setDateProp(final Date dateProp) {
        this.dateProp = dateProp;
        return this;
    }

    public Date getDateProp() {
        return dateProp;
    }

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

    @Observable
    public TgPersistentEntityWithProperties setMoneyProp(final Money moneyProp) {
        this.moneyProp = moneyProp;
        return this;
    }

    public Money getMoneyProp() {
        return moneyProp;
    }

    @Observable
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