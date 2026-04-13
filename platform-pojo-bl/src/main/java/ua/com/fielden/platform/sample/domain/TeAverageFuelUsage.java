package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.IMoneyType;

import java.math.BigDecimal;
import java.util.Date;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

@KeyType(TeVehicle.class)
@CompanionObject(ITeAverageFuelUsage.class)
public class TeAverageFuelUsage extends AbstractEntity<TeVehicle> {

    protected static final EntityResultQueryModel<TeAverageFuelUsage> model_ =
            select(TeVehicleFuelUsage.class).where()
                    .prop("date").gt().iParam("datePeriod.from")
                    .and()
                    .prop("date").lt().iParam("datePeriod.to")
                    .groupBy().prop("vehicle")
                    .yield().prop("vehicle").as("id")
                    .yield().prop("vehicle").as("key")
                    .yield().sumOf().prop("qty").as("qty")
                    .yield().sumOf().prop("cost.amount").as("cost.amount")
                    // An example of how currency can and must be yielded in aggregation queries.
                    // Here, we select the first currency for each vehicle group.
                    .yield().model(select(TeVehicleFuelUsage.class).where()
                                           .prop("vehicle").eq().extProp("vehicle")
                                           .and()
                                           .prop("cost.currency").isNotNull()
                                           .orderBy().prop("id").asc()
                                           .limit(1)
                                           .yield().prop("cost.currency")
                                           .modelAsPrimitive())
                    .as("cost.currency")
                    .modelAsEntity(TeAverageFuelUsage.class);


    @IsProperty
    @Title("Total qty over the period")
    private BigDecimal qty;
    
    @IsProperty
    @PersistentType(userType = IMoneyType.class)
    @Title(value = "Title", desc = "Desc")
    private Money cost;

    @IsProperty
    @CritOnly
    @Title("Date period")
    private Date datePeriod;
    
    @IsProperty
    @Readonly
    @Calculated(value="COUNT(SELF)", category = CalculatedPropertyCategory.AGGREGATED_EXPRESSION)
    private Integer countAll;

    @Observable
    protected TeAverageFuelUsage setCountAll(final Integer countAll) {
        this.countAll = countAll;
        return this;
    }

    public Integer getCountAll() {
        return countAll;
    }

    @Observable
    public TeAverageFuelUsage setCost(final Money cost) {
        this.cost = cost;
        return this;
    }

    public Money getCost() {
        return cost;
    }

    @Observable
    public TeAverageFuelUsage setDatePeriod(final Date datePeriod) {
        this.datePeriod = datePeriod;
        return this;
    }

    public Date getDatePeriod() {
        return datePeriod;
    }

    @Observable
    public TeAverageFuelUsage setQty(final BigDecimal qty) {
        this.qty = qty;
        return this;
    }

    public BigDecimal getQty() {
        return qty;
    }

    // Not used, but kept as an example.
    private static EntityResultQueryModel<TeAverageFuelUsage> modelWithWrapping() {
        final var subModel = select(TeVehicleFuelUsage.class).where()
                .prop("date").gt().iParam("datePeriod.from")
                .and()
                .prop("date").lt().iParam("datePeriod.to")
                .groupBy().prop("vehicle")
                .yield().prop("vehicle").as("id")
                .yield().prop("vehicle").as("key")
                .yield().sumOf().prop("qty").as("qty")
                .yield().sumOf().prop("cost.amount").as("cost.amount")
                .modelAsEntity(TeAverageFuelUsage.class);
        // Another example of yielding currency in an aggregation context.
        // Here, we wrap the original synthetic model and select the first currency for each vehicle group.
        // Notably, this query is more complex as it requires each yield to be repeated (yieldAll cannot be reliably used
        // as it may discard the extra "cost.currency" yield).
        return select(subModel)
                .yield().prop("id").as("id")
                .yield().prop("key").as("key")
                .yield().prop("qty").as("qty")
                .yield().prop("cost.amount").as("cost.amount")
                .yield().model(select(TeVehicleFuelUsage.class).where()
                                       .prop("vehicle").eq().extProp("id")
                                       .and()
                                       .prop("cost.currency").isNotNull()
                                       .orderBy().prop("id").asc()
                                       .limit(1)
                                       .yield().prop("cost.currency")
                                       .modelAsPrimitive())
                .as("cost.currency")
                .modelAsEntity(TeAverageFuelUsage.class);
    }

}
