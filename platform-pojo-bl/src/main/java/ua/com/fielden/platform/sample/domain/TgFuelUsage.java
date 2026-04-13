package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.DynamicEntityKey;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.IMoneyType;

import java.math.BigDecimal;
import java.util.Date;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

@KeyTitle("Fuel Usages")
@KeyType(DynamicEntityKey.class)
@MapEntityTo
@CompanionObject(ITgFuelUsage.class)
public class TgFuelUsage extends AbstractEntity<DynamicEntityKey> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    @Title("Vehicle")
    @CompositeKeyMember(1)
    private TgVehicle vehicle;

    @IsProperty
    @MapTo
    @Title("Purchase Date")
    @CompositeKeyMember(2)
    private Date date;

    @IsProperty
    @Required
    @MapTo
    @Title(value = "Fuel Qty", desc = "Fuel Qty")
    private BigDecimal qty;

    @IsProperty(precision = 18, scale = 4)
    @MapTo
    @PersistentType(userType = IMoneyType.class)
    private Money pricePerLitre;

    @IsProperty
    @Readonly
    @Calculated
    @PersistentType(userType = IMoneyType.class)
    @Title(value = "Previous Price per Litre", desc = "Price per litre for the same vehicle on the previous date.")
    private Money previousPricePerLitre;
    // Example: Money.currency cannot be inferred due to the sub-query, so define it manually.
    protected static final ExpressionModel
            previousPricePerLitre_ = expr()
                    .model(select(TgFuelUsage.class).where()
                                   .prop("vehicle").eq().extProp("vehicle")
                                   .and()
                                   .prop("date").lt().extProp("date")
                                   .orderBy().prop("date").desc()
                                   .limit(1)
                                   .yield().prop("pricePerLitre")
                                   .modelAsPrimitive())
                    .model(),
            previousPricePerLitre_currency_ = expr()
                    .model(select(TgFuelUsage.class).where()
                                   .prop("vehicle").eq().extProp("vehicle")
                                   .and()
                                   .prop("date").lt().extProp("date")
                                   .orderBy().prop("date").desc()
                                   .limit(1)
                                   .yield().prop("pricePerLitre.currency")
                                   .modelAsPrimitive())
                    .model();

    @IsProperty
    @Readonly
    @Calculated
    @PersistentType(userType = IMoneyType.class)
    @Title(value = "Half Price per Litre")
    private Money halfPricePerLitre;
    // halfPricePerLitre.currency should be inferred.
    protected static final ExpressionModel halfPricePerLitre_ = expr().prop("pricePerLitre").div().val(2).model();

    @IsProperty
    @Required
    @MapTo
    @Title(value = "Fuel type", desc = "Fuel type")
    private TgFuelType fuelType;

    @Observable
    protected TgFuelUsage setPreviousPricePerLitre(final Money previousPricePerLitre) {
        this.previousPricePerLitre = previousPricePerLitre;
        return this;
    }

    public Money getPreviousPricePerLitre() {
        return previousPricePerLitre;
    }

    @Observable
    public TgFuelUsage setFuelType(final TgFuelType fuelType) {
        this.fuelType = fuelType;
        return this;
    }

    public TgFuelType getFuelType() {
        return fuelType;
    }

    public BigDecimal getQty() {
        return qty;
    }

    public TgVehicle getVehicle() {
        return vehicle;
    }

    public Date getDate() {
        return date;
    }

    @Observable
    public void setVehicle(final TgVehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Observable
    public void setDate(final Date date) {
        this.date = date;
    }

    @Observable
    public TgFuelUsage setQty(final BigDecimal qty) {
        this.qty = qty;
        return this;
    }
    
    @Observable
    public TgFuelUsage setPricePerLitre(final Money pricePerLitre) {
        this.pricePerLitre = pricePerLitre;
        return this;
    }

    public Money getPricePerLitre() {
        return pricePerLitre;
    }

    @Observable
    protected TgFuelUsage setHalfPricePerLitre(final Money halfPricePerLitre) {
        this.halfPricePerLitre = halfPricePerLitre;
        return this;
    }

    public Money getHalfPricePerLitre() {
        return halfPricePerLitre;
    }

}
