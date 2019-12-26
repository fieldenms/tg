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
import ua.com.fielden.platform.entity.annotation.Title;
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
    @Title(value = "Test vehicle model", desc = "Test vehicle model")
    private TeVehicleMake make;
    
    @IsProperty
    @Title("Ordinary property")
    private Integer ordinaryIntProp;

    @IsProperty
    @Calculated
    @Title(value = "Title", desc = "Desc")
    private String makeKey;
    protected static final ExpressionModel makeKey_ = expr().prop("make.key").model();

    @Observable
    protected TeVehicleModel setMakeKey(final String makeKey) {
        this.makeKey = makeKey;
        return this;
    }

    public String getMakeKey() {
        return makeKey;
    }
    
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
    public TeVehicleModel setOrdinaryIntProp(final Integer ordinaryIntProp) {
        this.ordinaryIntProp = ordinaryIntProp;
        return this;
    }

    public Integer getOrdinaryIntProp() {
        return ordinaryIntProp;
    }

    @Observable
    public TeVehicleModel setMake(final TeVehicleMake make) {
        this.make = make;
        return this;
    }

    public TeVehicleMake getMake() {
        return make;
    }

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TeVehicleModel() {
    }
}
