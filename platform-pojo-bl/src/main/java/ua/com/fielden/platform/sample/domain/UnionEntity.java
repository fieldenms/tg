package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

@DenyIntrospection
@CompanionObject(IUnionEntity.class)
public class UnionEntity extends AbstractUnionEntity {

    public enum Property implements IConvertableToPath {
        propertyOne, propertyTwo;

        @Override public String toPath() { return name(); }
    }

    @Title(value = "Prop One", desc = "Desc")
    @IsProperty
    @MapTo
    private EntityOne propertyOne;

    @Title(value = "Prop Two", desc = "Desc")
    @IsProperty
    @MapTo
    @SkipEntityExistsValidation(skipNew = true)
    private EntityTwo propertyTwo;

    public EntityOne getPropertyOne() {
        return propertyOne;
    }

    @Observable
    public UnionEntity setPropertyOne(final EntityOne propertyOne) {
        this.propertyOne = propertyOne;
        return this;
    }

    public EntityTwo getPropertyTwo() {
        return propertyTwo;
    }

    @Observable
    public UnionEntity setPropertyTwo(final EntityTwo propertyTwo) {
        this.propertyTwo = propertyTwo;
        return this;
    }
}
