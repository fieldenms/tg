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
public class Union1ForIncludedPropertiesLogic extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    protected Union1ForIncludedPropertiesLogic() {
    }

    ////////// Entity type //////////
    @IsProperty
    private Integer commonProp;

    @IsProperty
    private Integer nonCommonPropFrom1;

    public Integer getCommonProp() {
        return commonProp;
    }
    @Observable
    public void setCommonProp(final Integer commonProp) {
        this.commonProp = commonProp;
    }

    public Integer getNonCommonPropFrom1() {
        return nonCommonPropFrom1;
    }
    @Observable
    public void setNonCommonPropFrom1(final Integer nonCommonPropFrom1) {
        this.nonCommonPropFrom1 = nonCommonPropFrom1;
    }
}
