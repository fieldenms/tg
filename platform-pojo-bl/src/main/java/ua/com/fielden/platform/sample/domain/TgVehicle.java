package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.ISimpleMoneyType;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
@CompanionObject(ITgVehicle.class)
public class TgVehicle extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    private Date initDate;

    @IsProperty
    @MapTo
    private TgVehicle replacedBy;

    @IsProperty
    @MapTo
    private TgOrgUnit5 station;

    @IsProperty
    @MapTo
    @Required
    @Title("Model")
    private TgVehicleModel model;

    @IsProperty
    @MapTo
    @Title("Price")
    @PersistentType(userType = ISimpleMoneyType.class)
    private Money price;

    @IsProperty
    @MapTo
    @PersistentType(userType = ISimpleMoneyType.class)
    private Money purchasePrice;

    @IsProperty
    @MapTo
    @Title("Active")
    private boolean active;

    @IsProperty
    @MapTo
    @Title("Leased?")
    private boolean leased;

    @IsProperty(value = TgFuelUsage.class, linkProperty = "vehicle")
    @Title("Fuel usages")
    private Set<TgFuelUsage> fuelUsages = new HashSet<TgFuelUsage>();

    @IsProperty(value = TgVehicleFuelUsage.class, linkProperty = "vehicle")
    @Title("Vehicle Fuel usages")
    private Set<TgVehicleFuelUsage> vehicleFuelUsages = new HashSet<TgVehicleFuelUsage>();

    @IsProperty(precision = 10, scale = 3)
    @MapTo
    @Title("Last meter reading")
    private BigDecimal lastMeterReading;

    @IsProperty(linkProperty = "vehicle")
    @Calculated
    @Title("Last fuel usage")
    private TgFuelUsage lastFuelUsage;
    protected static final ExpressionModel lastFuelUsage_ = expr().model(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().notExists(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).model()).model();

    @IsProperty
    @Calculated
    @Title("Const value prop")
    private Integer constValueProp;
    protected static final ExpressionModel constValueProp_ = expr().val(10).add().val(20).model();

    @IsProperty
    @Calculated("price.amount + purchasePrice.amount")
    @Title("Calc0")
    private BigDecimal calc0;

    @IsProperty
    @Calculated
    @Title("Last fuel usage qty")
    private BigDecimal lastFuelUsageQty;
    protected static final ExpressionModel lastFuelUsageQty_ = expr().model(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().notExists(select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).yield().prop("qty").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    @Title(value = "Sum of prices", desc = "Sum of price.amount and purchasePrice.amount")
    @PersistentType(userType = ISimpleMoneyType.class)
    private Money sumOfPrices;
    protected static final ExpressionModel sumOfPrices_ = expr().val(1).mult().prop("price.amount").add().prop("purchasePrice.amount").model();

    @IsProperty
    @Calculated
    @Title("Calc2")
    private BigDecimal calc2;
    protected static final ExpressionModel calc2_ = expr().model(select(TgFuelUsage.class).yield().sumOf().prop("qty").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    @Title("Calc3")
    private BigDecimal calc3;
    protected static final ExpressionModel calc3_ = expr().model(select(TgFuelUsage.class).where().prop("date").lt().extProp("initDate").yield().sumOf().prop("qty").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    @Title("Calc4")
    private BigDecimal calc4;
    protected static final ExpressionModel calc4_ = expr().model(select(TgFuelUsage.class).where().prop("qty").lt().extProp("calc2").yield().sumOf().prop("qty").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    @Title("Calc5")
    private BigDecimal calc5;
    protected static final ExpressionModel calc5_ = expr().prop("sumOfPrices").mult().val(2).model();

    @IsProperty
    @Calculated
    @Title("Calc6")
    private BigDecimal calc6;
    protected static final ExpressionModel calc6_ = expr().prop("sumOfPrices").div().prop("calc3").model();

    @IsProperty
    @Calculated
    @Title("Calc Model")
    private TgVehicleModel calcModel;
    protected static final ExpressionModel calcModel_ = expr().prop("model").model();

    @IsProperty
    @CritOnly(Type.MULTI)
    private TgFuelType fuelTypeCrit;

    @IsProperty
    @Title(value = "Financial details", desc = "Fin Details")
    private TgVehicleFinDetails finDetails;

    //   FIXME
    //    @IsProperty @Calculated @Title("Calc Make")
    //    private TgVehicleMake calcMake;
    //    private static final ExpressionModel calcMake_ = expr().prop("model.make").model();

    //    @IsProperty @Calculated(category = CalculatedPropertyCategory.AGGREGATED_EXPRESSION) @Title("Aggregated prop")
    //    private BigDecimal aggregated;
    //    private static final ExpressionModel aggregated_ = expr().sumOf().prop("purchasePrice.amount").model();
    //  public BigDecimal getAggregated() {
    //  return aggregated;
    //  }

    
    @IsProperty
    @CritOnly
    @Title("Date period")
    private Date datePeriod;

    @Observable
    public TgVehicle setDatePeriod(final Date datePeriod) {
        this.datePeriod = datePeriod;
        return this;
    }

    public Date getDatePeriod() {
        return datePeriod;
    }

    @Observable
    public TgVehicle setFuelTypeCrit(final TgFuelType fuelTypeCrit) {
        this.fuelTypeCrit = fuelTypeCrit;
        return this;
    }

    public TgFuelType getFuelTypeCrit() {
        return fuelTypeCrit;
    }

    @Observable
    public TgVehicle setCalcModel(final TgVehicleModel calcModel) {
        this.calcModel = calcModel;
        return this;
    }
    
    @Observable
    public TgVehicle setFinDetails(final TgVehicleFinDetails finDetails) {
        this.finDetails = finDetails;
        return this;
    }

    public TgVehicleFinDetails getFinDetails() {
        return finDetails;
    }

    public TgFuelUsage getLastFuelUsage() {
        return lastFuelUsage;
    }

    @Observable
    protected TgVehicle setLastFuelUsage(final TgFuelUsage fu) {
        this.lastFuelUsage = fu;
        return this;
    }

    public Integer getConstValueProp() {
        return constValueProp;
    }

    public BigDecimal getCalc0() {
        return calc0;
    }

    public BigDecimal getLastFuelUsageQty() {
        return lastFuelUsageQty;
    }

    public TgVehicleModel getCalcModel() {
        return calcModel;
    }

    //    public TgVehicleMake getCalcMake() {
    //  return calcMake;
    //    }

    public Money getSumOfPrices() {
        return sumOfPrices;
    }

    public BigDecimal getCalc3() {
        return calc3;
    }

    public BigDecimal getCalc4() {
        return calc4;
    }

    public BigDecimal getCalc2() {
        return calc2;
    }

    public BigDecimal getCalc5() {
        return calc5;
    }

    public BigDecimal getCalc6() {
        return calc6;
    }
    
    @Observable
    public TgVehicle setLastMeterReading(final BigDecimal lastMeterReading) {
        this.lastMeterReading = lastMeterReading;
        return this;
    }

    public BigDecimal getLastMeterReading() {
        return lastMeterReading;
    }

    @Observable
    public void setFuelUsages(final Set<TgFuelUsage> fuelUsages) {
        this.fuelUsages = fuelUsages;
    }

    public Set<TgFuelUsage> getFuelUsages() {
        return fuelUsages;
    }

    @Observable
    public void setVehicleFuelUsages(final Set<TgVehicleFuelUsage> vehicleFuelUsages) {
        this.vehicleFuelUsages = vehicleFuelUsages;
    }

    public Set<TgVehicleFuelUsage> getVehicleFuelUsages() {
        return vehicleFuelUsages;
    }

    @Observable
    public TgVehicle setLeased(final boolean leased) {
        this.leased = leased;
        return this;
    }

    public boolean getLeased() {
        return leased;
    }

    @Observable
    public TgVehicle setActive(final boolean active) {
        this.active = active;
        return this;
    }

    public boolean getActive() {
        return active;
    }

    public Money getPrice() {
        return price;
    }

    @Observable
    public TgVehicle setPrice(final Money price) {
        this.price = price;
        return this;
    }

    public Money getPurchasePrice() {
        return purchasePrice;
    }

    @Observable
    public TgVehicle setPurchasePrice(final Money purchasePrice) {
        this.purchasePrice = purchasePrice;
        return this;
    }

    public TgVehicleModel getModel() {
        return model;
    }

    @Observable
    public TgVehicle setModel(final TgVehicleModel model) {
        this.model = model;
        return this;
    }

    public TgOrgUnit5 getStation() {
        return station;
    }

    @Observable
    public TgVehicle setStation(final TgOrgUnit5 station) {
        this.station = station;
        return this;
    }

    public Date getInitDate() {
        return initDate;
    }

    @Observable
    public TgVehicle setInitDate(final Date initDate) {
        this.initDate = initDate;
        return this;
    }

    public TgVehicle getReplacedBy() {
        return replacedBy;
    }

    @Observable
    public TgVehicle setReplacedBy(final TgVehicle replacedBy) {
        this.replacedBy = replacedBy;
        return this;
    }

    @Observable
    public TgVehicle setConstValueProp(final Integer constValueProp) {
        this.constValueProp = constValueProp;
        return this;
    }

    @Observable
    public TgVehicle setCalc0(final BigDecimal calc0) {
        this.calc0 = calc0;
        return this;
    }

    @Observable
    public TgVehicle setLastFuelUsageQty(final BigDecimal lastFuelUsageQty) {
        this.lastFuelUsageQty = lastFuelUsageQty;
        return this;
    }

    @Observable
    public TgVehicle setSumOfPrices(final Money sumOfPrices) {
        this.sumOfPrices = sumOfPrices;
        return this;
    }

    @Observable
    public TgVehicle setCalc2(final BigDecimal calc2) {
        this.calc2 = calc2;
        return this;
    }

    @Observable
    public TgVehicle setCalc3(final BigDecimal calc3) {
        this.calc3 = calc3;
        return this;
    }

    @Observable
    public TgVehicle setCalc4(final BigDecimal calc4) {
        this.calc4 = calc4;
        return this;
    }

    @Observable
    public TgVehicle setCalc5(final BigDecimal calc5) {
        this.calc5 = calc5;
        return this;
    }

    @Observable
    public TgVehicle setCalc6(final BigDecimal calc6) {
        this.calc6 = calc6;
        return this;
    }
}