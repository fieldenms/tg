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
public class Union2ForIncludedPropertiesLogic extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    protected Union2ForIncludedPropertiesLogic() {
    }

    ////////// Entity type //////////
    @IsProperty
    private Integer commonProp;

    @IsProperty
    private Integer nonCommonPropFrom2;

    public Integer getCommonProp() {
        return commonProp;
    }

    @Observable
    public void setCommonProp(final Integer commonProp) {
        this.commonProp = commonProp;
    }

    public Integer getNonCommonPropFrom2() {
        return nonCommonPropFrom2;
    }

    @Observable
    public void setNonCommonPropFrom2(final Integer nonCommonPropFrom2) {
        this.nonCommonPropFrom2 = nonCommonPropFrom2;
    }
}
