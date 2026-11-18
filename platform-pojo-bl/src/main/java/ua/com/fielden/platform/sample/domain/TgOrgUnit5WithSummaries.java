package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.ISimpleMoneyType;

@MapEntityTo("TGORGUNIT5_")
@CompanionObject(TgOrgUnit5WithSummariesCo.class)
public class TgOrgUnit5WithSummaries extends TgOrgUnit5 {

    @IsProperty
    @Readonly
    @Calculated(value="MIN(vehicleCount)", category = CalculatedPropertyCategory.AGGREGATED_EXPRESSION)
    private Integer minVehiclesCount;

    @IsProperty
    @Readonly
    @Calculated(value="MAX(averageVehPrice)", category = CalculatedPropertyCategory.AGGREGATED_EXPRESSION)
    @PersistentType(userType = ISimpleMoneyType.class)
    private Money maxAverageVehiclePrice;

    @Observable
    protected TgOrgUnit5WithSummaries setMaxAverageVehiclePrice(final Money maxAverageVehiclePrice) {
        this.maxAverageVehiclePrice = maxAverageVehiclePrice;
        return this;
    }

    public Money getMaxAverageVehiclePrice() {
        return maxAverageVehiclePrice;
    }

    @Observable
    protected TgOrgUnit5WithSummaries setMinVehiclesCount(final Integer minVehiclesCount) {
        this.minVehiclesCount = minVehiclesCount;
        return this;
    }

    public Integer getMinVehiclesCount() {
        return minVehiclesCount;
    }
}
