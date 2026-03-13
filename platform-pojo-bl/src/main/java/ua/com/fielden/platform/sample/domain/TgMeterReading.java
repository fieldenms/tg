package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;

import java.util.Date;

@KeyTitle("Meter Reading")
@KeyType(DynamicEntityKey.class)
@MapEntityTo("METER_READING")
@CompanionObject(ITgMeterReading.class)
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

    @IsProperty
    @Required
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

    public TgFuelUsage getFuelUsage() {
        return fuelUsage;
    }

    /////////////////////////////////////////////
    //////////////// SETTERS ////////////////////
    /////////////////////////////////////////////

    @Observable
    public TgMeterReading setVehicle(final TgVehicle vehicle) {
        this.vehicle = vehicle;
        return this;
    }

    @Observable
    public TgMeterReading setReadingDate(final Date readingDate) {
        this.readingDate = readingDate;
        return this;
    }

    @Observable
    public TgMeterReading setReading(final Integer reading) {
        this.reading = reading;
        return this;
    }

    @Observable
    public TgMeterReading setWorkOrder(final TgWorkOrder workOrder) {
        this.workOrder = workOrder;
        return this;
    }

    @Observable
    public TgMeterReading setFuelUsage(final TgFuelUsage fuelUsage) {
        this.fuelUsage = fuelUsage;
        return this;
    }

}
