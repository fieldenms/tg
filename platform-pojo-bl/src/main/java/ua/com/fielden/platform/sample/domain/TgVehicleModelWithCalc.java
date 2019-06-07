package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

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
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
@CompanionObject(ITgVehicleModel.class)
public class TgVehicleModelWithCalc extends AbstractEntity<String> {

    @IsProperty
    @Required
    @MapTo
    @Title(value = "Test vehicle model", desc = "Test vehicle model")
    private TgVehicleMake make;
    
    @IsProperty
    @Title("Ordinary property")
    @Calculated
    private Integer calcProp;
    protected static final ExpressionModel calcProp_ = expr().val(1).model();

    @Observable
    public TgVehicleModelWithCalc setCalcProp(final Integer calcProp) {
        this.calcProp = calcProp;
        return this;
    }

    public Integer getCalcProp() {
        return calcProp;
    }

    @Observable
    public TgVehicleModelWithCalc setMake(final TgVehicleMake make) {
        this.make = make;
        return this;
    }

    public TgVehicleMake getMake() {
        return make;
    }
}