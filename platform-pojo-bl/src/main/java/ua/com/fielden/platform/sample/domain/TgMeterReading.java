package ua.com.fielden.platform.sample.domain;

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
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

@KeyTitle("Meter Reading")
@KeyType(DynamicEntityKey.class)
@MapEntityTo("METER_READING")
public class TgMeterReading extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title("Vehicle")
    @CompositeKeyMember(1)
    @MapTo("ID_EQDET")
    private TgVehicle vehicle;
    @IsProperty
    @Title(value = "Reading Date", desc = "Reading Date")
    @CompositeKeyMember(2)
    @MapTo("LAST_READING_DATE")
    private Date readingDate;

    @IsProperty @Required
    @Title(value = "Reading", desc = "Reading")
    @MapTo("LAST_READING")
    private Integer reading;
    @IsProperty
    @Title(value = "Work Order")
    @MapTo("ID_WODET")
    private TgWorkOrder workOrder;
    @IsProperty
    @Title(value = "Fuel Usage", desc = "Fuel usage instance associated with this meter reading")
    @MapTo("ID_FUEL_USAGE")
    private TgFuelUsage fuelUsage;

    /////////////////////////////////////////////
    //////////////// GETTERS ////////////////////
    /////////////////////////////////////////////

    public TgVehicle getVehicle() {
        return vehicle;
    }

    public Date getReadingDate() {
        return readingDate;
    }

    public Integer getReading() {
        return reading;
    }

    public TgWorkOrder getWorkOrder() {
        return workOrder;
    }

    /////////////////////////////////////////////
    //////////////// SETTERS ////////////////////
    /////////////////////////////////////////////

    @Observable
    @EntityExists(TgVehicle.class)
    public void setVehicle(final TgVehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Observable
    public void setReadingDate(final Date readingDate) {
        this.readingDate = readingDate;
    }

    @Observable
    public void setReading(final Integer reading) {
        this.reading = reading;
    }

    @Observable
    @EntityExists(TgWorkOrder.class)
    public void setWorkOrder(final TgWorkOrder workOrder) {
        this.workOrder = workOrder;
    }

    public TgFuelUsage getFuelUsage() {
        return fuelUsage;
    }

    @Observable
    @EntityExists(TgFuelUsage.class)
    public void setFuelUsage(final TgFuelUsage fuelUsage) {
        this.fuelUsage = fuelUsage;
    }

}
