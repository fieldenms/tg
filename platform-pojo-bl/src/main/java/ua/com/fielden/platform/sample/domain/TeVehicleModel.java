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
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
@CompanionObject(ITeVehicleModel.class)
public class TeVehicleModel extends AbstractEntity<String> {

    @IsProperty
    @Required
    @MapTo
    private TeVehicleMake make;
    
    @IsProperty
    @Calculated
    private String makeKey;
    protected static final ExpressionModel makeKey_ = expr().prop("make.key").model();

    @IsProperty
    @Calculated
    private String makeKey2;
    protected static final ExpressionModel makeKey2_ = expr().model(select(TeVehicleMake.class).where().prop("id").eq().extProp("make").yield().prop("key").modelAsPrimitive()).model();

    @Observable
    protected TeVehicleModel setMakeKey2(final String makeKey2) {
        this.makeKey2 = makeKey2;
        return this;
    }

    public String getMakeKey2() {
        return makeKey2;
    }
    
    @Observable
    public TeVehicleModel setMake(final TeVehicleMake make) {
        this.make = make;
        return this;
    }

    public TeVehicleMake getMake() {
        return make;
    }

    @Observable
    protected TeVehicleModel setMakeKey(final String makeKey) {
        this.makeKey = makeKey;
        return this;
    }

    public String getMakeKey() {
        return makeKey;
    }
}
