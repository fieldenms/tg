package ua.com.fielden.platform.serialisation;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;

/// An entity that redeclares property `desc` as invisible.
///
@KeyType(String.class)
@KeyTitle("Key")
@DescTitle("Description")
public class EntityWithInvisibleDesc extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Invisible
    private String desc;

    @Override
    public String getDesc() {
        return desc;
    }

    @Observable
    public EntityWithInvisibleDesc setDesc(final String desc) {
        this.desc = desc;
        return this;
    }

}
