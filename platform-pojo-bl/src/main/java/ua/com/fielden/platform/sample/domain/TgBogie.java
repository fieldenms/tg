package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

import static ua.com.fielden.platform.entity.annotation.CritOnly.Type.MULTI;

@KeyType(String.class)
@MapEntityTo(autoConflictResolution = false)
@DescTitle("Description")
@KeyTitle("Bogie")
@CompanionObject(ITgBogie.class)
public class TgBogie extends ActivatableAbstractEntity<String> {

    public static final String
            LOCATION = "location",
            BOGIE_CLASS = "bogieClass",
            LOCATION_CRIT = "locationCrit";

    @IsProperty
    @MapTo
    @Title(value = "Location", desc = "Location")
    private TgBogieLocation location;
    
    @IsProperty
    @MapTo
    @Title(value = "Bogies Class", desc = "A class of this bogies.")
    private TgBogieClass bogieClass;

    @IsProperty
    @CritOnly(MULTI)
    private TgBogieLocation locationCrit;

    @Observable
    public TgBogie setLocationCrit(final TgBogieLocation locationCrit) {
        this.locationCrit = locationCrit;
        return this;
    }

    public TgBogieLocation getLocationCrit() {
        return locationCrit;
    }

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