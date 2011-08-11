package ua.com.fielden.platform.expression.ast.visitor.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
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
}
