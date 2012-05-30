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
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicle;
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

    private static final ExpressionModel calcModel_ = expr().prop("model").model();
    private static final ExpressionModel constValueProp_ = expr().val(10).add().val(20).model();
    private static final ExpressionModel sumOfPrices_ = expr().prop("price.amount").add().prop("purchasePrice.amount").model();
    private static final ExpressionModel calc2_ = expr().model(select(TgFuelUsage.class).yield().sumOf().prop("qty").modelAsPrimitive()).model();
    private static final ExpressionModel calc3_ = expr().model(select(TgFuelUsage.class).where().prop("date").lt().extProp("initDate").yield().sumOf().prop("qty").modelAsPrimitive()).model();
    private static final ExpressionModel calc4_ = expr().model(select(TgFuelUsage.class).where().prop("qty").lt().extProp("calc2").yield().sumOf().prop("qty").modelAsPrimitive()).model();
    private static final ExpressionModel calc5_ = expr().prop("sumOfPrices").mult().val(2).model();
    private static final ExpressionModel calc6_ = expr().prop("sumOfPrices").div().prop("calc3").model();

    private static final ExpressionModel lastFuelUsageQty_ = expr().model(
      select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().notExists(
      select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).yield().prop("qty").modelAsPrimitive()).model();

    private static final ExpressionModel lastFuelUsage_ = expr().model(
	      select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().notExists(
	      select(TgFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).model()).model();

    @IsProperty @MapTo
    private Date initDate;

    @IsProperty @MapTo
    private TgVehicle replacedBy;

    @IsProperty @MapTo
    private TgOrgUnit5 station;

    @IsProperty @MapTo @Required @Title("Model")
    private TgVehicleModel model;

    @IsProperty @MapTo @Title("Price")
    private Money price;

    @IsProperty @MapTo(userType = ISimpleMoneyType.class)
    private Money purchasePrice;

    @IsProperty @MapTo @Title("Active")
    private boolean active;

    @IsProperty @MapTo @Title("Leased?")
    private boolean leased;

    @IsProperty(value = TgFuelUsage.class, linkProperty = "vehicle") @Title("Fuel usages")
    private Set<TgFuelUsage> fuelUsages = new HashSet<TgFuelUsage>();

    @IsProperty @MapTo(length = 10, precision = 3, scale = 10)  @Title("Last meter reading")
    private BigDecimal lastMeterReading;

    @IsProperty @Calculated  @Title("Last fuel usage")
    private TgFuelUsage lastFuelUsage;

    @IsProperty @Calculated @Title("Const value prop")
    private Integer constValueProp;

    @IsProperty @Calculated("price.amount + purchasePrice.amount") @Title("Calc0")
    private BigDecimal calc0;

    @IsProperty @Calculated @Title("Last fuel usage qty")
    private BigDecimal lastFuelUsageQty;

    @IsProperty @Calculated @Title(value = "Sum of prices", desc = "Sum of price.amount and purchasePrice.amount")
    private BigDecimal sumOfPrices;

    @IsProperty @Calculated @Title("Calc2")
    private BigDecimal calc2;

    @IsProperty @Calculated @Title("Calc3")
    private BigDecimal calc3;

    @IsProperty @Calculated @Title("Calc4")
    private BigDecimal calc4;

    @IsProperty @Calculated @Title("Calc5")
    private BigDecimal calc5;

    @IsProperty @Calculated @Title("Calc6")
    private BigDecimal calc6;

    @IsProperty @Calculated @Title("Calc Model")
    private TgVehicleModel calcModel;

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgVehicle() {
    }

    @Observable
    public TgVehicle setLastFuelUsage(final TgFuelUsage lastFuelUsage) {
	this.lastFuelUsage = lastFuelUsage;
	return this;
    }

    public TgFuelUsage getLastFuelUsage() {
	return lastFuelUsage;
    }

    @Observable
    public TgVehicle setConstValueProp(final Integer constValueProp) {
	this.constValueProp = constValueProp;
	return this;
    }

    public Integer getConstValueProp() {
	return constValueProp;
    }

    @Observable
    public TgVehicle setCalc0(final BigDecimal calc0) {
	this.calc0 = calc0;
	return this;
    }

    public BigDecimal getCalc0() {
	return calc0;
    }

    @Observable
    public TgVehicle setLastFuelUsageQty(final BigDecimal lastFuelUsageQty) {
	this.lastFuelUsageQty = lastFuelUsageQty;
	return this;
    }

    public BigDecimal getLastFuelUsageQty() {
	return lastFuelUsageQty;
    }

    @Observable
    public TgVehicle setCalcModel(final TgVehicleModel calcModel) {
	this.calcModel = calcModel;
	return this;
    }

    public TgVehicleModel getCalcModel() {
	return calcModel;
    }

    @Observable
    public TgVehicle setSumOfPrices(final BigDecimal sumOfPrices) {
	this.sumOfPrices = sumOfPrices;
	return this;
    }

    public BigDecimal getSumOfPrices() {
	return sumOfPrices;
    }

    @Observable
    public TgVehicle setCalc3(final BigDecimal calc3) {
	this.calc3 = calc3;
	return this;
    }

    public BigDecimal getCalc3() {
	return calc3;
    }

    @Observable
    public TgVehicle setCalc4(final BigDecimal calc4) {
	this.calc4 = calc4;
	return this;
    }

    public BigDecimal getCalc4() {
	return calc4;
    }

    @Observable
    public TgVehicle setCalc2(final BigDecimal calc2) {
	this.calc2 = calc2;
	return this;
    }

    public BigDecimal getCalc2() {
	return calc2;
    }

    @Observable
    public TgVehicle setCalc5(final BigDecimal calc5) {
	this.calc5 = calc5;
	return this;
    }

    public BigDecimal getCalc5() {
	return calc5;
    }

    @Observable
    public TgVehicle setCalc6(final BigDecimal calc6) {
	this.calc6 = calc6;
	return this;
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