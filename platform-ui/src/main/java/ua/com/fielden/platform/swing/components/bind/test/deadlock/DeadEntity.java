package ua.com.fielden.platform.swing.components.bind.test.deadlock;

import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.DomainValidation;
import ua.com.fielden.platform.entity.validation.annotation.GeProperty;
import ua.com.fielden.platform.entity.validation.annotation.LeProperty;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.error.Result;

/**
 * Entity designed to emulate deadlocks.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
public class DeadEntity extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    public final static String PROPERTY_VEHICLE = "vehicle";
    public final static String PROPERTY_ODOMETER = "odometerReading";
    public final static String PROPERTY_ACT_ST = "actualStart";
    public final static String PROPERTY_ACT_FIN = "actualFinish";

    @IsProperty
    @Dependent("odometerReading")
    private String vehicle;

    @IsProperty
    @Dependent("actualStart")
    private Integer odometerReading = 20;

    @IsProperty
    @Dependent({"actualFinish", "odometerReading"})
    private Date actualStart;

    @IsProperty
    @Dependent("actualStart")
    private Date actualFinish;

    // =========================================
    public String getVehicle() {
        return vehicle;
    }

    public Integer getOdometerReading() {
        return odometerReading;
    }

    public Date getActualStart() {
        return actualStart;
    }

    public Date getActualFinish() {
        return actualFinish;
    }

    // =========================================

    @Observable
    @NotNull
    @DomainValidation
    public void setVehicle(final String vehicle) {
	this.vehicle = vehicle;
	System.out.println("vehicle := " + vehicle);
    }

    @Observable
    @NotNull
    @DomainValidation
    public DeadEntity setOdometerReading(final Integer odometerReading) {
	this.odometerReading = odometerReading;
	System.out.println("odometerReading := " + odometerReading);
	return this;
    }

    @Observable
    @DomainValidation
    @LeProperty("actualFinish")
    public void setActualStart(final Date actualStart) throws Result {
	this.actualStart = actualStart;
	System.out.println("actualStart := " + actualStart);
    }

    @Observable
    @GeProperty("actualStart")
    public void setActualFinish(final Date actualFinish) throws Result {
	this.actualFinish = actualFinish;
	System.out.println("actualFinish := " + actualFinish);
    }

}