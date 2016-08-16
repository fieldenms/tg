package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.markers.IColourType;
import ua.com.fielden.platform.types.markers.IHyperlinkType;

/**
 * Master entity object.
 *
 * @author Developers
 *
 */
@KeyType(String.class)
@CompanionObject(IEntityWithHyperlink.class)
@MapEntityTo
public class EntityWithHyperlink extends AbstractEntity<String> {

    @IsProperty
    @Title(value = "Hyperlink prop", desc = "Hyperlink prop description")
    @MapTo
    @PersistentType(userType = IHyperlinkType.class)
    private Hyperlink prop;
    
    @Observable
    public EntityWithHyperlink setProp(final Hyperlink prop) {
        this.prop = prop;
        return this;
    }

    public Hyperlink getProp() {
        return prop;
    }
}