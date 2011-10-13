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
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

@KeyTitle("Meter Reading")
@KeyType(DynamicEntityKey.class)
@MapEntityTo("METER_READING")
public class TgFuelUsage extends AbstractEntity<DynamicEntityKey> {
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


    /////////////////////////////////////////////
    //////////////// GETTERS ////////////////////
    /////////////////////////////////////////////

    public TgVehicle getVehicle() {
        return vehicle;
    }

    public Date getReadingDate() {
        return readingDate;
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

}
