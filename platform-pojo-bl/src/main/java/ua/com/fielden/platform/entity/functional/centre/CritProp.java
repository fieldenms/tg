package ua.com.fielden.platform.entity.functional.centre;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Represents the selection criteria property on the entity centre.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Criteria property", desc = "Criteria property")
@CompanionObject(ICritProp.class)
public class CritProp extends AbstractEntity<String> {
    private static final long serialVersionUID = -856658523610149038L;

    @IsProperty
    @MapTo
    @Title(value = "First property value", desc = "First property value")
    private Object value1;

    @IsProperty
    @MapTo
    @Title(value = "Second property value", desc = "Second property value")
    private Object value2;

    @IsProperty
    @MapTo
    @Title(value = "Property type", desc = "Property type")
    private String propType;

    @IsProperty
    @MapTo
    @Title(value = "Is proprty single?", desc = "Determines whehter proprty is single or not")
    private boolean single;

    @Observable
    public CritProp setSingle(final boolean single) {
        this.single = single;
        return this;
    }

    public boolean isSingle() {
        return single;
    }

    @Observable
    public CritProp setPropType(final String type) {
        this.propType = type;
        return this;
    }

    public String getPropType() {
        return propType;
    }

    @Observable
    public CritProp setValue2(final Object value2) {
        this.value2 = value2;
        return this;
    }

    public Object getValue2() {
        return value2;
    }

    @Observable
    public CritProp setValue1(final Object value1) {
        this.value1 = value1;
        return this;
    }

    public Object getValue1() {
        return value1;
    }
}