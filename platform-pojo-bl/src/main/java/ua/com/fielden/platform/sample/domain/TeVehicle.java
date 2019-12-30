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
@CompanionObject(ITeVehicle.class)
public class TeVehicle extends AbstractEntity<String> {

    @IsProperty
    @Calculated
    private String makeKey;
    protected static final ExpressionModel makeKey_ = expr().prop("model.make.key").model();

    @IsProperty
    @Calculated
    private String makeDesc;
    protected static final ExpressionModel makeDesc_ = expr().prop("model.make.desc").model();
    
    @IsProperty
    @Calculated
    private String modelKey;
    protected static final ExpressionModel modelKey_ = expr().prop("model.key").model();

    @IsProperty
    @Calculated
    private String modelDesc;
    protected static final ExpressionModel modelDesc_ = expr().prop("model.desc").model();

    @IsProperty
    @Calculated
    private String stationKey;
    protected static final ExpressionModel stationKey_ = expr().prop("station.key").model();

    @IsProperty
    @Calculated
    private String makeKey2;
    protected static final ExpressionModel makeKey2_ = expr().prop("model.makeKey").model();

    @IsProperty
    @Calculated
    private String makeKey3;
    protected static final ExpressionModel makeKey3_ = expr().model(select(TeVehicleModel.class).where().prop("id").eq().extProp("model").yield().prop("make.key").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    private String makeKey4;
    protected static final ExpressionModel makeKey4_ = expr().model(select(TeVehicleModel.class).where().prop("id").eq().extProp("model").yield().prop("makeKey").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    private String makeKey5;
    protected static final ExpressionModel makeKey5_ = expr().model(select(TeVehicleModel.class).where().prop("id").eq().extProp("model").yield().prop("makeKey2").modelAsPrimitive()).model();

    @IsProperty
    @MapTo
    private Date initDate;

    @IsProperty
    @MapTo
    private TeVehicle replacedBy;

    @IsProperty
    @MapTo
    private TgOrgUnit5 station;

    @IsProperty
    @MapTo
    @Required
    @Title("Model")
    private TeVehicleModel model;

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
    private TeVehicleModel calcModel;
    protected static final ExpressionModel calcModel_ = expr().prop("model").model();

    @IsProperty
    @CritOnly(Type.MULTI)
    private TgFuelType fuelTypeCrit;

    @IsProperty
    @Title(value = "Financial details", desc = "Fin Details")
    private TgVehicleFinDetails finDetails;
    
    @IsProperty
    @CritOnly
    @Title("Date period")
    private Date datePeriod;

    @Observable
    public TeVehicle setDatePeriod(final Date datePeriod) {
        this.datePeriod = datePeriod;
        return this;
    }

    public Date getDatePeriod() {
        return datePeriod;
    }

    @Observable
    public TeVehicle setFuelTypeCrit(final TgFuelType fuelTypeCrit) {
        this.fuelTypeCrit = fuelTypeCrit;
        return this;
    }

    public TgFuelType getFuelTypeCrit() {
        return fuelTypeCrit;
    }

    @Observable
    public TeVehicle setCalcModel(final TeVehicleModel calcModel) {
        this.calcModel = calcModel;
        return this;
    }
    
    @Observable
    public TeVehicle setFinDetails(final TgVehicleFinDetails finDetails) {
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
    protected TeVehicle setLastFuelUsage(final TgFuelUsage fu) {
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

    public TeVehicleModel getCalcModel() {
        return calcModel;
    }

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
    public TeVehicle setLastMeterReading(final BigDecimal lastMeterReading) {
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
    public TeVehicle setLeased(final boolean leased) {
        this.leased = leased;
        return this;
    }

    public boolean getLeased() {
        return leased;
    }

    @Observable
    public TeVehicle setActive(final boolean active) {
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
    public TeVehicle setPrice(final Money price) {
        this.price = price;
        return this;
    }

    public Money getPurchasePrice() {
        return purchasePrice;
    }

    @Observable
    public TeVehicle setPurchasePrice(final Money purchasePrice) {
        this.purchasePrice = purchasePrice;
        return this;
    }

    public TeVehicleModel getModel() {
        return model;
    }

    @Observable
    public TeVehicle setModel(final TeVehicleModel model) {
        this.model = model;
        return this;
    }

    public TgOrgUnit5 getStation() {
        return station;
    }

    @Observable
    public TeVehicle setStation(final TgOrgUnit5 station) {
        this.station = station;
        return this;
    }

    public Date getInitDate() {
        return initDate;
    }

    @Observable
    public TeVehicle setInitDate(final Date initDate) {
        this.initDate = initDate;
        return this;
    }

    public TeVehicle getReplacedBy() {
        return replacedBy;
    }

    @Observable
    public TeVehicle setReplacedBy(final TeVehicle replacedBy) {
        this.replacedBy = replacedBy;
        return this;
    }

    @Observable
    public TeVehicle setConstValueProp(final Integer constValueProp) {
        this.constValueProp = constValueProp;
        return this;
    }

    @Observable
    public TeVehicle setCalc0(final BigDecimal calc0) {
        this.calc0 = calc0;
        return this;
    }

    @Observable
    public TeVehicle setLastFuelUsageQty(final BigDecimal lastFuelUsageQty) {
        this.lastFuelUsageQty = lastFuelUsageQty;
        return this;
    }

    @Observable
    public TeVehicle setSumOfPrices(final Money sumOfPrices) {
        this.sumOfPrices = sumOfPrices;
        return this;
    }

    @Observable
    public TeVehicle setCalc2(final BigDecimal calc2) {
        this.calc2 = calc2;
        return this;
    }

    @Observable
    public TeVehicle setCalc3(final BigDecimal calc3) {
        this.calc3 = calc3;
        return this;
    }

    @Observable
    public TeVehicle setCalc4(final BigDecimal calc4) {
        this.calc4 = calc4;
        return this;
    }

    @Observable
    public TeVehicle setCalc5(final BigDecimal calc5) {
        this.calc5 = calc5;
        return this;
    }

    @Observable
    public TeVehicle setCalc6(final BigDecimal calc6) {
        this.calc6 = calc6;
        return this;
    }
    
    @Observable
    protected TeVehicle setMakeKey(final String makeKey) {
        this.makeKey = makeKey;
        return this;
    }

    public String getMakeKey() {
        return makeKey;
    }

    @Observable
    protected TeVehicle setModelKey(final String modelKey) {
        this.modelKey = modelKey;
        return this;
    }

    public String getModelKey() {
        return modelKey;
    }

    @Observable
    protected TeVehicle setModelDesc(final String modelDesc) {
        this.modelDesc = modelDesc;
        return this;
    }

    public String getModelDesc() {
        return modelDesc;
    }

    @Observable
    protected TeVehicle setStationKey(final String stationKey) {
        this.stationKey = stationKey;
        return this;
    }

    public String getStationKey() {
        return stationKey;
    }

    @Observable
    protected TeVehicle setMakeDesc(final String makeDesc) {
        this.makeDesc = makeDesc;
        return this;
    }

    public String getMakeDesc() {
        return makeDesc;
    }
    
    @Observable
    protected TeVehicle setMakeKey2(final String makeKey2) {
        this.makeKey2 = makeKey2;
        return this;
    }

    public String getMakeKey2() {
        return makeKey2;
    }

    @Observable
    protected TeVehicle setMakeKey3(final String makeKey3) {
        this.makeKey3 = makeKey3;
        return this;
    }

    public String getMakeKey3() {
        return makeKey3;
    }
    
    @Observable
    protected TeVehicle setMakeKey4(final String makeKey4) {
        this.makeKey4 = makeKey4;
        return this;
    }

    public String getMakeKey4() {
        return makeKey4;
    }

    @Observable
    protected TeVehicle setMakeKey5(final String makeKey5) {
        this.makeKey5 = makeKey5;
        return this;
    }

    public String getMakeKey5() {
        return makeKey5;
    }

}