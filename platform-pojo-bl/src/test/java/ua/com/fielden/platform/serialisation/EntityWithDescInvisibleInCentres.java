package ua.com.fielden.platform.serialisation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

/// An entity that redeclares property `desc` as invisible in entity centres only.
///
@KeyType(String.class)
@KeyTitle("Key")
@DescTitle("Description")
public class EntityWithDescInvisibleInCentres extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Invisible(centreOnly = true)
    private String desc;

    @Override
    public String getDesc() {
        return desc;
    }

    @Observable
    public EntityWithDescInvisibleInCentres setDesc(final String desc) {
        this.desc = desc;
        return this;
    }

}
