package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.types.Colour;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(String.class)
@CompanionObject(IEntityWithColour.class)
@MapEntityTo
public class EntityWithColour extends AbstractEntity<String> {

    @IsProperty
    @MapTo("COLOUR")
    @Title(value = "Colour prop")
    private Colour prop;

    @Observable
      public EntityWithColour setProp(final Colour prop) {
        this.prop = prop;
        return this;
    }

    public Colour getProp() {
        return prop;
    }

}