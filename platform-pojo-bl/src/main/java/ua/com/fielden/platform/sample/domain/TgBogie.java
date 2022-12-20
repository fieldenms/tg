package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@MapEntityTo(autoConflictResolution = false)
@DescTitle("Description")
@KeyTitle("Bogie")
@CompanionObject(ITgBogie.class)
public class TgBogie extends ActivatableAbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Location", desc = "Location")
    private TgBogieLocation location;
    
    @IsProperty
    @MapTo
    @Title(value = "Bogies Class", desc = "A class of this bogies.")
    private TgBogieClass bogieClass;

    @Observable
    public TgBogie setBogieClass(final TgBogieClass bogieClass) {
        this.bogieClass = bogieClass;
        return this;
    }

    public TgBogieClass getBogieClass() {
        return bogieClass;
    }

    @Observable
    public TgBogie setLocation(final TgBogieLocation location) {
        this.location = location;
        return this;
    }

    public TgBogieLocation getLocation() {
        return location;
    }

    @Override
    @Observable
    protected TgBogie setActive(final boolean active) {
        super.setActive(active);
        return this;
    }

}