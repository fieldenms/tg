package ua.com.fielden.platform.meta.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;

@MapEntityTo
@KeyType(String.class)
public sealed class Entity_OverridesProperty_Super extends AbstractEntity<String>
        permits Entity_OverridesProperty
{

    @IsProperty
    private String name;

    public String getName() {
        return name;
    }

    @Observable
    public Entity_OverridesProperty_Super setName(final String name) {
        this.name = name;
        return this;
    }

}
