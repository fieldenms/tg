package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.Max;
import ua.com.fielden.platform.types.Colour;

public class EntityWithColour extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Colour prop")
    
    private Colour prop;

    @Observable
    @Max(6)
    public EntityWithColour setProp(final Colour prop) {
        this.prop = prop;
        return this;
    }

    public Colour getProp() {
        return prop;
    }

}
