package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.annotation.CritOnly.Type.MULTI;
import static ua.com.fielden.platform.entity.annotation.CritOnly.Type.SINGLE;

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
    @Title("String Prop")
    private String stringProp;
    
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
    
    @IsProperty(TgCentreDiffSerialisation.class)
    @Title("Property Descriptor Prop")
    private PropertyDescriptor<TgCentreDiffSerialisation> propertyDescriptorProp;
    
    @IsProperty(TgCentreDiffSerialisationPersistentChild.class)
    @Title("Property Descriptor Prop")
    @CritOnly(SINGLE)
    private PropertyDescriptor<TgCentreDiffSerialisationPersistentChild> propertyDescriptorPropCritSingle;
    
    @Observable
    public TgCentreDiffSerialisation setPropertyDescriptorPropCritSingle(final PropertyDescriptor<TgCentreDiffSerialisationPersistentChild> propertyDescriptorPropCritSingle) {
        this.propertyDescriptorPropCritSingle = propertyDescriptorPropCritSingle;
        return this;
    }
    
    public PropertyDescriptor<TgCentreDiffSerialisationPersistentChild> getPropertyDescriptorPropCritSingle() {
        return propertyDescriptorPropCritSingle;
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
    
    @Observable
    public TgCentreDiffSerialisation setStringProp(final String stringProp) {
        this.stringProp = stringProp;
        return this;
    }
    
    public String getStringProp() {
        return stringProp;
    }
    
}