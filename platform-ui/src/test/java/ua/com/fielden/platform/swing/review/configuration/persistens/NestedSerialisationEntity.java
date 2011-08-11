package ua.com.fielden.platform.swing.review.configuration.persistens;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.types.Money;

@KeyType(String.class)
@KeyTitle(value = "key", desc = "key desc")
@DescTitle(value = "desc", desc = "desc desc")
public class NestedSerialisationEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = 2631688299576070775L;

    @IsProperty
    @Title(value = "string", desc = "string desc")
    private String string;

    @IsProperty
    @Title(value = "integer", desc = "integer desc")
    private Integer integer;

    @IsProperty
    @Title(value = "money", desc = "money desc")
    private Money money;

    public String getString() {
	return string;
    }

    @Observable
    public void setString(final String string) {
	this.string = string;
    }

    public Integer getInteger() {
	return integer;
    }

    @Observable
    public void setInteger(final Integer integer) {
	this.integer = integer;
    }

    public Money getMoney() {
	return money;
    }

    @Observable
    public void setMoney(final Money money) {
	this.money = money;
    }

}
