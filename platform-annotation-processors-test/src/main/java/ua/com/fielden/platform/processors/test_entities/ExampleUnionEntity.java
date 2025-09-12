package ua.com.fielden.platform.processors.test_entities;

import ua.com.fielden.platform.annotations.metamodel.DomainEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

/**
 * An example union entity for testing purposes.
 *
 * @author TG Team
 */
@DomainEntity
public class ExampleUnionEntity extends AbstractUnionEntity {

    @Title(value = "Prop One", desc = "Desc")
    @IsProperty
    @MapTo
    private ExampleEntity prop1;

    @Title(value = "Prop Two", desc = "Desc")
    @IsProperty
    @MapTo
    private PersistentEntity prop2;

    public ExampleEntity getProp1() {
        return prop1;
    }

    @Observable
    public void setProp1(final ExampleEntity propertyOne) {
        this.prop1 = propertyOne;
    }

    public PersistentEntity getProp2() {
        return prop2;
    }

    @Observable
    public void setProp2(final PersistentEntity propertyTwo) {
        this.prop2 = propertyTwo;
    }

}