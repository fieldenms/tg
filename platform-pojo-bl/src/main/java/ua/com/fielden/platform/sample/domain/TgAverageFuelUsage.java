package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.IMoneyType;

import java.math.BigDecimal;
import java.util.Date;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

@KeyType(TgVehicle.class)
@CompanionObject(ITgAverageFuelUsage.class)
public class TgAverageFuelUsage extends AbstractEntity<TgVehicle> {

    protected static final EntityResultQueryModel<TgAverageFuelUsage> model_ = //
            select(TgFuelUsage.class). //
                    where(). //
                    prop("date").gt().iParam("datePeriod.from").and(). //
                    prop("date").lt().iParam("datePeriod.to"). //
                    groupBy().prop("vehicle"). //
                    yield().prop("vehicle").as("key"). //
                    yield().sumOf().prop("qty").as("qty"). //
                    yield().sumOf().beginExpr().prop("qty").mult().prop("pricePerLitre").endExpr().as("cost"). //
                    modelAsEntity(TgAverageFuelUsage.class);

    @IsProperty
    @Title("Total qty over the period")
    private BigDecimal qty;

    @IsProperty
    @Title("Total cost over the period")
    @PersistentType(userType = IMoneyType.class)
    private Money cost;
    
    @IsProperty
    @CritOnly
    @Title("Date period")
    private Date datePeriod;

    @Observable
    public TgAverageFuelUsage setDatePeriod(final Date datePeriod) {
        this.datePeriod = datePeriod;
        return this;
    }

    public Date getDatePeriod() {
        return datePeriod;
    }

    @Observable
    public TgAverageFuelUsage setQty(final BigDecimal qty) {
        this.qty = qty;
        return this;
    }
    
    public BigDecimal getQty() {
        return qty;
    }

    @Observable
    public TgAverageFuelUsage setCost(final Money cost) {
        this.cost = cost;
        return this;
    }

    public Money getCost() {
        return cost;
    }
}
