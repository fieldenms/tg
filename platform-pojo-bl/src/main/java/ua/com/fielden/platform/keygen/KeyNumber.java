package ua.com.fielden.platform.keygen;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;

/**
 * An entity representing a concept of a sequential number used for generating key for WONO and some other entities.
 *
 * In reality, the only time this class should be used is when the underlying DB does not support custom sequences.
 *
 * @author 01es
 *
 */
@KeyType(String.class)
@MapEntityTo(value = "NUMBERS", keyColumn = "NUMBKEY")
@CompanionObject(IKeyNumber.class)
public class KeyNumber extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    /**
     * This is bloody silly, but value is string (VARCHAR) and at the same time it is used mainly for storing integer values, which should be treated as integer.
     */
    @IsProperty
    @MapTo("WONOINC")
    private String value;

    /**
     * Required for TG entity factory.
     */
    protected KeyNumber() {
    }

    /**
     * It is safe to instantiate KeyNumber using <code>new</code> since it does not require infrastructure provided by {@link EntityFactory}.
     *
     * @param key
     * @param value
     */
    public KeyNumber(final String key, final String value) {
	setKey(key);
	setValue(value);
    }

    public String getValue() {
	return value.trim();
    }

    @Observable
    public KeyNumber setValue(final String value) {
	this.value = value.trim();
	return this;
    }
}
