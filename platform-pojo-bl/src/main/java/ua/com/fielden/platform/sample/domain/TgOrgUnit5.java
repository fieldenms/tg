package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.types.Money;

@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(ITgOrgUnit5.class)
public class TgOrgUnit5 extends AbstractEntity<DynamicEntityKey> {

    @IsProperty
    @Required
    @MapTo
    @Title(value = "Parent", desc = "Parent")
    @CompositeKeyMember(1)
    private TgOrgUnit4 parent;

    @IsProperty
    @MapTo
    @Title(value = "Name", desc = "Desc")
    @CompositeKeyMember(2)
    private String name;

    @IsProperty
    @MapTo
    @Title(value = "Fuel Type", desc = "Desc")
    private TgFuelType fuelType;

    @IsProperty
    @Readonly
    @Calculated
    private Money averageVehPrice;
    protected static final ExpressionModel averageVehPrice_ = expr().model(select(TgVehicle.class).where().prop("station").eq().extProp(ID).yield().avgOf().prop("price").modelAsPrimitive()).model();

    @IsProperty
    @Readonly
    @Calculated
    private Money averageVehPurchasePrice;
    protected static final ExpressionModel averageVehPurchasePrice_ = expr().model(select(TgVehicle.class).where().prop("station").eq().extProp(ID).yield().avgOf().prop("purchasePrice").modelAsPrimitive()).model();

    @IsProperty
    @Readonly
    @Calculated
    @Title(value = "Station vehicles count")
    private Integer vehicleCount;
    protected static final ExpressionModel vehicleCount_ = expr().model(select(TgVehicle.class).where().prop("station").eq().extProp(ID).yield().countAll().modelAsPrimitive()).model();

    @Observable
    protected TgOrgUnit5 setVehicleCount(final Integer vehicleCount) {
        this.vehicleCount = vehicleCount;
        return this;
    }

    public Integer getVehicleCount() {
        return vehicleCount;
    }

    @Observable
    protected TgOrgUnit5 setAverageVehPurchasePrice(final Money averageVehPurchasePrice) {
        this.averageVehPurchasePrice = averageVehPurchasePrice;
        return this;
    }

    public Money getAverageVehPurchasePrice() {
        return averageVehPurchasePrice;
    }

    @Observable
    protected TgOrgUnit5 setAverageVehPrice(final Money averageVehPrice) {
        this.averageVehPrice = averageVehPrice;
        return this;
    }

    public Money getAverageVehPrice() {
        return averageVehPrice;
    }

    @Observable
    public TgOrgUnit5 setFuelType(final TgFuelType fuelType) {
        this.fuelType = fuelType;
        return this;
    }

    public TgFuelType getFuelType() {
        return fuelType;
    }

    @Observable
    public TgOrgUnit5 setName(final String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    @Observable
    public TgOrgUnit5 setParent(final TgOrgUnit4 parent) {
        this.parent = parent;
        return this;
    }

    public TgOrgUnit4 getParent() {
        return parent;
    }
}