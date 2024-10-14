package ua.com.fielden.platform.associations.one2many;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.types.Money;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * The master type in One-to-Many association with a collectional and single (special case) properties representing associations.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class MasterEntityWithOneToManyCollectionalAssociationProvidedWithLinkPropValue extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Property 1", desc = "Desc")
    private Money moneyProp;

    @IsProperty(linkProperty = "key1")
    //
    @MapTo
    @Title(value = "Property 1", desc = "Desc")
    private DetailsEntityForOneToManyAssociation one2manyAssociationSpecialCase;

    @IsProperty(value = DetailsEntityForOneToManyAssociation.class, linkProperty = "key1")
    @MapTo
    @Title(value = "Collectional Property", desc = "Desc")
    private final List<DetailsEntityForOneToManyAssociation> one2manyAssociationCollectional = new ArrayList<>();

    @Observable
    public MasterEntityWithOneToManyCollectionalAssociationProvidedWithLinkPropValue setOne2manyAssociationCollectional(final List<DetailsEntityForOneToManyAssociation> one2manyAssociationCollectional) {
        this.one2manyAssociationCollectional.clear();
        this.one2manyAssociationCollectional.addAll(one2manyAssociationCollectional);
        return this;
    }

    public List<DetailsEntityForOneToManyAssociation> getOne2manyAssociationCollectional() {
        return unmodifiableList(one2manyAssociationCollectional);
    }

    @Observable
    public MasterEntityWithOneToManyCollectionalAssociationProvidedWithLinkPropValue setOne2manyAssociationSpecialCase(final DetailsEntityForOneToManyAssociation one2oneAssociation) {
        this.one2manyAssociationSpecialCase = one2oneAssociation;
        return this;
    }

    public DetailsEntityForOneToManyAssociation getOne2manyAssociationSpecialCase() {
        return one2manyAssociationSpecialCase;
    }

    @Observable
    public MasterEntityWithOneToManyCollectionalAssociationProvidedWithLinkPropValue setMoneyProp(final Money moneyProp) {
        this.moneyProp = moneyProp;
        return this;
    }

    public Money getMoneyProp() {
        return moneyProp;
    }

}
