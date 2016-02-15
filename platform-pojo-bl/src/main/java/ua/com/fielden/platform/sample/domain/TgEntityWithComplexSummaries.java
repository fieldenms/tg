package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;

@KeyType(String.class)
@CompanionObject(ITgEntityWithComplexSummaries.class)
@MapEntityTo
public class TgEntityWithComplexSummaries extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    private Integer kms;
    
    @IsProperty
    @MapTo
    private Integer cost;
    
    @IsProperty
    @Readonly
    @Calculated(value="CASE WHEN SUM(kms) <> 0 THEN SUM(cost) / SUM(kms) END", category = CalculatedPropertyCategory.AGGREGATED_EXPRESSION)
    private BigDecimal costPerKm;

    @Observable
    protected TgEntityWithComplexSummaries setCostPerKm(final BigDecimal costPerKm) {
        this.costPerKm = costPerKm;
        return this;
    }

    public BigDecimal getCostPerKm() {
        return costPerKm;
    }

    @Observable
    public TgEntityWithComplexSummaries setCost(final Integer cost) {
        this.cost = cost;
        return this;
    }

    public Integer getCost() {
        return cost;
    }

    @Observable
    public TgEntityWithComplexSummaries setKms(final Integer kms) {
        this.kms = kms;
        return this;
    }

    public Integer getKms() {
        return kms;
    }
}