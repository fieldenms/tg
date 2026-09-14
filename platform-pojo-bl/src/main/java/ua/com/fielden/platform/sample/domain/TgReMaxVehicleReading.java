package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/// A synthetic one-to-one association with [TgVehicle] that represents the maximum [reading][TgMeterReading] for a vehicle.
///
@KeyType(TgVehicle.class)
@CompanionObject(TgReMaxVehicleReadingCo.class)
public class TgReMaxVehicleReading extends AbstractEntity<TgVehicle> {

    protected static final EntityResultQueryModel<TgReMaxVehicleReading> model_ = //
            select(TgMeterReading.class). //
                    groupBy().prop("vehicle"). //
                    yield().prop("vehicle").as("key"). //
                    yield().maxOf().prop("reading").as("reading"). //
                    modelAsEntity(TgReMaxVehicleReading.class);

    @IsProperty
    private Integer reading;

    @Observable
    public TgReMaxVehicleReading setReading(final Integer reading) {
        this.reading = reading;
        return this;
    }

    public Integer getReading() {
        return reading;
    }
}
