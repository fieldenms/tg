package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.PersistedType;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.markers.IColourType;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgEntityForColourMaster.class)
@MapEntityTo
@DescTitle(value = "Desc", desc = "Some desc description")
public class TgEntityForColourMaster extends AbstractEntity<String> {
    @IsProperty
    @MapTo
    @Title(value = "String prop", desc = "String prop description")
    private String stringProp;

    @IsProperty
    @MapTo
    @Title(value = "Boolean prop", desc = " Boolean prop description")
    private boolean booleanProp;

    @IsProperty
    @MapTo
    @PersistedType(userType = IColourType.class)
    @Title(value = "Colour prop", desc = "Colour prop description")
    private Colour colourProp;

    @Observable
    public TgEntityForColourMaster setColourProp(final Colour colourProp) {
        this.colourProp = colourProp;
        return this;
    }

    public Colour getColourProp() {
        return colourProp;
    }

    @Observable
    public TgEntityForColourMaster setBooleanProp(final boolean booleanProp) {
        this.booleanProp = booleanProp;
        return this;
    }

    public boolean getBooleanProp() {
        return booleanProp;
    }

    @Observable
    public TgEntityForColourMaster setStringProp(final String stringProp) {
        this.stringProp = stringProp;
        return this;
    }

    public String getStringProp() {
        return stringProp;
    }

}