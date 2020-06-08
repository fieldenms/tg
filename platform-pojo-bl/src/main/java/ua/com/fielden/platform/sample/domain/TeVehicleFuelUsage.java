package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.types.Money;

@KeyTitle("Fuel Usages")
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(ITeVehicleFuelUsage.class)
public class TeVehicleFuelUsage extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title("Vehicle")
    @CompositeKeyMember(1)
    private TeVehicle vehicle;

    @IsProperty
    @MapTo
    @Title("Purchase Date")
    @CompositeKeyMember(2)
    private Date date;

    @IsProperty
    @Required
    @MapTo
    @Title(value = "Fuel Qty", desc = "Fuel Qty")
    private BigDecimal qty;

    @IsProperty
    @Required
    @MapTo
    @Title(value = "Fuel type", desc = "Fuel type")
    private TgFuelType fuelType;
    
    @IsProperty
    @MapTo
    @Title(value = "Fuel cost")
    private Money cost;

    @Observable
    public TeVehicleFuelUsage setCost(final Money cost) {
        this.cost = cost;
        return this;
    }

    public Money getCost() {
        return cost;
    }

    


    @Observable
    public TeVehicleFuelUsage setFuelType(final TgFuelType fuelType) {
        this.fuelType = fuelType;
        return this;
    }

    public TgFuelType getFuelType() {
        return fuelType;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public TeVehicle getVehicle() {
        return vehicle;
    }

    public Date getDate() {
        return date;
    }

    @Observable
    public void setVehicle(final TeVehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Observable
    public void setDate(final Date date) {
        this.date = date;
    }

    @Observable
    public TeVehicleFuelUsage setQty(final BigDecimal qty) {
        this.qty = qty;
        return this;
    }
}