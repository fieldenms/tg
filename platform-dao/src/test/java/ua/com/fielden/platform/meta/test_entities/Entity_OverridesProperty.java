package ua.com.fielden.platform.meta.test_entities;

import ua.com.fielden.platform.entity.annotation.*;

@MapEntityTo
@KeyType(String.class)
public non-sealed class Entity_OverridesProperty extends Entity_OverridesProperty_Super {

    // make the property persistent
    @IsProperty
    @MapTo
    private String name;

    public String getName() {
        return name;
    }

    @Observable
    public Entity_OverridesProperty setName(final String name) {
        this.name = name;
        return this;
    }

}
