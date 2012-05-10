package ua.com.fielden.platform.associations.one2many.non_key;

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
public class MasterEntityWithNonKeyOneToManyAssociation extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Property 1", desc = "Desc")
    private Money moneyProp;

    @IsProperty(value = DetailsEntityForNonKeyOneToManyAssociation.class, linkProperty="many2oneProp")
    @MapTo
    @Title(value = "Collectional Property", desc = "Desc")
    private List<DetailsEntityForNonKeyOneToManyAssociation> one2manyAssociationCollectional;

    @Observable
    public MasterEntityWithNonKeyOneToManyAssociation setOne2manyAssociationCollectional(final List<DetailsEntityForNonKeyOneToManyAssociation> one2manyAssociationCollectional) {
	this.one2manyAssociationCollectional = one2manyAssociationCollectional;
	return this;
    }

    public List<DetailsEntityForNonKeyOneToManyAssociation> getOne2manyAssociationCollectional() {
	return one2manyAssociationCollectional;
    }

    @Observable
    public MasterEntityWithNonKeyOneToManyAssociation setMoneyProp(final Money moneyProp) {
	this.moneyProp = moneyProp;
	return this;
    }

    public Money getMoneyProp() {
	return moneyProp;
    }

}
