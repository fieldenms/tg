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
    private Money maxVehPrice;
    protected static final ExpressionModel maxVehPrice_ = expr().model(select(TeVehicle.class).where().prop("station").eq().extProp(ID).yield().maxOf().prop("price").modelAsPrimitive()).model();

    @IsProperty
    @Readonly
    @Calculated
    private Money maxVehPurchasePrice;
    protected static final ExpressionModel maxVehPurchasePrice_ = expr().model(select(TeVehicle.class).where().prop("station").eq().extProp(ID).yield().maxOf().prop("purchasePrice").modelAsPrimitive()).model();

    @Observable
    protected TgOrgUnit5 setMaxVehPurchasePrice(final Money maxVehPurchasePrice) {
        this.maxVehPurchasePrice = maxVehPurchasePrice;
        return this;
    }

    public Money getMaxVehPurchasePrice() {
        return maxVehPurchasePrice;
    }
    
    @Observable
    protected TgOrgUnit5 setMaxVehPrice(final Money maxVehPrice) {
        this.maxVehPrice = maxVehPrice;
        return this;
    }

    public Money getMaxVehPrice() {
        return maxVehPrice;
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