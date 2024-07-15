package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

@EntityTitle(value = "TG Entity With Property Descriptor", desc = "Test entity with property descriptor property.")
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(TgEntityWithPropertyDescriptorCo.class)
@MapEntityTo
public class TgEntityWithPropertyDescriptor extends AbstractPersistentEntity<String> {

    @IsProperty(TgPersistentEntityWithProperties.class)
    @MapTo
    @Title(value = "Property Descriptor", desc = "Property Descriptor property")
    private PropertyDescriptor<TgPersistentEntityWithProperties> propertyDescriptor;

    @IsProperty
    @MapTo
    @Title(value = "Parent", desc = "Parent association with entity of the same type.")
    private TgEntityWithPropertyDescriptor parent;

    @Observable
    public TgEntityWithPropertyDescriptor setParent(final TgEntityWithPropertyDescriptor parent) {
        this.parent = parent;
        return this;
    }

    public TgEntityWithPropertyDescriptor getParent() {
        return parent;
    }

    @Observable
    public TgEntityWithPropertyDescriptor setPropertyDescriptor(final PropertyDescriptor<TgPersistentEntityWithProperties> propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
        return this;
    }

    public PropertyDescriptor<TgPersistentEntityWithProperties> getPropertyDescriptor() {
        return propertyDescriptor;
    }

}