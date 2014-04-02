package ua.com.fielden.platform.associations.one2many.incorrect;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;

/**
 * The master type in One-to-Many association with a collectional and single (special case) properties representing assocaitons.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
@KeyTitle(value = "Key")
@DescTitle(value = "Description")
public class MasterEntity1 extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    // missing value()
    @MapTo
    private PropertyDescriptor<DetailsEntity1> propertyDescriptorProperty;

    @Observable
    public MasterEntity1 setPropertyDescriptorProperty(final PropertyDescriptor<DetailsEntity1> propertyDescriptorProperty) {
        this.propertyDescriptorProperty = propertyDescriptorProperty;
        return this;
    }

    public PropertyDescriptor<DetailsEntity1> getPropertyDescriptorProperty() {
        return propertyDescriptorProperty;
    }
}
