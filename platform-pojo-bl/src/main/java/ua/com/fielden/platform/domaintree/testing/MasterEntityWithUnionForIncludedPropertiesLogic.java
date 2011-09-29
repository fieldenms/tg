package ua.com.fielden.platform.domaintree.testing;

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
public class MasterEntityWithUnionForIncludedPropertiesLogic extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    protected MasterEntityWithUnionForIncludedPropertiesLogic() {
    }

    ////////// Entity type //////////
    @IsProperty
    private UnionEntityForIncludedPropertiesLogic unionEntityProp;

    public UnionEntityForIncludedPropertiesLogic getUnionEntityProp() {
        return unionEntityProp;
    }
    @Observable
    public void setUnionEntityProp(final UnionEntityForIncludedPropertiesLogic unionEntityProp) {
        this.unionEntityProp = unionEntityProp;
    }
}
