package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.PersistedType;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.ISimpleMoneyType;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
@DefaultController(ITgVehicle.class)
public class TgVehicle extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty @MapTo
    private Date initDate;

    @IsProperty @MapTo
    private TgVehicle replacedBy;

    @IsProperty @MapTo
    private TgOrgUnit5 station;

    @IsProperty @MapTo @Required @Title("Model")
    private TgVehicleModel model;

    @IsProperty @MapTo @Title("Price") @PersistedType(userType = ISimpleMoneyType.class)
    private Money price;

    @IsProperty @MapTo @PersistedType(userType = ISimpleMoneyType.class)
    private Money purchasePrice;

    @IsProperty @MapTo @Title("Active")
    private boolean active;

    @IsProperty @MapTo @Title("Leased?")
    private boolean leased;

    @IsProperty(value = TgFuelUsage.class, linkProperty = "vehicle") @Title("Fuel usages")
    private Set<TgFuelUsage> fuelUsages = new HashSet<TgFuelUsage>();

    @IsProperty @MapTo(length = 10, precision = 3, scale = 10)  @Title("Last meter reading")
    private BigDecimal lastMeterReading;

    @IsProperty(linkProperty = "vehicle") @Calculated  @Title("Last fuel usage")
    private TgFuelUsage lastFuelUsage;
    private static final ExpressionModel lastFuelUsage_ = expr().model(
	      select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().notExists(
	      select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).model()).model();

    @IsProperty @Calculated @Title("Const value prop")
    private Integer constValueProp;
    private static final ExpressionModel constValueProp_ = expr().val(10).add().val(20).model();

    @IsProperty @Calculated("price.amount + purchasePrice.amount") @Title("Calc0")
    private BigDecimal calc0;

    @IsProperty @Calculated @Title("Last fuel usage qty")
    private BigDecimal lastFuelUsageQty;
    private static final ExpressionModel lastFuelUsageQty_ = expr().model(
	      select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().notExists(
	      select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).yield().prop("qty").modelAsPrimitive()).model();

    @IsProperty @Calculated @Title(value = "Sum of prices", desc = "Sum of price.amount and purchasePrice.amount") @PersistedType(userType = ISimpleMoneyType.class)
    private Money sumOfPrices;
    private static final ExpressionModel sumOfPrices_ = expr().prop("price.amount").add().prop("purchasePrice.amount").model();

    @IsProperty @Calculated @Title("Calc2")
    private BigDecimal calc2;
    private static final ExpressionModel calc2_ = expr().model(select(TgFuelUsage.class).yield().sumOf().prop("qty").modelAsPrimitive()).model();

    @IsProperty @Calculated @Title("Calc3")
    private BigDecimal calc3;
    private static final ExpressionModel calc3_ = expr().model(select(TgFuelUsage.class).where().prop("date").lt().extProp("initDate").yield().sumOf().prop("qty").modelAsPrimitive()).model();

    @IsProperty @Calculated @Title("Calc4")
    private BigDecimal calc4;
    private static final ExpressionModel calc4_ = expr().model(select(TgFuelUsage.class).where().prop("qty").lt().extProp("calc2").yield().sumOf().prop("qty").modelAsPrimitive()).model();

    @IsProperty @Calculated @Title("Calc5")
    private BigDecimal calc5;
    private static final ExpressionModel calc5_ = expr().prop("sumOfPrices").mult().val(2).model();

    @IsProperty @Calculated @Title("Calc6")
    private BigDecimal calc6;
    private static final ExpressionModel calc6_ = expr().prop("sumOfPrices").div().prop("calc3").model();

    @IsProperty @Calculated @Title("Calc Model")
    private TgVehicleModel calcModel;
    private static final ExpressionModel calcModel_ = expr().prop("model").model();

    @IsProperty
    @Title(value = "Financial details", desc = "Fin Details")
    private TgVehicleFinDetails finDetails;

//    @IsProperty @Calculated @Title("Calc Make")
//    private TgVehicleMake calcMake;
//    private static final ExpressionModel calcMake_ = expr().prop("model.make").model();

//    @IsProperty @Calculated(category = CalculatedPropertyCategory.AGGREGATED_EXPRESSION) @Title("Aggregated prop")
//    private BigDecimal aggregated;
//    private static final ExpressionModel aggregated_ = expr().sumOf().prop("purchasePrice.amount").model();
//  public BigDecimal getAggregated() {
//	return aggregated;
//  }


    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgVehicle() {
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
//	return calcMake;
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

    public Set<TgFuelUsage> getFuelUsages() { return fuelUsages; }

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

    @Observable  @EntityExists(TgVehicleModel.class)
    public TgVehicle setModel(final TgVehicleModel model) {
	this.model = model;
	return this;
    }

    public TgOrgUnit5 getStation() {
	return station;
    }

    @Observable  @EntityExists(TgOrgUnit5.class)
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

    @Observable  @EntityExists(TgVehicle.class)
    public TgVehicle setReplacedBy(final TgVehicle replacedBy) {
        this.replacedBy = replacedBy;
        return this;
    }
}