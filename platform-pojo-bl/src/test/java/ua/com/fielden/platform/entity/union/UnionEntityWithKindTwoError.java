package ua.com.fielden.platform.entity.union;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

public class UnionEntityWithKindTwoError extends AbstractUnionEntity {
    @IsProperty
    @Title(value = "Property One", desc = "Desc")
    private EntityOne propertyOne;

    @IsProperty
    @Title(value = "Property Two", desc = "Desc")
    private EntityTwo propertyTwo;

    @IsProperty
    @Title(value = "Property Three", desc = "Desc")
    private EntityTwo propertyThree;

    public EntityOne getPropertyOne() {
        return propertyOne;
    }

    @Observable
    public void setPropertyOne(final EntityOne propertyOne) {
        this.propertyOne = propertyOne;
    }

    public EntityTwo getPropertyTwo() {
        return propertyTwo;
    }

    @Observable
    public void setPropertyTwo(final EntityTwo propertyTwo) {
        this.propertyTwo = propertyTwo;
    }

    public EntityTwo getPropertyThree() {
        return propertyThree;
    }

    @Observable
    public void setPropertyThree(final EntityTwo propertyThree) {
        this.propertyThree = propertyThree;
    }

}
