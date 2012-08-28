package ua.com.fielden.platform.associations.one2many;

import java.util.Date;
import java.util.List;

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
 * Type representing the details side of One-to-Many association.
 *
 * @author TG Team
 *
 */
@KeyType(DynamicEntityKey.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class DetailsEntityForOneToManyAssociation extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Key 1", desc = "Desc")
    @CompositeKeyMember(1)
    private MasterEntityWithOneToManyAssociation key1;

    @IsProperty
    @MapTo
    @Title(value = "Key 2", desc = "Desc")
    @CompositeKeyMember(2)
    private Integer key2;

    @IsProperty
    @MapTo
    @Title(value = "Date", desc = "Date")
    private Date dateProp;


    @IsProperty(value = DetailsEntityOfSecondLevelNestingWithOneToManyAssociation.class) // linkProperty="key1" is missing on purpose
    @MapTo
    @Title(value = "Collectional Property", desc = "Desc")
    private List<DetailsEntityOfSecondLevelNestingWithOneToManyAssociation> one2manyAssociationCollectional;

    @Observable
    public DetailsEntityForOneToManyAssociation setKey2(final Integer key2) {
	this.key2 = key2;
	return this;
    }

    public Integer getKey2() {
	return key2;
    }

    @Observable
    public DetailsEntityForOneToManyAssociation setKey1(final MasterEntityWithOneToManyAssociation key1) {
	this.key1 = key1;
	return this;
    }

    public MasterEntityWithOneToManyAssociation getKey1() {
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
    public DetailsEntityForOneToManyAssociation setMoneyProp(final Money moneyProp) {
	this.moneyProp = moneyProp;
	return this;
    }

    public Money getMoneyProp() {
	return moneyProp;
    }


    @Observable
    public DetailsEntityForOneToManyAssociation setIntProp(final Integer intProp) {
	this.intProp = intProp;
	return this;
    }

    public Integer getIntProp() {
	return intProp;
    }


    @Observable
    public DetailsEntityForOneToManyAssociation setStrProp(final String strProp) {
	this.strProp = strProp;
	return this;
    }

    public String getStrProp() {
	return strProp;
    }

    @Observable
    public DetailsEntityForOneToManyAssociation setOne2manyAssociationCollectional(final List<DetailsEntityOfSecondLevelNestingWithOneToManyAssociation> one2manyAssociationCollectional) {
	this.one2manyAssociationCollectional = one2manyAssociationCollectional;
	return this;
    }

    public List<DetailsEntityOfSecondLevelNestingWithOneToManyAssociation> getOne2manyAssociationCollectional() {
        return one2manyAssociationCollectional;
    }

    @Observable
    public DetailsEntityForOneToManyAssociation setDateProp(final Date dateProp) {
	this.dateProp = dateProp;
	return this;
    }

    public Date getDateProp() {
	return dateProp;
    }
}

