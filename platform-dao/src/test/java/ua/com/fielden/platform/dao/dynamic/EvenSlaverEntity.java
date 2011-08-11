package ua.com.fielden.platform.dao.dynamic;

import java.math.BigDecimal;
import java.util.Date;

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
@MapEntityTo("EVEN_SLAVER_ENTITY")
public class EvenSlaverEntity extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    protected EvenSlaverEntity() {
    }

    @IsProperty
    @CompositeKeyMember(1)
    @MapTo("SLAVE_ENTITY_PROP")
    private SlaveEntity slaveEntityProp;

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

    public SlaveEntity getSlaveEntityProp() {
        return slaveEntityProp;
    }
    @Observable
    public void setSlaveEntityProp(final SlaveEntity slaveEntityProp) {
        this.slaveEntityProp = slaveEntityProp;
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
}
