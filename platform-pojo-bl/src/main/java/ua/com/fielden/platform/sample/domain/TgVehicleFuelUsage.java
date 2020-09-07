package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

@KeyType(DynamicEntityKey.class)
@CompanionObject(ITgVehicleFuelUsage.class)
public class TgVehicleFuelUsage extends AbstractEntity<DynamicEntityKey> {

    private static final EntityResultQueryModel<TgVehicleFuelUsage> model_ = //
            select(TgFuelUsage.class). //
                    where(). //
                    prop("date").gt().iParam("datePeriod.from").and(). //
                    prop("date").lt().iParam("datePeriod.to"). //
                    groupBy().prop("vehicle"). //
                    groupBy().prop("fuelType"). //
                    yield().prop("vehicle").as("vehicle"). //
                    yield().prop("fuelType").as("fuelType"). //
                    yield().sumOf().prop("qty").as("qty"). //
                    modelAsEntity(TgVehicleFuelUsage.class);

    @IsProperty
    @CompositeKeyMember(1)
    private TgVehicle vehicle;

    @IsProperty
    @CompositeKeyMember(2)
    private TgFuelType fuelType;
    
    @IsProperty
    @Title("Total qty over the period")
    private BigDecimal qty;

    @IsProperty
    @CritOnly
    @Title("Date period")
    private Date datePeriod;
    
    @Observable
    public TgVehicleFuelUsage setFuelType(final TgFuelType fuelType) {
        this.fuelType = fuelType;
        return this;
    }

    public TgFuelType getFuelType() {
        return fuelType;
    }

    public TgVehicle getVehicle() {
        return vehicle;
    }

    @Observable
    public TgVehicleFuelUsage setVehicle(final TgVehicle vehicle) {
        this.vehicle = vehicle;
        return this;
    }
    
    @Observable
    public TgVehicleFuelUsage setDatePeriod(final Date datePeriod) {
        this.datePeriod = datePeriod;
        return this;
    }

    public Date getDatePeriod() {
        return datePeriod;
    }

    @Observable
    public TgVehicleFuelUsage setQty(final BigDecimal qty) {
        this.qty = qty;
        return this;
    }

    public BigDecimal getQty() {
        return qty;
    }
}