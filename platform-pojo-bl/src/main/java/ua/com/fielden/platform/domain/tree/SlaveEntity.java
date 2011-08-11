package ua.com.fielden.platform.domain.tree;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.com.fielden.platform.domain.tree.MasterEntity.EnumType;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.Ignore;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.ResultOnly;
import ua.com.fielden.platform.types.Money;

/**
 * Entity for "domain tree representation" testing.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Key title", desc = "Key desc")
@DescTitle(value = "Desc title", desc = "Desc desc")
public class SlaveEntity extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    protected SlaveEntity() {
    }

    @IsProperty
    @CompositeKeyMember(1)
    private MasterEntity masterEntityProp;

    ////////// Enum types //////////
    @IsProperty
    private EnumType enumProp = null;

    ////////// Range types //////////
    @IsProperty
    @CompositeKeyMember(2)
    private Integer integerProp = null;
    @IsProperty
    private Integer propWithFunctions = null;
    @IsProperty
    @Calculated
    private Integer calcIntegerProp = null;
    @IsProperty
    private Double doubleProp = 0.0;
    @IsProperty
    private BigDecimal bigDecimalProp = new BigDecimal(0.0);
    @IsProperty
    private BigDecimal checkedUntouchedProp = new BigDecimal(0.0);
    @IsProperty
    private BigDecimal immutablyCheckedUntouchedProp = new BigDecimal(0.0);
    @IsProperty
    private BigDecimal mutatedWithFunctionsProp = new BigDecimal(0.0);
    @IsProperty
    private EntityWithStringKeyType mutablyCheckedProp;
    @IsProperty
    private EntityWithStringKeyType mutablyManuallyCheckedProp;

    @IsProperty
    private Money moneyProp;
    @IsProperty
    private Date dateProp;

    ////////// boolean type //////////
    @IsProperty
    private boolean booleanProp = false;

    ////////// String type //////////
    @IsProperty
    private String stringProp;

    ////////// Entity type //////////
    @IsProperty
    private EvenSlaverEntity entityProp;

    ///////// Collections /////////
    @IsProperty(EvenSlaverEntity.class)
    private List<EvenSlaverEntity> collection = new ArrayList<EvenSlaverEntity>();

    ////////// Any property to be specifically excluded //////////
    @IsProperty
    private EvenSlaverEntity excludedManuallyProp;

    ////////// Any property to be specifically disabled //////////
    @IsProperty
    private Integer disabledManuallyProp;

    ////////// Any property to be specifically checked //////////
    @IsProperty
    private Integer checkedManuallyProp;
    
    ////////// Any property to be unchecked //////////
    @IsProperty
    private Integer uncheckedProp;

    ////////// A property of AbstractEntity type with 'abstract' modifier //////////
    @IsProperty
    private EntityWithAbstractNature entityPropWithAbstractNature;

    ////////// A property of AbstractEntity type without KeyType annotation //////////
    @IsProperty
    private EntityWithoutKeyType entityPropWithoutKeyType;

    ////////// A property of AbstractEntity type without KeyTitle annotation //////////
    @IsProperty
    private EntityWithoutKeyTitleAndWithKeyType entityPropWithoutKeyTitle;

    ////////// A property of AbstractEntity type with AE-typed key //////////
    @IsProperty
    private EntityWithKeyTitleAndWithAEKeyType entityPropWithAEKeyType;

    ////////// A property of entity type //////////
    @IsProperty
    private EntityWithStringKeyType simpleEntityProp;

    ////////// Invisible property //////////
    @Invisible
    @IsProperty
    private Integer invisibleProp = null;

    ////////// Ignore property //////////
    @Ignore
    @IsProperty
    private Integer ignoreProp = null;

    ////////// Crit-only property //////////
    @CritOnly
    @IsProperty
    private Integer critOnlyProp = null;

    ////////// Result-only property //////////
    @ResultOnly
    @IsProperty
    private EvenSlaverEntity resultOnlyProp = null;

    ////////// A property of AbstractEntity type with CritOnly assigned //////////
    @CritOnly
    @IsProperty
    private EvenSlaverEntity critOnlyAEProp;

    @CritOnly(Type.SINGLE)
    @IsProperty
    private SlaveEntity critOnlySingleAEProp;

    ////////// A collection of AbstractEntity type with CritOnly assigned //////////
    @CritOnly
    @IsProperty(EvenSlaverEntity.class)
    private List<EvenSlaverEntity> critOnlyAECollectionProp = new ArrayList<EvenSlaverEntity>();

    ////////// A property of "entity with composite key" type //////////
    @IsProperty
    private EntityWithCompositeKey entityWithCompositeKeyProp;

    public MasterEntity getMasterEntityProp() {
        return masterEntityProp;
    }
    @Observable
    public void setMasterEntityProp(final MasterEntity masterEntityProp) {
        this.masterEntityProp = masterEntityProp;
    }

    public Integer getIntegerProp() {
        return integerProp;
    }
    @Observable
    public void setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
    }

    public Double getDoubleProp() {
        return doubleProp;
    }
    @Observable
    public void setDoubleProp(final Double doubleProp) {
        this.doubleProp = doubleProp;
    }

    public BigDecimal getBigDecimalProp() {
        return bigDecimalProp;
    }
    @Observable
    public void setBigDecimalProp(final BigDecimal bigDecimalProp) {
        this.bigDecimalProp = bigDecimalProp;
    }

    public Money getMoneyProp() {
        return moneyProp;
    }
    @Observable
    public void setMoneyProp(final Money moneyProp) {
        this.moneyProp = moneyProp;
    }

    public Date getDateProp() {
        return dateProp;
    }
    @Observable
    public void setDateProp(final Date dateProp) {
        this.dateProp = dateProp;
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

    public EvenSlaverEntity getEntityProp() {
        return entityProp;
    }
    @Observable
    public void setEntityProp(final EvenSlaverEntity entityProp) {
        this.entityProp = entityProp;
    }

    public List<EvenSlaverEntity> getCollection() {
        return collection;
    }
    @Observable
    public void setCollection(final List<EvenSlaverEntity> collection) {
        this.collection.clear();
        this.collection.addAll(collection);
    }

    public EnumType getEnumProp() {
	return enumProp;
    }
    @Observable
    public void setEnumProp(final EnumType enumProp) {
	this.enumProp = enumProp;
    }

    public EvenSlaverEntity getExcludedManuallyProp() {
	return excludedManuallyProp;
    }
    @Observable
    public void setExcludedManuallyProp(final EvenSlaverEntity excludedManuallyProp) {
	this.excludedManuallyProp = excludedManuallyProp;
    }

    public EntityWithAbstractNature getEntityPropWithAbstractNature() {
	return entityPropWithAbstractNature;
    }
    @Observable
    public void setEntityPropWithAbstractNature(final EntityWithAbstractNature entityPropWithAbstractNature) {
	this.entityPropWithAbstractNature = entityPropWithAbstractNature;
    }

    public EntityWithoutKeyType getEntityPropWithoutKeyType() {
	return entityPropWithoutKeyType;
    }
    @Observable
    public void setEntityPropWithoutKeyType(final EntityWithoutKeyType entityPropWithoutKeyType) {
	this.entityPropWithoutKeyType = entityPropWithoutKeyType;
    }

    public EntityWithoutKeyTitleAndWithKeyType getEntityPropWithoutKeyTitle() {
	return entityPropWithoutKeyTitle;
    }
    @Observable
    public void setEntityPropWithoutKeyTitle(final EntityWithoutKeyTitleAndWithKeyType entityPropWithoutKeyTitle) {
	this.entityPropWithoutKeyTitle = entityPropWithoutKeyTitle;
    }

    public EntityWithKeyTitleAndWithAEKeyType getEntityPropWithAEKeyType() {
	return entityPropWithAEKeyType;
    }
    @Observable
    public void setEntityPropWithAEKeyType(final EntityWithKeyTitleAndWithAEKeyType entityPropWithAEKeyType) {
	this.entityPropWithAEKeyType = entityPropWithAEKeyType;
    }

    public Integer getInvisibleProp() {
	return invisibleProp;
    }
    @Observable
    public void setInvisibleProp(final Integer invisibleProp) {
	this.invisibleProp = invisibleProp;
    }

    public Integer getIgnoreProp() {
	return ignoreProp;
    }
    @Observable
    public void setIgnoreProp(final Integer ignoreProp) {
	this.ignoreProp = ignoreProp;
    }

    public EvenSlaverEntity getCritOnlyAEProp() {
	return critOnlyAEProp;
    }
    @Observable
    public void setCritOnlyAEProp(final EvenSlaverEntity critOnlyAEProp) {
	this.critOnlyAEProp = critOnlyAEProp;
    }

    public List<EvenSlaverEntity> getCritOnlyAECollectionProp() {
	return critOnlyAECollectionProp;
    }
    @Observable
    public void setCritOnlyAECollectionProp(final List<EvenSlaverEntity> critOnlyAECollectionProp) {
        this.critOnlyAECollectionProp.clear();
        this.critOnlyAECollectionProp.addAll(critOnlyAECollectionProp);
    }

    public EntityWithCompositeKey getEntityWithCompositeKeyProp() {
        return entityWithCompositeKeyProp;
    }
    @Observable
    public void setEntityWithCompositeKeyProp(final EntityWithCompositeKey entityWithCompositeKeyProp) {
        this.entityWithCompositeKeyProp = entityWithCompositeKeyProp;
    }

    public Integer getCritOnlyProp() {
        return critOnlyProp;
    }
    @Observable
    public void setCritOnlyProp(final Integer critOnlyProp) {
        this.critOnlyProp = critOnlyProp;
    }

    public Integer getDisabledManuallyProp() {
        return disabledManuallyProp;
    }
    @Observable
    public void setDisabledManuallyProp(final Integer disabledManuallyProp) {
        this.disabledManuallyProp = disabledManuallyProp;
    }

    public EvenSlaverEntity getResultOnlyProp() {
        return resultOnlyProp;
    }
    @Observable
    public void setResultOnlyProp(final EvenSlaverEntity resultOnlyProp) {
        this.resultOnlyProp = resultOnlyProp;
    }

    public EntityWithStringKeyType getSimpleEntityProp() {
        return simpleEntityProp;
    }
    @Observable
    public void setSimpleEntityProp(final EntityWithStringKeyType simpleEntityProp) {
        this.simpleEntityProp = simpleEntityProp;
    }

    public Integer getCheckedManuallyProp() {
        return checkedManuallyProp;
    }
    @Observable
    public void setCheckedManuallyProp(final Integer checkedManuallyProp) {
        this.checkedManuallyProp = checkedManuallyProp;
    }

    public Integer getCalcIntegerProp() {
        return calcIntegerProp;
    }
    @Observable
    public void setCalcIntegerProp(final Integer calcIntegerProp) {
        this.calcIntegerProp = calcIntegerProp;
    }

    public Integer getPropWithFunctions() {
        return propWithFunctions;
    }
    @Observable
    public void setPropWithFunctions(final Integer propWithFunctions) {
        this.propWithFunctions = propWithFunctions;
    }

    public EntityWithStringKeyType getMutablyCheckedProp() {
        return mutablyCheckedProp;
    }
    @Observable
    public void setMutablyCheckedProp(final EntityWithStringKeyType mutablyCheckedProp) {
        this.mutablyCheckedProp = mutablyCheckedProp;
    }

    public EntityWithStringKeyType getMutablyManuallyCheckedProp() {
        return mutablyManuallyCheckedProp;
    }
    @Observable
    public void setMutablyManuallyCheckedProp(final EntityWithStringKeyType mutablyManuallyCheckedProp) {
        this.mutablyManuallyCheckedProp = mutablyManuallyCheckedProp;
    }

    public BigDecimal getCheckedUntouchedProp() {
        return checkedUntouchedProp;
    }
    @Observable
    public void setCheckedUntouchedProp(final BigDecimal checkedUntouchedProp) {
        this.checkedUntouchedProp = checkedUntouchedProp;
    }

    public BigDecimal getImmutablyCheckedUntouchedProp() {
        return immutablyCheckedUntouchedProp;
    }
    @Observable
    public void setImmutablyCheckedUntouchedProp(final BigDecimal immutablyCheckedUntouchedProp) {
        this.immutablyCheckedUntouchedProp = immutablyCheckedUntouchedProp;
    }

    public BigDecimal getMutatedWithFunctionsProp() {
        return mutatedWithFunctionsProp;
    }
    @Observable
    public void setMutatedWithFunctionsProp(final BigDecimal mutatedWithFunctionsProp) {
        this.mutatedWithFunctionsProp = mutatedWithFunctionsProp;
    }

    public SlaveEntity getCritOnlySingleAEProp() {
        return critOnlySingleAEProp;
    }
    @Observable
    public void setCritOnlySingleAEProp(final SlaveEntity critOnlySingleAEProp) {
        this.critOnlySingleAEProp = critOnlySingleAEProp;
    }
    
    public Integer getUncheckedProp() {
        return uncheckedProp;
    }
    @Observable
    public void setUncheckedProp(final Integer uncheckedProp) {
        this.uncheckedProp = uncheckedProp;
    }
}
