package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

@CompanionObject(TgReVehicleWithHighPriceCo.class)
public class TgReVehicleWithHighPrice extends TgVehicle {

    protected static final EntityResultQueryModel<TgReVehicleWithHighPrice> model_ = select(TgVehicle.class)
            .where().prop("price").ge().val(100000)
            .yieldAll()
            .modelAsEntity(TgReVehicleWithHighPrice.class);

    @IsProperty
    private TgVehicleTechDetails techDetails;

    @IsProperty
    private TgAverageFuelUsage averageFuelUsage;

    @Observable
    public TgReVehicleWithHighPrice setAverageFuelUsage(final TgAverageFuelUsage averageFuelUsage) {
        this.averageFuelUsage = averageFuelUsage;
        return this;
    }

    public TgAverageFuelUsage getAverageFuelUsage() {
        return averageFuelUsage;
    }

    @Observable
    public TgReVehicleWithHighPrice setTechDetails(final TgVehicleTechDetails techDetails) {
        this.techDetails = techDetails;
        return this;
    }

    public TgVehicleTechDetails getTechDetails() {
        return techDetails;
    }
}