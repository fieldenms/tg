package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.*;

@DenyIntrospection
@CompanionObject(UnionEntityWithoutSecondDescTitleCo.class)
public class UnionEntityWithoutSecondDescTitle extends AbstractUnionEntity {

    @Title(value = "Prop One", desc = "Desc")
    @IsProperty
    private EntityOne propertyOne;

    @Title(value = "Prop Three", desc = "Desc")
    @IsProperty
    private EntityThree propertyThree;

    public EntityOne getPropertyOne() {
        return propertyOne;
    }

    @Observable
    public UnionEntityWithoutSecondDescTitle setPropertyOne(final EntityOne propertyOne) {
        this.propertyOne = propertyOne;
        return this;
    }

    public EntityThree getPropertyThree() {
        return propertyThree;
    }

    @Observable
    public UnionEntityWithoutSecondDescTitle setPropertyThree(final EntityThree propertyThree) {
        this.propertyThree = propertyThree;
        return this;
    }

}
