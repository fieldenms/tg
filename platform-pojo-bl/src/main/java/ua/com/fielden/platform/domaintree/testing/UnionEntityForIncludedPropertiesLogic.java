package ua.com.fielden.platform.domaintree.testing;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Entity for "included properties logic" testing.
 *
 * @author TG Team
 *
 */
@KeyTitle(value = "Key title", desc = "Key desc")
@DescTitle(value = "Desc title", desc = "Desc desc")
public class UnionEntityForIncludedPropertiesLogic extends AbstractUnionEntity {
    private static final long serialVersionUID = 1L;

    protected UnionEntityForIncludedPropertiesLogic() {
    }

    @IsProperty
    private Union1ForIncludedPropertiesLogic unionProp1;

    @IsProperty
    private Union2ForIncludedPropertiesLogic unionProp2;

    public Union1ForIncludedPropertiesLogic getUnionProp1() {
        return unionProp1;
    }
    @Observable
    public void setUnionProp1(final Union1ForIncludedPropertiesLogic unionProp1) {
        this.unionProp1 = unionProp1;
    }

    public Union2ForIncludedPropertiesLogic getUnionProp2() {
        return unionProp2;
    }
    @Observable
    public void setUnionProp2(final Union2ForIncludedPropertiesLogic unionProp2) {
        this.unionProp2 = unionProp2;
    }
}
