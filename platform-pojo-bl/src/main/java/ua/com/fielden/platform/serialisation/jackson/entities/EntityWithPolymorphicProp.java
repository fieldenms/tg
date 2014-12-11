package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * Entity class used for testing.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class EntityWithPolymorphicProp extends AbstractEntity<String> {

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
