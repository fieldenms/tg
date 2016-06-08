package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

/** 
 * Master entity object.
 * 
 * @author Developers
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(ITgEntityWithPropertyDescriptor.class)
@MapEntityTo
public class TgEntityWithPropertyDescriptor extends AbstractPersistentEntity<String> {
    private static final long serialVersionUID = 1L;
    
    @IsProperty(TgPersistentEntityWithProperties.class)
    @MapTo
    @Title(value = "Property Descriptor", desc = "Property Descriptor property")
    private PropertyDescriptor<TgPersistentEntityWithProperties> propertyDescriptor;

    @Observable
    public TgEntityWithPropertyDescriptor setPropertyDescriptor(final PropertyDescriptor<TgPersistentEntityWithProperties> propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
        return this;
    }

    public PropertyDescriptor<TgPersistentEntityWithProperties> getPropertyDescriptor() {
        return propertyDescriptor;
    }
}