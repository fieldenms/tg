package ua.com.fielden.platform.associations.one2many;

import java.util.List;

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
 * The master type in One-to-Many association with a collectional and single (special case) properties representing assocaitons.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class MasterEntityWithOneToManyAssociation extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Property 1", desc = "Desc")
    private Money moneyProp;

    @IsProperty // linkProperty="key1" is missing on purpose
    @MapTo
    @Title(value = "Property 1", desc = "Desc")
    private DetailsEntityForOneToManyAssociation one2manyAssociationSpecialCase;

    @IsProperty(linkProperty="key1", value = DetailsEntityForOneToManyAssociation.class)  @MapTo
    @Title(value = "Collectional Property", desc = "Desc")
    private List<DetailsEntityForOneToManyAssociation> one2manyAssociationCollectional;

    @Observable
    public MasterEntityWithOneToManyAssociation setOne2manyAssociationCollectional(final List<DetailsEntityForOneToManyAssociation> one2manyAssociationCollectional) {
	this.one2manyAssociationCollectional = one2manyAssociationCollectional;
	return this;
    }

    public List<DetailsEntityForOneToManyAssociation> getOne2manyAssociationCollectional() {
	return one2manyAssociationCollectional;
    }

    @Observable
    public MasterEntityWithOneToManyAssociation setOne2manyAssociationSpecialCase(final DetailsEntityForOneToManyAssociation one2oneAssociation) {
	this.one2manyAssociationSpecialCase = one2oneAssociation;
	return this;
    }

    public DetailsEntityForOneToManyAssociation getOne2manyAssociationSpecialCase() {
	return one2manyAssociationSpecialCase;
    }

    @Observable
    public MasterEntityWithOneToManyAssociation setMoneyProp(final Money moneyProp) {
	this.moneyProp = moneyProp;
	return this;
    }

    public Money getMoneyProp() {
	return moneyProp;
    }

}
