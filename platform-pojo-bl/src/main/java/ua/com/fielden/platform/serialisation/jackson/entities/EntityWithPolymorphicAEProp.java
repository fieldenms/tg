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
public class EntityWithPolymorphicAEProp extends AbstractEntity<String> {

    @IsProperty
    @Title("Polymorphyc property")
    private AbstractEntity<?> polyProperty;

    public AbstractEntity<?> getPolyProperty() {
        return polyProperty;
    }

    @Observable
    public void setPolyProperty(final AbstractEntity<?> polyProperty) {
        this.polyProperty = polyProperty;
    }

}
