package ua.com.fielden.platform.associations.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.types.Money;

/**
 * Contains only Many-to-One associations
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class EntityWithoutAssociations extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Property 1", desc = "Desc")
    private String strProp;

    @IsProperty
    @MapTo
    @Title(value = "Property 2", desc = "Desc")
    private Integer intProp;

    @IsProperty
    @MapTo
    @Title(value = "Property 3", desc = "Desc")
    private Money moneyProp;

    @Observable
    public EntityWithoutAssociations setMoneyProp(final Money moneyProp) {
	this.moneyProp = moneyProp;
	return this;
    }

    public Money getMoneyProp() {
	return moneyProp;
    }


    @Observable
    public EntityWithoutAssociations setIntProp(final Integer intProp) {
	this.intProp = intProp;
	return this;
    }

    public Integer getIntProp() {
	return intProp;
    }


    @Observable
    public EntityWithoutAssociations setStrProp(final String strProp) {
	this.strProp = strProp;
	return this;
    }

    public String getStrProp() {
	return strProp;
    }

}
