package ua.com.fielden.platform.expression.ast.visitor.entities;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.types.Money;

@KeyType(String.class)
public class EntityLevel3 extends AbstractEntity<String> {

    @IsProperty
    private Money moneyProperty;

    public Money getMoneyProperty() {
	return moneyProperty;
    }

    @Observable
    public void setMoneyProperty(final Money moneyProperty) {
	this.moneyProperty = moneyProperty;
    }

    @IsProperty
    @MapTo
    @Title(value = "Date Property", desc = "Property to test date functions in expression language.")
    private Date dateProperty;

    @Observable
    public EntityLevel3 setDateProperty(final Date dateProperty) {
	this.dateProperty = dateProperty;
	return this;
    }

    public Date getDateProperty() {
	return dateProperty;
    }
}
