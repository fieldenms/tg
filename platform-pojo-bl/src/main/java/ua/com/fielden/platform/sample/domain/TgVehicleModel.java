package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
@CompanionObject(ITgVehicleModel.class)
public class TgVehicleModel extends AbstractEntity<String> {

    @IsProperty
    @Required
    @MapTo
    @Title(value = "Test vehicle model", desc = "Test vehicle model")
    private TgVehicleMake make;
    
    @IsProperty
    @Calculated
    private Integer makeModelsCount;
    protected static final ExpressionModel makeModelsCount_ = expr().model(select(TgVehicleModel.class).where().prop("make").eq().extProp("make").yield().countAll().modelAsPrimitive()).model();

    @Observable
    protected TgVehicleModel setMakeModelsCount(final Integer makeModelsCount) {
        this.makeModelsCount = makeModelsCount;
        return this;
    }

    public Integer getMakeModelsCount() {
        return makeModelsCount;
    }
    
    @IsProperty
    @Title("Ordinary property")
    private Integer ordinaryIntProp;

    @Observable
    public TgVehicleModel setOrdinaryIntProp(final Integer ordinaryIntProp) {
        this.ordinaryIntProp = ordinaryIntProp;
        return this;
    }

    public Integer getOrdinaryIntProp() {
        return ordinaryIntProp;
    }

    @Observable
    public TgVehicleModel setMake(final TgVehicleMake make) {
        this.make = make;
        return this;
    }

    public TgVehicleMake getMake() {
        return make;
    }

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgVehicleModel() {
    }
}
