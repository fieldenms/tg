package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.annotation.CritOnly.Type.MULTI;
import static ua.com.fielden.platform.entity.annotation.CritOnly.Type.SINGLE;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.DateOnly;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.TimeOnly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.IUtcDateTimeType;

/**
 * Entity that is used in CentreUpdaterTest for testing centre diff serialisation.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Key")
@CompanionObject(ITgCentreDiffSerialisation.class)
@DescTitle("Desc")
public class TgCentreDiffSerialisation extends AbstractEntity<String> {
    
    @IsProperty
    @Title("Date Prop")
    private Date dateProp;
    
    @IsProperty
    @Title("Date Prop Default")
    private Date datePropDefault;
    
    @IsProperty
    @Title("Date Prop Default Mnemonics")
    private Date datePropDefaultMnemonics;
    
    @IsProperty
    @Title("Date Prop Crit")
    @CritOnly//(RANGE)
    private Date datePropCrit;
    
    @IsProperty
    @Title("Date Prop Crit Single")
    @CritOnly(SINGLE)
    private Date datePropCritSingle;
    
    @IsProperty
    @Title("Date Prop Utc")
    @PersistentType(userType = IUtcDateTimeType.class)
    @MapTo
    private Date datePropUtc;
    
    @IsProperty
    @Title("Date Prop Date Only")
    @DateOnly
    private Date datePropDateOnly;
    
    @IsProperty
    @Title("Date Prop Time Only")
    @TimeOnly
    private Date datePropTimeOnly;
    
    @IsProperty
    @Title("Entity Prop")
    private TgCentreDiffSerialisationPersistentChild entityProp;
    
    @IsProperty
    @Title("Entity Prop Default")
    private TgCentreDiffSerialisationPersistentChild entityPropDefault;
    
    @IsProperty
    @Title("Entity Prop Crit")
    @CritOnly(MULTI)
    private TgCentreDiffSerialisationPersistentChild entityPropCrit;
    
    @IsProperty
    @Title("Entity Prop Crit Single")
    @CritOnly(SINGLE)
    private TgCentreDiffSerialisationPersistentChild entityPropCritSingle;
    
    @IsProperty
    @Title("Non Persistent Entity Prop Crit Single")
    @CritOnly(SINGLE)
    private TgCentreDiffSerialisationNonPersistentChild nonPersistentEntityPropCritSingle;
    
    @IsProperty(TgCentreDiffSerialisation.class)
    @Title("Property Descriptor Prop")
    private PropertyDescriptor<TgCentreDiffSerialisation> propertyDescriptorProp;
    
    @IsProperty(TgCentreDiffSerialisation.class)
    @Title("Property Descriptor Prop Crit")
    @CritOnly(MULTI)
    private PropertyDescriptor<TgCentreDiffSerialisation> propertyDescriptorPropCrit;
    
    @IsProperty(TgCentreDiffSerialisationPersistentChild.class)
    @Title("Property Descriptor Prop Crit Single")
    @CritOnly(SINGLE)
    private PropertyDescriptor<TgCentreDiffSerialisationPersistentChild> propertyDescriptorPropCritSingle;
    
    @IsProperty
    @Title("String Prop")
    private String stringProp;
    
    @IsProperty
    @Title("String Prop Default")
    private String stringPropDefault;
    
    @IsProperty
    @Title("String Prop Crit")
    @CritOnly(MULTI)
    private String stringPropCrit;
    
    @IsProperty
    @Title("String Prop Crit Single")
    @CritOnly(SINGLE)
    private String stringPropCritSingle;
    
    @IsProperty
    @Title("Boolean Prop")
    private boolean booleanProp;
    
    @IsProperty
    @Title("Boolean Prop Default")
    private boolean booleanPropDefault;
    
    @IsProperty
    @Title("Boolean Prop Crit")
    @CritOnly(MULTI)
    private boolean booleanPropCrit;
    
    @IsProperty
    @Title("Boolean Prop Crit Single")
    @CritOnly(SINGLE)
    private boolean booleanPropCritSingle;
    
    @IsProperty
    @Title("Integer Prop")
    private Integer integerProp;
    
    @IsProperty
    @Title("Integer Prop Default")
    private Integer integerPropDefault;
    
    @IsProperty
    @Title("Integer Prop Crit")
    @CritOnly(MULTI)
    private Integer integerPropCrit;
    
    @IsProperty
    @Title("Integer Prop Crit Single")
    @CritOnly(SINGLE)
    private Integer integerPropCritSingle;
    
    @IsProperty
    @Title("Long Prop")
    private Long longProp;
    
    @IsProperty
    @Title("Long Prop Default")
    private Long longPropDefault;
    
    @IsProperty
    @Title("Long Prop Crit")
    @CritOnly(MULTI)
    private Long longPropCrit;
    
    @IsProperty
    @Title("Long Prop Crit Single")
    @CritOnly(SINGLE)
    private Long longPropCritSingle;
    
    @IsProperty
    @Title("BigDecimal Prop")
    private BigDecimal bigDecimalProp;
    
    @IsProperty
    @Title("BigDecimal Prop Default")
    private BigDecimal bigDecimalPropDefault;
    
    @IsProperty
    @Title("BigDecimal Prop Crit")
    @CritOnly(MULTI)
    private BigDecimal bigDecimalPropCrit;
    
    @IsProperty
    @Title("BigDecimal Prop Crit Single")
    @CritOnly(SINGLE)
    private BigDecimal bigDecimalPropCritSingle;
    
    @IsProperty
    @Title("Money Prop")
    private Money moneyProp;
    
    @IsProperty
    @Title("Money Prop Default")
    private Money moneyPropDefault;
    
    @IsProperty
    @Title("Money Prop Crit")
    @CritOnly(MULTI)
    private Money moneyPropCrit;
    
    @IsProperty
    @Title("Money Prop Crit Single")
    @CritOnly(SINGLE)
    private Money moneyPropCritSingle;
    
    @Observable
    public TgCentreDiffSerialisation setMoneyPropCritSingle(final Money moneyPropCritSingle) {
        this.moneyPropCritSingle = moneyPropCritSingle;
        return this;
    }
    
    public Money getMoneyPropCritSingle() {
        return moneyPropCritSingle;
    }
    
    @Observable
    public TgCentreDiffSerialisation setMoneyPropCrit(final Money moneyPropCrit) {
        this.moneyPropCrit = moneyPropCrit;
        return this;
    }
    
    public Money getMoneyPropCrit() {
        return moneyPropCrit;
    }
    
    @Observable
    public TgCentreDiffSerialisation setMoneyPropDefault(final Money moneyPropDefault) {
        this.moneyPropDefault = moneyPropDefault;
        return this;
    }
    
    public Money getMoneyPropDefault() {
        return moneyPropDefault;
    }
    
    @Observable
    public TgCentreDiffSerialisation setMoneyProp(final Money moneyProp) {
        this.moneyProp = moneyProp;
        return this;
    }
    
    public Money getMoneyProp() {
        return moneyProp;
    }
    
    @Observable
    public TgCentreDiffSerialisation setBigDecimalPropCritSingle(final BigDecimal bigDecimalPropCritSingle) {
        this.bigDecimalPropCritSingle = bigDecimalPropCritSingle;
        return this;
    }
    
    public BigDecimal getBigDecimalPropCritSingle() {
        return bigDecimalPropCritSingle;
    }
    
    @Observable
    public TgCentreDiffSerialisation setBigDecimalPropCrit(final BigDecimal bigDecimalPropCrit) {
        this.bigDecimalPropCrit = bigDecimalPropCrit;
        return this;
    }
    
    public BigDecimal getBigDecimalPropCrit() {
        return bigDecimalPropCrit;
    }
    
    @Observable
    public TgCentreDiffSerialisation setBigDecimalPropDefault(final BigDecimal bigDecimalPropDefault) {
        this.bigDecimalPropDefault = bigDecimalPropDefault;
        return this;
    }
    
    public BigDecimal getBigDecimalPropDefault() {
        return bigDecimalPropDefault;
    }
    
    @Observable
    public TgCentreDiffSerialisation setBigDecimalProp(final BigDecimal bigDecimalProp) {
        this.bigDecimalProp = bigDecimalProp;
        return this;
    }
    
    public BigDecimal getBigDecimalProp() {
        return bigDecimalProp;
    }
    
    @Observable
    public TgCentreDiffSerialisation setLongPropCritSingle(final Long longPropCritSingle) {
        this.longPropCritSingle = longPropCritSingle;
        return this;
    }
    
    public Long getLongPropCritSingle() {
        return longPropCritSingle;
    }
    
    @Observable
    public TgCentreDiffSerialisation setLongPropCrit(final Long longPropCrit) {
        this.longPropCrit = longPropCrit;
        return this;
    }
    
    public Long getLongPropCrit() {
        return longPropCrit;
    }
    
    @Observable
    public TgCentreDiffSerialisation setLongPropDefault(final Long longPropDefault) {
        this.longPropDefault = longPropDefault;
        return this;
    }
    
    public Long getLongPropDefault() {
        return longPropDefault;
    }
    
    @Observable
    public TgCentreDiffSerialisation setLongProp(final Long longProp) {
        this.longProp = longProp;
        return this;
    }
    
    public Long getLongProp() {
        return longProp;
    }
    
    @Observable
    public TgCentreDiffSerialisation setIntegerPropCritSingle(final Integer integerPropCritSingle) {
        this.integerPropCritSingle = integerPropCritSingle;
        return this;
    }
    
    public Integer getIntegerPropCritSingle() {
        return integerPropCritSingle;
    }
    
    @Observable
    public TgCentreDiffSerialisation setIntegerPropCrit(final Integer integerPropCrit) {
        this.integerPropCrit = integerPropCrit;
        return this;
    }
    
    public Integer getIntegerPropCrit() {
        return integerPropCrit;
    }
    
    @Observable
    public TgCentreDiffSerialisation setIntegerPropDefault(final Integer integerPropDefault) {
        this.integerPropDefault = integerPropDefault;
        return this;
    }
    
    public Integer getIntegerPropDefault() {
        return integerPropDefault;
    }
    
    @Observable
    public TgCentreDiffSerialisation setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
        return this;
    }
    
    public Integer getIntegerProp() {
        return integerProp;
    }
    
    @Observable
    public TgCentreDiffSerialisation setBooleanPropCritSingle(final boolean booleanPropCritSingle) {
        this.booleanPropCritSingle = booleanPropCritSingle;
        return this;
    }
    
    public boolean getBooleanPropCritSingle() {
        return booleanPropCritSingle;
    }
    
    @Observable
    public TgCentreDiffSerialisation setBooleanPropCrit(final boolean booleanPropCrit) {
        this.booleanPropCrit = booleanPropCrit;
        return this;
    }
    
    public boolean getBooleanPropCrit() {
        return booleanPropCrit;
    }
    
    @Observable
    public TgCentreDiffSerialisation setBooleanPropDefault(final boolean booleanPropDefault) {
        this.booleanPropDefault = booleanPropDefault;
        return this;
    }
    
    public boolean getBooleanPropDefault() {
        return booleanPropDefault;
    }
    
    @Observable
    public TgCentreDiffSerialisation setBooleanProp(final boolean booleanProp) {
        this.booleanProp = booleanProp;
        return this;
    }
    
    public boolean getBooleanProp() {
        return booleanProp;
    }
    
    @Observable
    public TgCentreDiffSerialisation setStringPropCritSingle(final String stringPropCritSingle) {
        this.stringPropCritSingle = stringPropCritSingle;
        return this;
    }
    
    public String getStringPropCritSingle() {
        return stringPropCritSingle;
    }
    
    @Observable
    public TgCentreDiffSerialisation setStringPropCrit(final String stringPropCrit) {
        this.stringPropCrit = stringPropCrit;
        return this;
    }
    
    public String getStringPropCrit() {
        return stringPropCrit;
    }
    
    @Observable
    public TgCentreDiffSerialisation setStringPropDefault(final String stringPropDefault) {
        this.stringPropDefault = stringPropDefault;
        return this;
    }
    
    public String getStringPropDefault() {
        return stringPropDefault;
    }
    
    @Observable
    public TgCentreDiffSerialisation setStringProp(final String stringProp) {
        this.stringProp = stringProp;
        return this;
    }
    
    public String getStringProp() {
        return stringProp;
    }
    
    @Observable
    public TgCentreDiffSerialisation setPropertyDescriptorPropCritSingle(final PropertyDescriptor<TgCentreDiffSerialisationPersistentChild> propertyDescriptorPropCritSingle) {
        this.propertyDescriptorPropCritSingle = propertyDescriptorPropCritSingle;
        return this;
    }
    
    public PropertyDescriptor<TgCentreDiffSerialisationPersistentChild> getPropertyDescriptorPropCritSingle() {
        return propertyDescriptorPropCritSingle;
    }
    
    @Observable
    public TgCentreDiffSerialisation setPropertyDescriptorPropCrit(final PropertyDescriptor<TgCentreDiffSerialisation> propertyDescriptorPropCrit) {
        this.propertyDescriptorPropCrit = propertyDescriptorPropCrit;
        return this;
    }
    
    public PropertyDescriptor<TgCentreDiffSerialisation> getPropertyDescriptorPropCrit() {
        return propertyDescriptorPropCrit;
    }
    
    @Observable
    public TgCentreDiffSerialisation setPropertyDescriptorProp(final PropertyDescriptor<TgCentreDiffSerialisation> propertyDescriptorProp) {
        this.propertyDescriptorProp = propertyDescriptorProp;
        return this;
    }
    
    public PropertyDescriptor<TgCentreDiffSerialisation> getPropertyDescriptorProp() {
        return propertyDescriptorProp;
    }
    
    @Observable
    public TgCentreDiffSerialisation setNonPersistentEntityPropCritSingle(final TgCentreDiffSerialisationNonPersistentChild nonPersistentEntityPropCritSingle) {
        this.nonPersistentEntityPropCritSingle = nonPersistentEntityPropCritSingle;
        return this;
    }
    
    public TgCentreDiffSerialisationNonPersistentChild getNonPersistentEntityPropCritSingle() {
        return nonPersistentEntityPropCritSingle;
    }
    
    @Observable
    public TgCentreDiffSerialisation setEntityPropCritSingle(final TgCentreDiffSerialisationPersistentChild entityPropCritSingle) {
        this.entityPropCritSingle = entityPropCritSingle;
        return this;
    }
    
    public TgCentreDiffSerialisationPersistentChild getEntityPropCritSingle() {
        return entityPropCritSingle;
    }
    
    @Observable
    public TgCentreDiffSerialisation setEntityPropCrit(final TgCentreDiffSerialisationPersistentChild entityPropCrit) {
        this.entityPropCrit = entityPropCrit;
        return this;
    }
    
    public TgCentreDiffSerialisationPersistentChild getEntityPropCrit() {
        return entityPropCrit;
    }
    
    @Observable
    public TgCentreDiffSerialisation setEntityPropDefault(final TgCentreDiffSerialisationPersistentChild entityPropDefault) {
        this.entityPropDefault = entityPropDefault;
        return this;
    }
    
    public TgCentreDiffSerialisationPersistentChild getEntityPropDefault() {
        return entityPropDefault;
    }
    
    @Observable
    public TgCentreDiffSerialisation setEntityProp(final TgCentreDiffSerialisationPersistentChild entityProp) {
        this.entityProp = entityProp;
        return this;
    }
    
    public TgCentreDiffSerialisationPersistentChild getEntityProp() {
        return entityProp;
    }
    
    @Observable
    public TgCentreDiffSerialisation setDatePropTimeOnly(final Date datePropTimeOnly) {
        this.datePropTimeOnly = datePropTimeOnly;
        return this;
    }
    
    public Date getDatePropTimeOnly() {
        return datePropTimeOnly;
    }
    
    @Observable
    public TgCentreDiffSerialisation setDatePropDateOnly(final Date datePropDateOnly) {
        this.datePropDateOnly = datePropDateOnly;
        return this;
    }
    
    public Date getDatePropDateOnly() {
        return datePropDateOnly;
    }
    
    @Observable
    public TgCentreDiffSerialisation setDatePropUtc(final Date datePropUtc) {
        this.datePropUtc = datePropUtc;
        return this;
    }
    
    public Date getDatePropUtc() {
        return datePropUtc;
    }
    
    @Observable
    public TgCentreDiffSerialisation setDatePropCritSingle(final Date datePropCritSingle) {
        this.datePropCritSingle = datePropCritSingle;
        return this;
    }
    
    public Date getDatePropCritSingle() {
        return datePropCritSingle;
    }
    
    @Observable
    public TgCentreDiffSerialisation setDatePropCrit(final Date datePropCrit) {
        this.datePropCrit = datePropCrit;
        return this;
    }
    
    public Date getDatePropCrit() {
        return datePropCrit;
    }
    
    @Observable
    public TgCentreDiffSerialisation setDatePropDefaultMnemonics(final Date datePropDefaultMnemonics) {
        this.datePropDefaultMnemonics = datePropDefaultMnemonics;
        return this;
    }
    
    public Date getDatePropDefaultMnemonics() {
        return datePropDefaultMnemonics;
    }
    
    @Observable
    public TgCentreDiffSerialisation setDatePropDefault(final Date datePropDefault) {
        this.datePropDefault = datePropDefault;
        return this;
    }
    
    public Date getDatePropDefault() {
        return datePropDefault;
    }
    
    @Observable
    public TgCentreDiffSerialisation setDateProp(final Date dateProp) {
        this.dateProp = dateProp;
        return this;
    }
    
    public Date getDateProp() {
        return dateProp;
    }
    
    @Override
    protected boolean isEntityExistsValidationRequired(final String propertyName) {
        return "nonPersistentEntityPropCritSingle".equals(propertyName);
    }
    
}