package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DenyIntrospection;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

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
    public void setPropertyOne(final EntityOne propertyOne) {
        this.propertyOne = propertyOne;
    }

    public EntityThree getPropertyThree() {
        return propertyThree;
    }

    @Observable
    public void setPropertyThree(final EntityThree propertyThree) {
        this.propertyThree = propertyThree;
    }
}
