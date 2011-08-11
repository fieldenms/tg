package ua.com.fielden.platform.entity.union;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

public class UnionEntity extends AbstractUnionEntity {

    @Title(value = "Prop One", desc = "Desc")
    @IsProperty
    private EntityOne propertyOne;

    @Title(value = "Prop Two", desc = "Desc")
    @IsProperty
    private EntityTwo propertyTwo;

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
}
