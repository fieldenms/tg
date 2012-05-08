package ua.com.fielden.platform.associations.one2one;

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
 * The master type in One-to-One association.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class MasterEntityWithOneToOneAssociation extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Property 1", desc = "Desc")
    private DetailEntityForOneToOneAssociationWithOneToManyAssociation one2oneAssociation;

    @IsProperty
    @MapTo
    @Title(value = "Property 2", desc = "Desc")
    private Integer intProp;

    @IsProperty
    @MapTo
    @Title(value = "Property 3", desc = "Desc")
    private Money moneyProp;

    @Observable
    public MasterEntityWithOneToOneAssociation setMoneyProp(final Money moneyProp) {
	this.moneyProp = moneyProp;
	return this;
    }

    public Money getMoneyProp() {
	return moneyProp;
    }


    @Observable
    public MasterEntityWithOneToOneAssociation setIntProp(final Integer intProp) {
	this.intProp = intProp;
	return this;
    }

    public Integer getIntProp() {
	return intProp;
    }

    @Observable
    public MasterEntityWithOneToOneAssociation setOne2oneAssociation(final DetailEntityForOneToOneAssociationWithOneToManyAssociation one2oneAssociation) {
	this.one2oneAssociation = one2oneAssociation;
	return this;
    }

    public DetailEntityForOneToOneAssociationWithOneToManyAssociation getOne2oneAssociation() {
	return one2oneAssociation;
    }

}
