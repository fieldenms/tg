package ua.com.fielden.platform.associations.one2one;

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
 * Type representing the detail side of One-to-One association.
 * 
 * @author TG Team
 * 
 */
@KeyType(MasterEntityWithOneToOneAssociation.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class DetailEntityForOneToOneAssociationWithOneToManyAssociation extends AbstractEntity<MasterEntityWithOneToOneAssociation> {
    private static final long serialVersionUID = 1L;

    @IsProperty(DetailsEntityForOneToOneDetailTypeInOneToManyAssociation.class)
    // linkProperty = "key1" is omitted deliberately
    @MapTo
    @Title(value = "One 2 Many", desc = "Desc")
    private List<DetailsEntityForOneToOneDetailTypeInOneToManyAssociation> one2ManyAssociation;

    @Observable
    public DetailEntityForOneToOneAssociationWithOneToManyAssociation setOne2ManyAssociation(final List<DetailsEntityForOneToOneDetailTypeInOneToManyAssociation> one2ManyAssociation) {
        this.one2ManyAssociation = one2ManyAssociation;
        return this;
    }

    public List<DetailsEntityForOneToOneDetailTypeInOneToManyAssociation> getOne2ManyAssociation() {
        return one2ManyAssociation;
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
    public DetailEntityForOneToOneAssociationWithOneToManyAssociation setMoneyProp(final Money moneyProp) {
        this.moneyProp = moneyProp;
        return this;
    }

    public Money getMoneyProp() {
        return moneyProp;
    }

    @Observable
    public DetailEntityForOneToOneAssociationWithOneToManyAssociation setIntProp(final Integer intProp) {
        this.intProp = intProp;
        return this;
    }

    public Integer getIntProp() {
        return intProp;
    }

    @Observable
    public DetailEntityForOneToOneAssociationWithOneToManyAssociation setStrProp(final String strProp) {
        this.strProp = strProp;
        return this;
    }

    public String getStrProp() {
        return strProp;
    }

}
