package ua.com.fielden.platform.dao.dynamic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.types.Money;

/**
 * Entity for "dynamic query" testing.
 * 
 * @author TG Team
 * 
 */
@KeyType(DynamicEntityKey.class)
@MapEntityTo("SLAVE_ENTITY")
public class SlaveEntity extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    protected SlaveEntity() {
    }

    @IsProperty
    @CompositeKeyMember(1)
    @MapTo("MASTER_ENTITY_PROP")
    private MasterEntity masterEntityProp;

    ////////// Range types //////////
    @IsProperty
    @CompositeKeyMember(2)
    @MapTo("INTEGER_PROP")
    private Integer integerProp = null;
    @IsProperty
    @MapTo("DOUBLE_PROP")
    private Double doubleProp = 0.0;
    @IsProperty
    @MapTo("DOUBLE_PROP")
    private BigDecimal bigDecimalProp = new BigDecimal(0.0);

    @IsProperty
    @MapTo("MONEY_PROP")
    private Money moneyProp;
    @IsProperty
    @MapTo("DATE_PROP")
    private Date dateProp;

    ////////// boolean type //////////
    @IsProperty
    @MapTo("BOOLEAN_PROP")
    private boolean booleanProp = false;

    ////////// String type //////////
    @IsProperty
    @MapTo("STRING_PROP")
    private String stringProp;

    ///////// Collections /////////
    @IsProperty(EvenSlaverEntity.class)
    private List<EvenSlaverEntity> collection = new ArrayList<EvenSlaverEntity>();

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

    public List<EvenSlaverEntity> getCollection() {
        return collection;
    }

    @Observable
    public void setCollection(final List<EvenSlaverEntity> collection) {
        this.collection.clear();
        this.collection.addAll(collection);
    }
}
