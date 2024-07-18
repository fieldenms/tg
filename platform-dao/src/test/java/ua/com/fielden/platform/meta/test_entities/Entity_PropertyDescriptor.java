package ua.com.fielden.platform.meta.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.sample.domain.TgVehicle;

@MapEntityTo
@KeyType(String.class)
public class Entity_PropertyDescriptor extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    @Title(value = "", desc = "")
    private PropertyDescriptor<TgVehicle> pd;

    public PropertyDescriptor<TgVehicle> getPd() {
        return pd;
    }

    @Observable
    public Entity_PropertyDescriptor setPd(final PropertyDescriptor<TgVehicle> pd) {
        this.pd = pd;
        return this;
    }

}
