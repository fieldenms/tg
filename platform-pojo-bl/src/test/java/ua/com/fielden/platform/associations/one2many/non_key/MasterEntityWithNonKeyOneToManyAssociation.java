package ua.com.fielden.platform.associations.one2many.non_key;

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
public class MasterEntityWithNonKeyOneToManyAssociation extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title(value = "Property 1", desc = "Desc")
    private Money moneyProp;

    @IsProperty(value = DetailsEntityForNonKeyOneToManyAssociation.class, linkProperty = "many2oneProp")
    @MapTo
    @Title(value = "Collectional Property", desc = "Desc")
    private final List<DetailsEntityForNonKeyOneToManyAssociation> one2manyAssociationCollectional = new ArrayList<>();

    @Observable
    public MasterEntityWithNonKeyOneToManyAssociation setOne2manyAssociationCollectional(final List<DetailsEntityForNonKeyOneToManyAssociation> one2manyAssociationCollectional) {
        this.one2manyAssociationCollectional.clear();
        this.one2manyAssociationCollectional.addAll(one2manyAssociationCollectional);
        return this;
    }

    public List<DetailsEntityForNonKeyOneToManyAssociation> getOne2manyAssociationCollectional() {
        return unmodifiableList(one2manyAssociationCollectional);
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
