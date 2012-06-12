package ua.com.fielden.platform.sample.domain;

import java.math.BigInteger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

@KeyType(TgVehicleMake.class)
@DescTitle("Description")
public class TgMakeCount extends AbstractEntity<TgVehicleMake> {
    private static final long serialVersionUID = 1L;
    private static final EntityResultQueryModel<TgMakeCount> model_ = select(TgVehicleModel.class).groupBy().prop("make").yield().prop("make").as("key").yield().countAll().as("count").modelAsEntity(TgMakeCount.class);

    @IsProperty
    @Title(value = "Count", desc = "Vehicle Model Count per Make")
    private BigInteger count;

    @Observable
    public TgMakeCount setCount(final BigInteger count) {
	this.count = count;
	return this;
    }

    public BigInteger getCount() {
	return count;
    }

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgMakeCount() {
    }
}