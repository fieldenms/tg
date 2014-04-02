package ua.com.fielden.platform.swing.review;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * Entity for "domain tree representation" testing.
 * 
 * @author TG Team
 * 
 */
@KeyTitle(value = "Key title", desc = "Key desc")
@DescTitle(value = "Desc title", desc = "Desc desc")
@KeyType(String.class)
public class DomainEntityWithStringKeyType extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    protected DomainEntityWithStringKeyType() {
    }

    @IsProperty
    private Integer integerProp = null;

    public Integer getIntegerProp() {
        return integerProp;
    }

    @Observable
    public void setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
    }
}