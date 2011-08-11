package ua.com.fielden.platform.swing.review.configuration.persistens;

import java.util.Date;

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
public class SerialisationEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = 2703117499795511385L;

    @IsProperty
    @Title(value = "string", desc = "string desc")
    private String string;

    @IsProperty
    @Title(value = "money", desc = "money desc")
    private Money money;

    @IsProperty
    @Title(value = "date", desc = "date desc")
    private Date date;

    @IsProperty
    @Title(value = "iteger", desc = "integer desc")
    private Integer integer;

    @IsProperty
    @Title(value = "entity", desc = "entity desc")
    private NestedSerialisationEntity entity;

    public String getString() {
	return string;
    }

    @Observable
    public void setString(final String string) {
	this.string = string;
    }

    public Money getMoney() {
	return money;
    }

    @Observable
    public void setMoney(final Money money) {
	this.money = money;
    }

    public Date getDate() {
	return date;
    }

    @Observable
    public void setDate(final Date date) {
	this.date = date;
    }

    public Integer getInteger() {
	return integer;
    }

    @Observable
    public void setInteger(final Integer integer) {
	this.integer = integer;
    }

    public NestedSerialisationEntity getEntity() {
	return entity;
    }

    @Observable
    public void setEntity(final NestedSerialisationEntity entity) {
	this.entity = entity;
    }
}
