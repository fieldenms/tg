package ua.com.fielden.platform.serialisation.impl.spike;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.types.Money;

@KeyType(String.class)
@KeyTitle("Key")
public class DomainType1 extends AbstractEntity<String> {

    @IsProperty
    private DomainType1 itself;
    @IsProperty
    private Money money;
    @IsProperty
    private BigDecimal bigDecimal;
    @IsProperty
    private Integer integer;
    @IsProperty
    private Date date;

    protected DomainType1() {
    }

    public Money getMoney() {
	return money;
    }

    @Observable
    public void setMoney(final Money money) {
	this.money = money;
    }

    public BigDecimal getBigDecimal() {
	return bigDecimal;
    }

    @Observable
    public void setBigDecimal(final BigDecimal bigDecimal) {
	this.bigDecimal = bigDecimal;
    }

    public Integer getInteger() {
	return integer;
    }

    @Observable
    public void setInteger(final Integer integer) {
	this.integer = integer;
    }

    public Date getDate() {
	return date;
    }

    @Observable
    public void setDate(final Date date) {
	this.date = date;
    }

    public DomainType1 getItself() {
	return itself;
    }

    @Observable
    public void setItself(final DomainType1 itself) {
	this.itself = itself;
    }
}
