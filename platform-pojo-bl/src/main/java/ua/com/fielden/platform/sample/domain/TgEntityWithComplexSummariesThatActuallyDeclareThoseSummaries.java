package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;

@KeyType(String.class)
@CompanionObject(TgEntityWithComplexSummariesThatActuallyDeclareThoseSummariesCo.class)
@MapEntityTo("TGENTITYWITHCOMPLEXSUMMARIES_")
public class TgEntityWithComplexSummariesThatActuallyDeclareThoseSummaries extends TgEntityWithComplexSummaries {

    @IsProperty
    @Readonly
    @Calculated(value="CASE WHEN SUM(kms) <> 0 THEN SUM(cost) / SUM(kms) END", category = CalculatedPropertyCategory.AGGREGATED_EXPRESSION)
    private BigDecimal costPerKm;

    @Observable
    protected TgEntityWithComplexSummariesThatActuallyDeclareThoseSummaries setCostPerKm(final BigDecimal costPerKm) {
        this.costPerKm = costPerKm;
        return this;
    }

    public BigDecimal getCostPerKm() {
        return costPerKm;
    }

}