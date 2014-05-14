package ua.com.fielden.platform.example.swing.booking;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;

@KeyTitle(value = "Book No", desc = "Booking number")
@KeyType(DynamicEntityKey.class)
@DescTitle(value = "Description", desc = "Booking description")
@EntityTitle(value = "Booking", desc = "Domain entity representing an work request.")
public class BookingEntity extends AbstractEntity<String> {

    private static final long serialVersionUID = 616658360097771693L;

    @IsProperty
    @MapTo
    @Title(value = "Vehicle", desc = "Vehicle key part member")
    @CompositeKeyMember(1)
    private VehicleEntity vehicleEntity;

    @Observable
    @EntityExists(VehicleEntity.class)
    public BookingEntity setVehicleEntity(final VehicleEntity vehicleEntity) {
	this.vehicleEntity = vehicleEntity;
	return this;
    }

    public VehicleEntity getVehicleEntity() {
	return vehicleEntity;
    }

    @IsProperty
    @MapTo
    @Title(value = "Booking start", desc = "Booked on date")
    @CompositeKeyMember(2)
    private Date bookingStart;

    @Observable
    public BookingEntity setBookingStart(final Date bookingStart) {
	this.bookingStart = bookingStart;
	return this;
    }

    public Date getBookingStart() {
	return bookingStart;
    }

    @IsProperty
    @MapTo
    @Title(value = "Booking finish", desc = "Booked to date")
    private Date bookingFinish;

    @Observable
    public BookingEntity setBookingFinish(final Date bookingFinish) {
	this.bookingFinish = bookingFinish;
	return this;
    }

    public Date getBookingFinish() {
	return bookingFinish;
    }

    @IsProperty
    @MapTo
    @Title(value = "Actual start", desc = "Actual start")
    private Date actStart;

    @Observable
    public BookingEntity setActStart(final Date actStart) {
	this.actStart = actStart;
	return this;
    }

    public Date getActStart() {
	return actStart;
    }

    @IsProperty
    @MapTo
    @Title(value = "Actual finish", desc = "Actual finish")
    private Date actFinish;

    @Observable
    public BookingEntity setActFinish(final Date actFinish) {
	this.actFinish = actFinish;
	return this;
    }

    public Date getActFinish() {
	return actFinish;
    }
}
