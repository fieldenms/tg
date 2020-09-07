package ua.com.fielden.platform.sample.domain;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
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
@MapEntityTo
@DescTitle("Description")
@KeyTitle("Bogie")
@Ignore
@CompanionObject(ITgBogie.class)
public class TgBogie extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "Location", desc = "Location")
    private TgBogieLocation location;

    @Observable
    public TgBogie setLocation(final TgBogieLocation location) {
        this.location = location;
        return this;
    }

    public TgBogieLocation getLocation() {
        return location;
    }
}