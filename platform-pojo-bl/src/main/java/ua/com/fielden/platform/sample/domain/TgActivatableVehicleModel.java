package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.ActivatableAbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
public class TgActivatableVehicleModel extends ActivatableAbstractEntity<String> {

    @IsProperty
    @Required
    @MapTo
    @Title(value = "Test vehicle model", desc = "Test vehicle model")
    private TgVehicleMake make;
    
    @IsProperty
    @Calculated
    private Integer makeModelsCount;
    protected static final ExpressionModel makeModelsCount_ = expr().model(select(TgActivatableVehicleModel.class).where().prop("make").eq().extProp("make").yield().countAll().modelAsPrimitive()).model();

    @IsProperty
    @Title("Ordinary property")
    private Integer ordinaryIntProp;

    @Observable
    protected TgActivatableVehicleModel setMakeModelsCount(final Integer makeModelsCount) {
        this.makeModelsCount = makeModelsCount;
        return this;
    }

    public Integer getMakeModelsCount() {
        return makeModelsCount;
    }

    @Observable
    public TgActivatableVehicleModel setOrdinaryIntProp(final Integer ordinaryIntProp) {
        this.ordinaryIntProp = ordinaryIntProp;
        return this;
    }

    public Integer getOrdinaryIntProp() {
        return ordinaryIntProp;
    }

    @Observable
    public TgActivatableVehicleModel setMake(final TgVehicleMake make) {
        this.make = make;
        return this;
    }

    public TgVehicleMake getMake() {
        return make;
    }

}
