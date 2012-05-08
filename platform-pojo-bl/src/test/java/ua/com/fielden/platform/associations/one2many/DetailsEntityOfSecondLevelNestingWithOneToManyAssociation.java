package ua.com.fielden.platform.associations.one2many;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.types.Money;

/**
 * Type representing the details side of One-to-Many association with another details entity.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class DetailsEntityOfSecondLevelNestingWithOneToManyAssociation extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Key 1", desc = "Desc")
    @CompositeKeyMember(1)
    private DetailsEntityForOneToManyAssociation key1;

    @IsProperty
    @MapTo
    @Title(value = "Key 2", desc = "Desc")
    @CompositeKeyMember(2)
    private Integer key2;

    @Observable
    public DetailsEntityOfSecondLevelNestingWithOneToManyAssociation setKey2(final Integer key2) {
	this.key2 = key2;
	return this;
    }

    public Integer getKey2() {
	return key2;
    }

    @Observable
    public DetailsEntityOfSecondLevelNestingWithOneToManyAssociation setKey1(final DetailsEntityForOneToManyAssociation key1) {
	this.key1 = key1;
	return this;
    }

    public DetailsEntityForOneToManyAssociation getKey1() {
	return key1;
    }


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
    public DetailsEntityOfSecondLevelNestingWithOneToManyAssociation setMoneyProp(final Money moneyProp) {
	this.moneyProp = moneyProp;
	return this;
    }

    public Money getMoneyProp() {
	return moneyProp;
    }


    @Observable
    public DetailsEntityOfSecondLevelNestingWithOneToManyAssociation setIntProp(final Integer intProp) {
	this.intProp = intProp;
	return this;
    }

    public Integer getIntProp() {
	return intProp;
    }


    @Observable
    public DetailsEntityOfSecondLevelNestingWithOneToManyAssociation setStrProp(final String strProp) {
	this.strProp = strProp;
	return this;
    }

    public String getStrProp() {
	return strProp;
    }

}
