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
public class EntityWithInvalidIntegerProp extends AbstractEntity<String> {

    
    @IsProperty(precision = 10, scale = 14)
    private Integer numericInteger;
    

    @Observable
    public EntityWithInvalidIntegerProp setNumericInteger(final Integer integer) {
        this.numericInteger = integer;
        return this;
    }

    public Integer getNumericInteger() {
        return numericInteger;
    }
}
