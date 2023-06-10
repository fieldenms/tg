package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DenyIntrospection;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@DenyIntrospection
@CompanionObject(IUnionEntity.class)
public class UnionEntity extends AbstractUnionEntity {

    @Title(value = "Prop One", desc = "Desc")
    @IsProperty
    @MapTo
    private EntityOne propertyOne;

    @Title(value = "Prop Two", desc = "Desc")
    @IsProperty
    @MapTo
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
