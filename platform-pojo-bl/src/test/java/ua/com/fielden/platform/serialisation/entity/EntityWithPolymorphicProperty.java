package ua.com.fielden.platform.serialisation.entity;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@KeyTitle(value = "Entity No", desc = "Key Property")
public class EntityWithPolymorphicProperty extends AbstractEntity<String> {

    @IsProperty
    @Title("Polymorphyc property")
    private BaseEntity polyProperty;

    public BaseEntity getPolyProperty() {
        return polyProperty;
    }

    @Observable
    public void setPolyProperty(final BaseEntity polyProperty) {
        this.polyProperty = polyProperty;
    }


}
