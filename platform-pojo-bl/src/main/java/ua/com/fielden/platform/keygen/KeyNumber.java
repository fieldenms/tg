package ua.com.fielden.platform.keygen;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * An entity representing a concept of a sequential number used for generating key values for entities like work order and purchase order.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
@KeyTitle("Key Name")
@EntityTitle(value = "Key Number", desc = "Represents a concept of auto-increment keys. Key names designate keys for specific domain entities.")
@MapEntityTo("NUMBERS")
@CompanionObject(IKeyNumber.class)
public class KeyNumber extends AbstractPersistentEntity<String> {

    /**
     * This is bloody silly, but value is string (VARCHAR) and at the same time it is used mainly for storing integer values, which should be treated as integer.
     */
    @IsProperty
    @MapTo("WONOINC")
    private String value;

    public String getValue() {
        return value.trim();
    }

    @Observable
    public KeyNumber setValue(final String value) {
        this.value = value.trim();
        return this;
    }

    @Override
    @Observable
    public KeyNumber setKey(String key) {
        super.setKey(key);
        return this;
    }

}
