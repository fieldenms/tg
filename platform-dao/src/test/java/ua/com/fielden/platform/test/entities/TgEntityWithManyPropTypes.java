package ua.com.fielden.platform.test.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;
import ua.com.fielden.platform.sample.domain.EntityOne;
import ua.com.fielden.platform.sample.domain.UnionEntity;
import ua.com.fielden.platform.sample.domain.UnionEntityDetails;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * An entity for testing purposes, which contains properties with all possible types.
 * The initial intent for this entity was be used for testing batch-insert operations to ensure that properties of any supported type can be saved correctly.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@MapEntityTo
@CompanionObject(TgEntityWithManyPropTypesCo.class)
public class TgEntityWithManyPropTypes extends AbstractEntity<String> {

    public enum Property implements IConvertableToPath {
        unionEntityDetails;

        @Override public String toPath() { return name(); }
    }

    @IsProperty
    @MapTo
    private EntityOne entityProp;

    @IsProperty
    @MapTo
    private UnionEntity unionProp;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation
    private UnionEntity unionProp2;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation(skipActiveOnly = true, skipNew = false)
    private UnionEntity unionProp3;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation(skipActiveOnly = false, skipNew = true)
    private UnionEntity unionProp4;

    @IsProperty
    @MapTo
    @SkipEntityExistsValidation(skipActiveOnly = true, skipNew = true)
    private UnionEntity unionProp5;

    @IsProperty
    @MapTo
    private String stringProp;

    @IsProperty
    @MapTo
    private Hyperlink hyperlinkProp;
    
    @IsProperty
    @MapTo
    @PersistentType(userType = IUtcDateTimeType.class)
    private Date utcDateProp;
    
    @IsProperty
    @MapTo
    private Date dateProp;

    @IsProperty
    @MapTo
    private Class<?> classProperty;

    @IsProperty
    @MapTo
    private boolean booleanProp;
    
    @IsProperty(precision = 10, scale = 3)
    @MapTo
    private BigDecimal bigDecimalProp;

    @IsProperty
    @MapTo
    private Integer integerProp;
    
    @IsProperty
    @MapTo
    private Long longProp;
    
    @IsProperty
    @MapTo
    private Colour colourProp;
    
    @IsProperty(TgEntityWithManyPropTypes.class)
    @MapTo
    private PropertyDescriptor<TgEntityWithManyPropTypes> propertyDescriptorProp;

    @IsProperty
    @MapTo
    @PersistentType(userType = IMoneyType.class)
    private Money moneyUserTypeProp;
    
    @IsProperty
    @MapTo
    @PersistentType(userType = IMoneyWithTaxAmountType.class)
    private Money moneyWithTaxAmountUserTypeProp;

    @IsProperty
    @MapTo
    @PersistentType(userType = ISimpleMoneyType.class)
    private Money simpleMoneyTypeProp;

    @IsProperty
    @MapTo
    @PersistentType(userType = ISimplyMoneyWithTaxAmountType.class)
    private Money simplyMoneyWithTaxAmountProp;

    @IsProperty
    @MapTo
    @PersistentType(userType = ISimplyMoneyWithTaxAndExTaxAmountType.class)
    private Money simplyMoneyWithTaxAndExTaxAmountTypeProp;

    @IsProperty
    @MapTo
    private UnionEntityDetails unionEntityDetails;

    public UnionEntityDetails getUnionEntityDetails() {
        return unionEntityDetails;
    }

    @Observable
    public TgEntityWithManyPropTypes setUnionEntityDetails(final UnionEntityDetails unionEntityDetails) {
        this.unionEntityDetails = unionEntityDetails;
        return this;
    }

    @Observable
    public TgEntityWithManyPropTypes setSimpleMoneyTypeProp(final Money simpleMoneyTypeProp) {
        this.simpleMoneyTypeProp = simpleMoneyTypeProp;
        return this;
    }

    public Money getSimpleMoneyTypeProp() {
        return simpleMoneyTypeProp;
    }

    @Observable
    public TgEntityWithManyPropTypes setSimplyMoneyWithTaxAndExTaxAmountTypeProp(final Money simplyMoneyWithTaxAndExTaxAmountTypeProp) {
        this.simplyMoneyWithTaxAndExTaxAmountTypeProp = simplyMoneyWithTaxAndExTaxAmountTypeProp;
        return this;
    }

    public Money getSimplyMoneyWithTaxAndExTaxAmountTypeProp() {
        return simplyMoneyWithTaxAndExTaxAmountTypeProp;
    }

    @Observable
    public TgEntityWithManyPropTypes setMoneyWithTaxAmountUserTypeProp(final Money moneyWithTaxAmountUserTypeProp) {
        this.moneyWithTaxAmountUserTypeProp = moneyWithTaxAmountUserTypeProp;
        return this;
    }

    public Money getMoneyWithTaxAmountUserTypeProp() {
        return moneyWithTaxAmountUserTypeProp;
    }

    @Observable
    public TgEntityWithManyPropTypes setMoneyUserTypeProp(final Money moneyUserTypeProp) {
        this.moneyUserTypeProp = moneyUserTypeProp;
        return this;
    }

    public Money getMoneyUserTypeProp() {
        return moneyUserTypeProp;
    }
    
    @Observable
    public TgEntityWithManyPropTypes setSimplyMoneyWithTaxAmountProp(final Money simplyMoneyWithTaxAmountProp) {
        this.simplyMoneyWithTaxAmountProp = simplyMoneyWithTaxAmountProp;
        return this;
    }

    public Money getSimplyMoneyWithTaxAmountProp() {
        return simplyMoneyWithTaxAmountProp;
    }
    
    @Observable
    public TgEntityWithManyPropTypes setPropertyDescriptorProp(final PropertyDescriptor<TgEntityWithManyPropTypes> propertyDescriptorProp) {
        this.propertyDescriptorProp = propertyDescriptorProp;
        return this;
    }

    public PropertyDescriptor<TgEntityWithManyPropTypes> getPropertyDescriptorProp() {
        return propertyDescriptorProp;
    }

    @Observable
    public TgEntityWithManyPropTypes setColourProp(final Colour colourProp) {
        this.colourProp = colourProp;
        return this;
    }

    public Colour getColourProp() {
        return colourProp;
    }

    @Observable
    public TgEntityWithManyPropTypes setLongProp(final Long longProp) {
        this.longProp = longProp;
        return this;
    }

    public Long getLongProp() {
        return longProp;
    }

    @Observable
    public TgEntityWithManyPropTypes setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
        return this;
    }

    public Integer getIntegerProp() {
        return integerProp;
    }

    @Observable
    public TgEntityWithManyPropTypes setBigDecimalProp(final BigDecimal bigDecimalProp) {
        this.bigDecimalProp = bigDecimalProp;
        return this;
    }

    public BigDecimal getBigDecimalProp() {
        return bigDecimalProp;
    }

    @Observable
    public TgEntityWithManyPropTypes setBooleanProp(final boolean booleanProp) {
        this.booleanProp = booleanProp;
        return this;
    }

    public boolean getBooleanProp() {
        return booleanProp;
    }
    
    @Observable
    public TgEntityWithManyPropTypes setClassProperty(final Class<?> classProperty) {
        this.classProperty = classProperty;
        return this;
    }

    public Class<?> getClassProperty() {
        return classProperty;
    }

    public UnionEntity getUnionProp5() {
        return unionProp5;
    }

    @Observable
    public TgEntityWithManyPropTypes setUnionProp5(final UnionEntity unionProp5) {
        this.unionProp5 = unionProp5;
        return this;
    }

    public UnionEntity getUnionProp4() {
        return unionProp4;
    }

    @Observable
    public TgEntityWithManyPropTypes setUnionProp4(final UnionEntity unionProp4) {
        this.unionProp4 = unionProp4;
        return this;
    }

    public UnionEntity getUnionProp3() {
        return unionProp3;
    }

    @Observable
    public TgEntityWithManyPropTypes setUnionProp3(final UnionEntity unionProp3) {
        this.unionProp3 = unionProp3;
        return this;
    }

    public UnionEntity getUnionProp2() {
        return unionProp2;
    }

    @Observable
    public TgEntityWithManyPropTypes setUnionProp2(final UnionEntity unionProp2) {
        this.unionProp2 = unionProp2;
        return this;
    }

    @Observable
    public TgEntityWithManyPropTypes setUnionProp(final UnionEntity unionProp) {
        this.unionProp = unionProp;
        return this;
    }

    public UnionEntity getUnionProp() {
        return unionProp;
    }
    
    @Observable
    public TgEntityWithManyPropTypes setEntityProp(final EntityOne entityProp) {
        this.entityProp = entityProp;
        return this;
    }

    public EntityOne getEntityProp() {
        return entityProp;
    }

    @Observable
    public TgEntityWithManyPropTypes setStringProp(final String stringProp) {
        this.stringProp = stringProp;
        return this;
    }

    public String getStringProp() {
        return stringProp;
    }

    @Observable
    public TgEntityWithManyPropTypes setHyperlinkProp(final Hyperlink hyperlinkProp) {
        this.hyperlinkProp = hyperlinkProp;
        return this;
    }

    public Hyperlink getHyperlinkProp() {
        return hyperlinkProp;
    }

    @Observable
    public TgEntityWithManyPropTypes setUtcDateProp(final Date utcDateProp) {
        this.utcDateProp = utcDateProp;
        return this;
    }

    public Date getUtcDateProp() {
        return utcDateProp;
    }

    @Observable
    public TgEntityWithManyPropTypes setDateProp(final Date dateProp) {
        this.dateProp = dateProp;
        return this;
    }

    public Date getDateProp() {
        return dateProp;
    }
}
