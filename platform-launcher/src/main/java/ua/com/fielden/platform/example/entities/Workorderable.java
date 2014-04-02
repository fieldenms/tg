package ua.com.fielden.platform.example.entities;

import ua.com.fielden.platform.entity.AbstractUnionEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@EntityTitle(value = "Workorderable", desc = "Workorderable entity")
@KeyTitle(value = "Workorderable key")
@DescTitle(value = "Workorderable description", desc = "Description of Workorderable")
public class Workorderable extends AbstractUnionEntity {

    private static final long serialVersionUID = 7362243737334921917L;

    @IsProperty
    @Title("Vehicle")
    private Vehicle vehicle;

    @IsProperty
    @Title("Equipment")
    private FEquipment equipment;

    public Vehicle getVehicle() {
        return vehicle;
    }

    @Observable
    public void setVehicle(final Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public FEquipment getEquipment() {
        return equipment;
    }

    @Observable
    public void setEquipment(final FEquipment equipment) {
        this.equipment = equipment;
    }

}
