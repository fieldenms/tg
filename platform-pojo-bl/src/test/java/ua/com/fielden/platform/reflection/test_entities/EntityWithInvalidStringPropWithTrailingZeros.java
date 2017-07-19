package ua.com.fielden.platform.reflection.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * A entity for validating definitions of numeric properties.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
public class EntityWithInvalidStringPropWithTrailingZeros extends AbstractEntity<String> {

    
    @IsProperty(trailingZeros = false)
    private String stringProp;
    

    @Observable
    public EntityWithInvalidStringPropWithTrailingZeros setNumericMoney(final String value) {
        this.stringProp = value;
        return this;
    }

    public String getStringProp() {
        return stringProp;
    }
}
