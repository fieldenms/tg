package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.CompanionObject;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

@KeyTitle("Fuel Usages")
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(ITgFuelUsage.class)
public class TgFuelUsage extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty @MapTo @Title("Vehicle")
    @CompositeKeyMember(1)
    private TgVehicle vehicle;

    @IsProperty @MapTo @Title("Purchase Date")
    @CompositeKeyMember(2)
    private Date date;

    @IsProperty @Required @MapTo @Title(value = "Fuel Qty", desc = "Fuel Qty")
    private BigDecimal qty;

    @IsProperty @Required @MapTo @Title(value = "Fuel type", desc = "Fuel type")
    private TgFuelType fuelType;

    @Observable
    public TgFuelUsage setFuelType(final TgFuelType fuelType) {
	this.fuelType = fuelType;
	return this;
    }

    public TgFuelType getFuelType() {
	return fuelType;
    }

    public BigDecimal getQty() {
	return qty;
    }

    public TgVehicle getVehicle() {
        return vehicle;
    }

    public Date getDate() {
        return date;
    }

    @Observable
    @EntityExists(TgVehicle.class)
    public void setVehicle(final TgVehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Observable
    public void setDate(final Date date) {
        this.date = date;
    }

    @Observable
    public TgFuelUsage setQty(final BigDecimal qty) {
	this.qty = qty;
	return this;
    }
}