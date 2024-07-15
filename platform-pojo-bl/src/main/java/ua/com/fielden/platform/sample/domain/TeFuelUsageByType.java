package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

@KeyType(DynamicEntityKey.class)
@CompanionObject(TeFuelUsageByTypeCo.class)
public class TeFuelUsageByType extends AbstractEntity<DynamicEntityKey> {
    protected static final EntityResultQueryModel<TeFuelUsageByType> model_ =
            select(TeVehicleFuelUsage.class).
                    groupBy().prop("vehicle").
                    groupBy().prop("fuelType").
                    yield().prop("vehicle").as("vehicle").
                    yield().prop("fuelType").as("fuelType").
                    yield().sumOf().prop("qty").as("qty").
                    modelAsEntity(TeFuelUsageByType.class);
    
    
    @IsProperty
    @Title("Vehicle")
    @CompositeKeyMember(1)
    private TeVehicle vehicle;
    
    @IsProperty
    @CompositeKeyMember(2)
    @Title(value = "Fuel type")
    private TgFuelType fuelType;
    
    @IsProperty
    @Title("Total qty per vehicle/fuel")
    private BigDecimal qty;
    
    @IsProperty
    @Readonly
    @Calculated(value="COUNT(SELF)", category = CalculatedPropertyCategory.AGGREGATED_EXPRESSION)
    private Integer countAll;

    @Observable
    protected TeFuelUsageByType setCountAll(final Integer countAll) {
        this.countAll = countAll;
        return this;
    }

    public Integer getCountAll() {
        return countAll;
    }
    
    @Observable
    public TeFuelUsageByType setQty(final BigDecimal qty) {
        this.qty = qty;
        return this;
    }

    public BigDecimal getQty() {
        return qty;
    }
    
    @Observable
    public TeFuelUsageByType setFuelType(final TgFuelType fuelType) {
        this.fuelType = fuelType;
        return this;
    }

    public TgFuelType getFuelType() {
        return fuelType;
    }

    @Observable
    public TeFuelUsageByType setVehicle(final TeVehicle vehicle) {
        this.vehicle = vehicle;
        return this;
    }

    public TeVehicle getVehicle() {
        return vehicle;
    }
}