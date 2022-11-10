package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;

@MapEntityTo("TGORGUNIT5_")
@CompanionObject(TgOrgUnit5WithSummariesCo.class)
public class TgOrgUnit5WithSummaries extends TgOrgUnit5 {
    
    @IsProperty
    @Readonly
    @Calculated(value="MIN(vehicleCount)", category = CalculatedPropertyCategory.AGGREGATED_EXPRESSION)
    private Integer minVehiclesCount;

    @Observable
    protected TgOrgUnit5WithSummaries setMinVehiclesCount(final Integer minVehiclesCount) {
        this.minVehiclesCount = minVehiclesCount;
        return this;
    }

    public Integer getMinVehiclesCount() {
        return minVehiclesCount;
    }
}