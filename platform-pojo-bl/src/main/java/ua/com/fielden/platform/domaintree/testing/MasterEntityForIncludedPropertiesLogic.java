package ua.com.fielden.platform.domaintree.testing;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Entity for "included properties logic" testing.
 *
 * @author TG Team
 *
 */
@KeyTitle(value = "Key title", desc = "Key desc")
@DescTitle(value = "Desc title", desc = "Desc desc")
@KeyType(String.class)
public class MasterEntityForIncludedPropertiesLogic extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    protected MasterEntityForIncludedPropertiesLogic() {
    }

    ////////// Range types //////////
    @IsProperty
    private Integer integerProp = null;

    ////////// Entity type //////////
    @IsProperty
    private MasterEntityForIncludedPropertiesLogic entityPropOfSelfType;

    ////////// Entity type //////////
    @IsProperty(linkProperty = "keyPartProp")
    private SlaveEntityForIncludedPropertiesLogic entityProp;

    ///////// Collections /////////
    @IsProperty(value = SlaveEntityForIncludedPropertiesLogic.class, linkProperty = "keyPartProp")
    private List<SlaveEntityForIncludedPropertiesLogic> entityPropCollection = new ArrayList<SlaveEntityForIncludedPropertiesLogic>();

    public Integer getIntegerProp() {
        return integerProp;
    }
    @Observable
    public void setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
    }

    public SlaveEntityForIncludedPropertiesLogic getEntityProp() {
        return entityProp;
    }
    @Observable
    public void setEntityProp(final SlaveEntityForIncludedPropertiesLogic entityProp) {
        this.entityProp = entityProp;
    }

    public List<SlaveEntityForIncludedPropertiesLogic> getEntityPropCollection() {
        return entityPropCollection;
    }
    @Observable
    public void setEntityPropCollection(final List<SlaveEntityForIncludedPropertiesLogic> collection) {
        this.entityPropCollection.clear();
        this.entityPropCollection.addAll(collection);
    }

    public MasterEntityForIncludedPropertiesLogic getEntityPropOfSelfType() {
        return entityPropOfSelfType;
    }
    @Observable
    public void setEntityPropOfSelfType(final MasterEntityForIncludedPropertiesLogic entityPropOfSelfType) {
        this.entityPropOfSelfType = entityPropOfSelfType;
    }
}
