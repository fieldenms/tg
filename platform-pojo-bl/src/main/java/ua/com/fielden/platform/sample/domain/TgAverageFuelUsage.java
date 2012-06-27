package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;
import ua.com.fielden.platform.sample.domain.controller.ITgAverageFuelUsage;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
@KeyType(TgVehicle.class)
@DefaultController(ITgAverageFuelUsage.class)
public class TgAverageFuelUsage extends AbstractEntity<TgVehicle> {
    // TODO support make property being entity key (KeyType(TgVehicleMake))
    private static final long serialVersionUID = 1L;

    private static String from(final String param) {
	return param + ".from";
    }

    private static String to(final String param) {
	return param + ".to";
    }

    private static final EntityResultQueryModel<TgAverageFuelUsage> model_ = //
	    select(TgFuelUsage.class). //
	    where(). //
	    prop("date").gt().iParam(from("datePeriod")).and(). //
	    prop("date").lt().iParam(to("datePeriod")). //
	    groupBy().prop("vehicle"). //
	    yield().prop("vehicle").as("key"). //
	    yield().sumOf().prop("qty").as("qty"). //
	    modelAsEntity(TgAverageFuelUsage.class);

    @IsProperty
    @Title("Total qty over the period")
    private BigDecimal qty;

    @IsProperty @CritOnly @Title("Date period")
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

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgAverageFuelUsage() {
    }
}