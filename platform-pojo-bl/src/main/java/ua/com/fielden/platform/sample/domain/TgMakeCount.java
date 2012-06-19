package ua.com.fielden.platform.sample.domain;

import java.math.BigInteger;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;
import ua.com.fielden.platform.sample.domain.controller.ITgMakeCount;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
@KeyType(TgVehicleMake.class)
@DefaultController(ITgMakeCount.class)
public class TgMakeCount extends AbstractEntity<TgVehicleMake> {
    // TODO support make property being entity key (KeyType(TgVehicleMake))
    private static final long serialVersionUID = 1L;
    private static final EntityResultQueryModel<TgMakeCount> model_ = select(TgVehicleModel.class).groupBy().prop("make").yield().prop("make").as("make").yield().countAll().as("count").modelAsEntity(TgMakeCount.class);


    @IsProperty
    @MapTo
    @Title(value = "Make", desc = "Make")
    private TgVehicleMake make;

    @Observable
    public TgMakeCount setMake(final TgVehicleMake make) {
	this.make = make;
	return this;
    }

    public TgVehicleMake getMake() {
	return make;
    }

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