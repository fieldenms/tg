package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;

@KeyType(TgVehicle.class)
@KeyTitle(value = "Vehicle", desc = "Vehicle")
@MapEntityTo
@CompanionObject(TgVehicleTechDetailsCo.class)
public class TgVehicleTechDetails extends AbstractEntity<TgVehicle> {

    @IsProperty
    @MapTo
    private TgFuelType fuelType;
    
    @IsProperty
    @MapTo
    private Integer tankCapacity;

    @Observable
    public TgVehicleTechDetails setTankCapacity(final Integer tankCapacity) {
        this.tankCapacity = tankCapacity;
        return this;
    }

    public Integer getTankCapacity() {
        return tankCapacity;
    }

    @Observable
    public TgVehicleTechDetails setFuelType(final TgFuelType fuelType) {
        this.fuelType = fuelType;
        return this;
    }

    public TgFuelType getFuelType() {
        return fuelType;
    }
}