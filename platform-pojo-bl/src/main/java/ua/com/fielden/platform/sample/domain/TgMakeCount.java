package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.ISimpleMoneyType;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

@KeyType(TgVehicleMake.class)
@CompanionObject(ITgMakeCount.class)
public class TgMakeCount extends AbstractEntity<TgVehicleMake> {

    private static final EntityResultQueryModel<TgMakeCount> model_ = select(TgVehicleModel.class).
            groupBy().prop("make").
            yield().prop("make").as("key").
            yield().countAll().as("count").
            yield().countAll().as("cost.amount").
            yield().countAll().as("cost").
            modelAsEntity(TgMakeCount.class);

    @IsProperty
    @Title(value = "Count", desc = "Vehicle Model Count per Make")
    private Long count;

    @IsProperty
    @Title("Value")
    @PersistentType(userType = ISimpleMoneyType.class)
    private Money cost;

    @Observable
    public TgMakeCount setCost(final Money cost) {
        this.cost = cost;
        return this;
    }

    public Money getCost() {
        return cost;
    }

    @Observable
    public TgMakeCount setCount(final Long count) {
        this.count = count;
        return this;
    }

    public Long getCount() {
        return count;
    }
}
