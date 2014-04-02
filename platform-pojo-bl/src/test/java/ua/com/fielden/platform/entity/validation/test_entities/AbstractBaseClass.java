/**
 *
 */
package ua.com.fielden.platform.entity.validation.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.DomainValidation;

/**
 * Class for testing of domain validators registration.
 * 
 * @author TG Team
 */
@KeyType(String.class)
public abstract class AbstractBaseClass extends AbstractEntity<String> {

    private static final long serialVersionUID = 1992788348926508923L;

    @IsProperty
    private Integer intProp;

    public Integer getIntProp() {
        return intProp;
    }

    @Observable
    @DomainValidation
    public void setIntProp(final Integer intProp) {
        this.intProp = intProp;
    }

}
