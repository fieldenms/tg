package ua.com.fielden.platform.eql.entities;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

@KeyType(TgtVehicle.class)
public class TgtAverageFuelUsage extends AbstractEntity<TgtVehicle> {
    private static final long serialVersionUID = 1L;

    private static String from(final String param) {
        return param + ".from";
    }

    private static String to(final String param) {
        return param + ".to";
    }

    private static final EntityResultQueryModel<TgtAverageFuelUsage> model_ = //
    select(TgtFuelUsage.class). //
    where(). //
    prop("date").gt().iParam(from("datePeriod")).and(). //
    prop("date").lt().iParam(to("datePeriod")). //
    groupBy().prop("vehicle"). //
    yield().prop("vehicle").as("key"). //
    yield().sumOf().prop("qty").as("qty"). //
    modelAsEntity(TgtAverageFuelUsage.class);

    @IsProperty
    @Title("Total qty over the period")
    private BigDecimal qty;

    @IsProperty
    @CritOnly
    @Title("Date period")
    private Date datePeriod;

    @Observable
    public TgtAverageFuelUsage setDatePeriod(final Date datePeriod) {
        this.datePeriod = datePeriod;
        return this;
    }

    public Date getDatePeriod() {
        return datePeriod;
    }

    @Observable
    public TgtAverageFuelUsage setQty(final BigDecimal qty) {
        this.qty = qty;
        return this;
    }

    public BigDecimal getQty() {
        return qty;
    }
}