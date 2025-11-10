package ua.com.fielden.platform.dao.dynamic;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.Date;

/// Entity for "dynamic query" testing.
///
@KeyType(DynamicEntityKey.class)
@MapEntityTo("EVEN_SLAVER_ENTITY")
public class EvenSlaverEntity extends AbstractEntity<DynamicEntityKey> {

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
    @MapTo("BIG_DECIMAL_PROP")
    private BigDecimal bigDecimalProp;

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
