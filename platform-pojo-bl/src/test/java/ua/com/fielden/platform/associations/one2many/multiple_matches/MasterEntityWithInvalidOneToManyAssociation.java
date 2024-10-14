package ua.com.fielden.platform.associations.one2many.multiple_matches;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.types.Money;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * The master type in One-to-Many association with a collectional and single (special case) properties representing assocaitons.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class MasterEntityWithInvalidOneToManyAssociation extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Property 1", desc = "Desc")
    private Money moneyProp;

    @IsProperty
    // linkProperty="key1" is missing on purpose
    @MapTo
    @Title(value = "Property 1", desc = "Desc")
    private DetailsEntityForInvalidOneToManyAssociation one2manyAssociationSpecialCase;

    @IsProperty(value = DetailsEntityForInvalidOneToManyAssociation.class)
    // linkProperty is omitted on purpose
    @MapTo
    @Title(value = "Collectional Property", desc = "Desc")
    private final List<DetailsEntityForInvalidOneToManyAssociation> one2manyAssociationCollectional = new ArrayList<>();

    @Observable
    public MasterEntityWithInvalidOneToManyAssociation setOne2manyAssociationCollectional(final List<DetailsEntityForInvalidOneToManyAssociation> one2manyAssociationCollectional) {
        this.one2manyAssociationCollectional.clear();
        this.one2manyAssociationCollectional.addAll(one2manyAssociationCollectional);
        return this;
    }

    public List<DetailsEntityForInvalidOneToManyAssociation> getOne2manyAssociationCollectional() {
        return unmodifiableList(one2manyAssociationCollectional);
    }

    @Observable
    public MasterEntityWithInvalidOneToManyAssociation setOne2manyAssociationSpecialCase(final DetailsEntityForInvalidOneToManyAssociation one2oneAssociation) {
        this.one2manyAssociationSpecialCase = one2oneAssociation;
        return this;
    }

    public DetailsEntityForInvalidOneToManyAssociation getOne2manyAssociationSpecialCase() {
        return one2manyAssociationSpecialCase;
    }

    @Observable
    public MasterEntityWithInvalidOneToManyAssociation setMoneyProp(final Money moneyProp) {
        this.moneyProp = moneyProp;
        return this;
    }

    public Money getMoneyProp() {
        return moneyProp;
    }

}
