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
import ua.com.fielden.platform.entity.annotation.Readonly;
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
    private String modelKey;
    protected static final ExpressionModel modelKey_ = expr().prop("model.key").model();

    @IsProperty
    @Calculated
    private String modelKey2;
    protected static final ExpressionModel modelKey2_ = expr().model(select(TeVehicleModel.class).where().prop("id").eq().extProp("model").yield().prop("key").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    private String modelDesc;
    protected static final ExpressionModel modelDesc_ = expr().prop("model.desc").model();

    @IsProperty
    @Calculated
    private String stationName;
    protected static final ExpressionModel stationName_ = expr().prop("station.name").model();

    @IsProperty
    @Calculated
    private String modelMakeDesc;
    protected static final ExpressionModel modelMakeDesc_ = expr().prop("model.make.desc").model();

    @IsProperty
    @Calculated
    private TeVehicleMake modelMake;
    protected static final ExpressionModel modelMake_ = expr().prop("model.make").model();

    @IsProperty
    @Calculated
    private TeVehicleMake mmake;
    protected static final ExpressionModel mmake_ = expr().prop("model.make").model();

    @IsProperty
    @Calculated
    private TeVehicleMake modelMake2;
    protected static final ExpressionModel modelMake2_ = expr().model(select(TeVehicleModel.class).where().prop("id").eq().extProp("model").yield().prop("make").modelAsEntity(TeVehicleMake.class)).model();

    @IsProperty
    @Calculated
    private TeVehicleMake mmake2;
    protected static final ExpressionModel mmake2_ = expr().model(select(TeVehicleModel.class).where().prop("id").eq().extProp("model").yield().prop("make").modelAsEntity(TeVehicleMake.class)).model();

    @IsProperty
    @Calculated
    private String modelMakeKey;
    protected static final ExpressionModel modelMakeKey_ = expr().prop("model.make.key").model();

    @IsProperty
    @Calculated
    private String modelMakeKeyDuplicate;
    protected static final ExpressionModel modelMakeKeyDuplicate_ = expr().prop("model.make.key").model();

    @IsProperty
    @Calculated
    private String modelMakeKey2;
    protected static final ExpressionModel modelMakeKey2_ = expr().prop("model.makeKey").model();

    @IsProperty
    @Calculated
    private String modelMakeKey3;
    protected static final ExpressionModel modelMakeKey3_ = expr().prop("model.makeKey2").model();

    @IsProperty
    @Calculated
    private String modelMakeKey4;
    protected static final ExpressionModel modelMakeKey4_ = expr().model(select(TeVehicleModel.class).where().prop("id").eq().extProp("model").yield().prop("make.key").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    private String modelMakeKey5;
    protected static final ExpressionModel modelMakeKey5_ = expr().model(select(TeVehicleModel.class).where().prop("id").eq().extProp("model").yield().prop("makeKey").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    private String modelMakeKey6;
    protected static final ExpressionModel modelMakeKey6_ = expr().model(select(TeVehicleModel.class).where().prop("id").eq().extProp("model").yield().prop("makeKey2").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    private String modelMakeKey7;
    protected static final ExpressionModel modelMakeKey7_ = expr().prop("modelMake.key").model();

    @IsProperty
    @Calculated
    private String modelMakeKey8;
    protected static final ExpressionModel modelMakeKey8_ = expr().prop("modelMake2.key").model();

    @IsProperty
    @Calculated
    private TeVehicle replacedByTwice;
    protected static final ExpressionModel replacedByTwice_ = expr().prop("replacedBy.replacedBy").model();

    @IsProperty
	@Calculated
	private TeVehicleModel replacedByTwiceModel;
	protected static final ExpressionModel replacedByTwiceModel_ = expr().prop("replacedByTwice.model").model();

	@IsProperty
    @Calculated
    private TeVehicleMake replacedByTwiceModelMake;
    protected static final ExpressionModel replacedByTwiceModelMake_ = expr().prop("replacedByTwiceModel.make").model();

    @IsProperty
    @Calculated
    private Money replacedByTwicePrice;
    protected static final ExpressionModel replacedByTwicePrice_ = expr().prop("replacedByTwice.price").model();

    @IsProperty
    @Calculated
    private Money priceDiffBetweenCurrentAndReplacedByTwice;
    protected static final ExpressionModel priceDiffBetweenCurrentAndReplacedByTwice_ = expr().prop("price").sub().prop("replacedByTwicePrice").model();

    @IsProperty
    @Calculated
    private TeVehicle theSameVehicle; //contains transitive dependency on another ET-calc-prop's subprops (i.e. "replacedByTwice.price")
    protected static final ExpressionModel theSameVehicle_ = expr().model(select(TeVehicle.class).where().prop("id").eq().extProp("id").
            and().prop("priceDiffBetweenCurrentAndReplacedByTwice").eq().extProp("priceDiffBetweenCurrentAndReplacedByTwice").model()).model();

    @IsProperty
    @MapTo
    private TeVehicleMake make;

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

    @IsProperty(value = TeVehicleFuelUsage.class, linkProperty = "vehicle")
    @Title("Fuel usages")
    private Set<TeVehicleFuelUsage> fuelUsages = new HashSet<TeVehicleFuelUsage>();

    @IsProperty(precision = 10, scale = 3)
    @MapTo
    @Title("Last meter reading")
    private BigDecimal lastMeterReading;

    @IsProperty(linkProperty = "vehicle")
    @Calculated
    @Title("Last fuel usage")
    private TeVehicleFuelUsage lastFuelUsage;
    protected static final ExpressionModel lastFuelUsage_ = expr().model(select(TeVehicleFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().notExists(select(TeVehicleFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).model()).model();

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
    protected static final ExpressionModel lastFuelUsageQty_ = expr().model(select(TeVehicleFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().notExists(select(TeVehicleFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).yield().prop("qty").modelAsPrimitive()).model();

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
    protected static final ExpressionModel calc2_ = expr().model(select(TeVehicleFuelUsage.class).yield().sumOf().prop("qty").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    @Title("Calc3")
    private BigDecimal calc3;
    protected static final ExpressionModel calc3_ = expr().model(select(TeVehicleFuelUsage.class).where().prop("date").lt().extProp("initDate").yield().sumOf().prop("qty").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    @Title("Calc4")
    private BigDecimal calc4;
    protected static final ExpressionModel calc4_ = expr().model(select(TeVehicleFuelUsage.class).where().prop("qty").lt().extProp("calc2").yield().sumOf().prop("qty").modelAsPrimitive()).model();

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
    private TeVehicleFinDetails finDetails;

    @IsProperty
    @CritOnly
    @Title("Date period")
    private Date datePeriod;

    @IsProperty
    @Calculated
    private Money repPrice;
    protected static final ExpressionModel repPrice_ = expr().prop("replacedBy.price").model();

    @IsProperty
    @Calculated
    private Money repPurchasePrice;
    protected static final ExpressionModel repPurchasePrice_ = expr().prop("replacedBy.purchasePrice").model();

    @IsProperty
    @Readonly
    @Calculated
    @Title(value = "Title", desc = "Desc")
    private Money avgRepPrice;
    protected static final ExpressionModel avgRepPrice_ = expr().expr(expr().prop("repPrice").add().prop("repPurchasePrice").model()).div().val(2).model();


    @Observable
    protected TeVehicle setTheSameVehicle(final TeVehicle theSameVehicle) {
        this.theSameVehicle = theSameVehicle;
        return this;
    }

    public TeVehicle getTheSameVehicle() {
        return theSameVehicle;
    }

    @Observable
    protected TeVehicle setPriceDiffBetweenCurrentAndReplacedByTwice(final Money priceDiffBetweenCurrentAndReplacedByTwice) {
        this.priceDiffBetweenCurrentAndReplacedByTwice = priceDiffBetweenCurrentAndReplacedByTwice;
        return this;
    }

    public Money getPriceDiffBetweenCurrentAndReplacedByTwice() {
        return priceDiffBetweenCurrentAndReplacedByTwice;
    }

    @Observable
    protected TeVehicle setReplacedByTwicePrice(final Money replacedByTwicePrice) {
        this.replacedByTwicePrice = replacedByTwicePrice;
        return this;
    }

    public Money getReplacedByTwicePrice() {
        return replacedByTwicePrice;
    }

    @Observable
    protected TeVehicle setReplacedByTwiceModelMake(final TeVehicleMake replacedByTwiceModelMake) {
        this.replacedByTwiceModelMake = replacedByTwiceModelMake;
        return this;
    }

    public TeVehicleMake getReplacedByTwiceModelMake() {
        return replacedByTwiceModelMake;
    }

    @Observable
    protected TeVehicle setReplacedByTwiceModel(final TeVehicleModel replacedByTwiceModel) {
        this.replacedByTwiceModel = replacedByTwiceModel;
        return this;
    }

    public TeVehicleModel getReplacedByTwiceModel() {
        return replacedByTwiceModel;
    }

    @Observable
    protected TeVehicle setReplacedByTwice(final TeVehicle replacedByTwice) {
        this.replacedByTwice = replacedByTwice;
        return this;
    }

    public TeVehicle getReplacedByTwice() {
        return replacedByTwice;
    }

    @Observable
    protected TeVehicle setAvgRepPrice(final Money avgRepPrice) {
        this.avgRepPrice = avgRepPrice;
        return this;
    }

    public Money getAvgRepPrice() {
        return avgRepPrice;
    }

    @Observable
    protected TeVehicle setRepPurchasePrice(final Money repPurchasePrice) {
        this.repPurchasePrice = repPurchasePrice;
        return this;
    }

    public Money getRepPurchasePrice() {
        return repPurchasePrice;
    }

    @Observable
    protected TeVehicle setRepPrice(final Money repPrice) {
        this.repPrice = repPrice;
        return this;
    }

    public Money getRepPrice() {
        return repPrice;
    }

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
    public TeVehicle setFinDetails(final TeVehicleFinDetails finDetails) {
        this.finDetails = finDetails;
        return this;
    }

    public TeVehicleFinDetails getFinDetails() {
        return finDetails;
    }

    public TeVehicleFuelUsage getLastFuelUsage() {
        return lastFuelUsage;
    }

    @Observable
    protected TeVehicle setLastFuelUsage(final TeVehicleFuelUsage fu) {
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
    public TeVehicle setFuelUsages(final Set<TeVehicleFuelUsage> fuelUsages) {
        this.fuelUsages.clear();
        this.fuelUsages.addAll(fuelUsages);
        return this;
    }

    public Set<TeVehicleFuelUsage> getFuelUsages() {
        return fuelUsages;
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
    protected TeVehicle setMmake(final TeVehicleMake mmake) {
        this.mmake = mmake;
        return this;
    }

    public TeVehicleMake getMmake() {
        return mmake;
    }

    @Observable
    protected TeVehicle setModelMake(final TeVehicleMake modelMake) {
        this.modelMake = modelMake;
        return this;
    }

    public TeVehicleMake getModelMake() {
        return modelMake;
    }

    @Observable
    protected TeVehicle setModelMake2(final TeVehicleMake modelMake2) {
        this.modelMake2 = modelMake2;
        return this;
    }

    public TeVehicleMake getModelMake2() {
        return modelMake2;
    }

    @Observable
    protected TeVehicle setMmake2(final TeVehicleMake mmake2) {
        this.mmake2 = mmake2;
        return this;
    }

    public TeVehicleMake getMmake2() {
        return mmake2;
    }

    @Observable
    protected TeVehicle setModelMakeKey(final String modelMakeKey) {
        this.modelMakeKey = modelMakeKey;
        return this;
    }

    public String getModelMakeKey() {
        return modelMakeKey;
    }

    @Observable
    protected TeVehicle setModelMakeKeyDuplicate(final String modelMakeKeyDuplicate) {
        this.modelMakeKeyDuplicate = modelMakeKeyDuplicate;
        return this;
    }

    public String getModelMakeKeyDuplicate() {
        return modelMakeKeyDuplicate;
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
    protected TeVehicle setModelKey2(final String modelKey2) {
        this.modelKey2 = modelKey2;
        return this;
    }

    public String getModelKey2() {
        return modelKey2;
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
    protected TeVehicle setStationName(final String stationName) {
        this.stationName = stationName;
        return this;
    }

    public String getStationName() {
        return stationName;
    }

    @Observable
    protected TeVehicle setModelMakeDesc(final String modelMakeDesc) {
        this.modelMakeDesc = modelMakeDesc;
        return this;
    }

    public String getModelMakeDesc() {
        return modelMakeDesc;
    }

    @Observable
    protected TeVehicle setModelMakeKey2(final String modelMakeKey2) {
        this.modelMakeKey2 = modelMakeKey2;
        return this;
    }

    public String getModelMakeKey2() {
        return modelMakeKey2;
    }

    @Observable
    protected TeVehicle setModelMakeKey3(final String modelMakeKey3) {
        this.modelMakeKey3 = modelMakeKey3;
        return this;
    }

    public String getModelMakeKey3() {
        return modelMakeKey3;
    }

    @Observable
    protected TeVehicle setModelMakeKey4(final String modelMakeKey4) {
        this.modelMakeKey4 = modelMakeKey4;
        return this;
    }

    public String getModelMakeKey4() {
        return modelMakeKey4;
    }

    @Observable
    protected TeVehicle setModelMakeKey5(final String modelMakeKey5) {
        this.modelMakeKey5 = modelMakeKey5;
        return this;
    }

    public String getModelMakeKey5() {
        return modelMakeKey5;
    }

    @Observable
    protected TeVehicle setModelMakeKey6(final String modelMakeKey6) {
        this.modelMakeKey6 = modelMakeKey6;
        return this;
    }

    public String getModelMakeKey6() {
        return modelMakeKey6;
    }

    @Observable
    protected TeVehicle setModelMakeKey7(final String modelMakeKey7) {
        this.modelMakeKey7 = modelMakeKey7;
        return this;
    }

    public String getModelMakeKey7() {
        return modelMakeKey7;
    }

    @Observable
    protected TeVehicle setModelMakeKey8(final String modelMakeKey8) {
        this.modelMakeKey8 = modelMakeKey8;
        return this;
    }

    public String getModelMakeKey8() {
        return modelMakeKey8;
    }

    @Observable
    protected TeVehicle setMake(final TeVehicleMake make) {
        this.make = make;
        return this;
    }

    public TeVehicleMake getMake() {
        return make;
    }

}
