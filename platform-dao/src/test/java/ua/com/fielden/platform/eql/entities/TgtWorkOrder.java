package ua.com.fielden.platform.eql.entities;

import java.math.BigDecimal;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;

@KeyType(String.class)
@MapEntityTo
public class TgtWorkOrder extends AbstractEntity<String> {
    @IsProperty
    @MapTo
    private TgtVehicle vehicle;

    @IsProperty
    @MapTo
    private BigDecimal actCost;

    @IsProperty
    @MapTo
    private BigDecimal estCost;

    @IsProperty
    @MapTo
    private BigDecimal yearlyCost;

    @Observable
    public TgtWorkOrder setYearlyCost(final BigDecimal yearlyCost) {
        this.yearlyCost = yearlyCost;
        return this;
    }

    public BigDecimal getYearlyCost() {
        return yearlyCost;
    }

    @Observable
    public TgtWorkOrder setEstCost(final BigDecimal estCost) {
        this.estCost = estCost;
        return this;
    }

    public BigDecimal getEstCost() {
        return estCost;
    }

    @Observable
    public TgtWorkOrder setActCost(final BigDecimal actCost) {
        this.actCost = actCost;
        return this;
    }

    public BigDecimal getActCost() {
        return actCost;
    }

    public TgtVehicle getVehicle() {
        return vehicle;
    }

    @Observable
    public void setVehicle(final TgtVehicle vehicle) {
        this.vehicle = vehicle;
    }
}
