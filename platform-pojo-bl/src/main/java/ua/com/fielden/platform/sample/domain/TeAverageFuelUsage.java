package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.types.Money;

@KeyType(TeVehicle.class)
@CompanionObject(ITeAverageFuelUsage.class)
public class TeAverageFuelUsage extends AbstractEntity<TeVehicle> {

    private static final EntityResultQueryModel<TeAverageFuelUsage> model_ = //
            select(TeVehicleFuelUsage.class). //
                    where(). //
                    prop("date").gt().iParam("datePeriod.from").and(). //
                    prop("date").lt().iParam("datePeriod.to"). //
                    groupBy().prop("vehicle"). //
                    yield().prop("vehicle").as("id"). //
                    yield().prop("vehicle").as("key"). //
                    yield().sumOf().prop("qty").as("qty"). //
                    yield().sumOf().prop("cost.amount").as("cost.amount"). //
                    modelAsEntity(TeAverageFuelUsage.class);

    @IsProperty
    @Title("Total qty over the period")
    private BigDecimal qty;
    
    @IsProperty
    @MapTo
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
}