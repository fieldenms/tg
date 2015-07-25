package ua.com.fielden.platform.eql.entities;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;

@KeyType(String.class)
@MapEntityTo
public class TgtVehicle extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo
    private Date initDate;

    @IsProperty
    @MapTo
    private TgtVehicle replacedBy;

    @IsProperty
    @MapTo
    private TgtStation station;

    @IsProperty
    @MapTo
    @Required
    @Title("Model")
    private TgtVehicleModel model;

    @IsProperty
    @MapTo
    private BigDecimal price;

    @IsProperty
    @MapTo
    private BigDecimal purchasePrice;

    @IsProperty
    @MapTo
    @Title("Active")
    private boolean active;

    @IsProperty
    @MapTo
    @Title("Leased?")
    private boolean leased;

    @IsProperty
    @MapTo(length = 10, precision = 10, scale = 3)
    @Title("Last meter reading")
    private BigDecimal lastMeterReading;

    @IsProperty
    @Readonly
    @Calculated
    @Title(value = "Mostly more than 10l", desc = "Indicates that vehicle has more fuel usages with qty exceeding 10l than those below 10l")
    private boolean mostlyMoreThan10L;
    private static PrimitiveResultQueryModel exceeds10LCountSubqry = select(TgtFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().prop("qty").gt().val(10).yield().countAll().modelAsPrimitive();
    private static PrimitiveResultQueryModel lessThan10LCountSubqry = select(TgtFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().prop("qty").le().val(10).yield().countAll().modelAsPrimitive();
    private static ExpressionModel mostlyMoreThan10L_ = expr().caseWhen().model(exceeds10LCountSubqry).gt().model(lessThan10LCountSubqry).then().val(true).otherwise().val(false).endAsBool().model();

    @IsProperty(linkProperty = "vehicle")
    @Calculated
    private TgtFuelUsage lastFuelUsage;
    private static final ExpressionModel lastFuelUsage_ = expr().model(select(TgtFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().notExists(select(TgtFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).model()).model();

    @IsProperty
    @Calculated
    private Integer constValueProp;
    private static final ExpressionModel constValueProp_ = expr().val(10).add().val(20).model();

    @IsProperty
    @Calculated("price + purchasePrice")
    private BigDecimal calc0;

    @IsProperty
    @Calculated
    private BigDecimal lastFuelUsageQty;
    private static final ExpressionModel lastFuelUsageQty_ = expr().model(select(TgtFuelUsage.class).where().prop("vehicle").eq().extProp("id").and().notExists(select(TgtFuelUsage.class).where().prop("vehicle").eq().extProp("vehicle").and().prop("date").gt().extProp("date").model()).yield().prop("qty").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    private BigDecimal sumOfPrices;
    private static final ExpressionModel sumOfPrices_ = expr().val(1).mult().prop("price").add().prop("purchasePrice").model();

    @IsProperty
    @Calculated
    private BigDecimal calc2;
    private static final ExpressionModel calc2_ = expr().model(select(TgtFuelUsage.class).yield().sumOf().prop("qty").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    private BigDecimal calc3;
    private static final ExpressionModel calc3_ = expr().model(select(TgtFuelUsage.class).where().prop("date").lt().extProp("initDate").yield().sumOf().prop("qty").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    private BigDecimal calc4;
    private static final ExpressionModel calc4_ = expr().model(select(TgtFuelUsage.class).where().prop("qty").lt().extProp("calc2").yield().sumOf().prop("qty").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    private BigDecimal calc5;
    private static final ExpressionModel calc5_ = expr().prop("sumOfPrices").mult().val(2).model();

    @IsProperty
    @Calculated
    private BigDecimal calc6;
    private static final ExpressionModel calc6_ = expr().prop("sumOfPrices").div().prop("calc3").model();

    @IsProperty
    @Calculated
    private TgtVehicleModel calcModel;
    private static final ExpressionModel calcModel_ = expr().prop("model").model();

    @Observable
    protected TgtVehicle setMostlyMoreThan10L(final boolean mostlyMoreThan10L) {
        this.mostlyMoreThan10L = mostlyMoreThan10L;
        return this;
    }

    public boolean getMostlyMoreThan10L() {
        return mostlyMoreThan10L;
    }

    public TgtFuelUsage getLastFuelUsage() {
        return lastFuelUsage;
    }

    @Observable
    protected TgtVehicle setLastFuelUsage(final TgtFuelUsage fu) {
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

    public TgtVehicleModel getCalcModel() {
        return calcModel;
    }

    public BigDecimal getSumOfPrices() {
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
    public TgtVehicle setLastMeterReading(final BigDecimal lastMeterReading) {
        this.lastMeterReading = lastMeterReading;
        return this;
    }

    public BigDecimal getLastMeterReading() {
        return lastMeterReading;
    }

    @Observable
    public TgtVehicle setLeased(final boolean leased) {
        this.leased = leased;
        return this;
    }

    public boolean getLeased() {
        return leased;
    }

    @Observable
    public TgtVehicle setActive(final boolean active) {
        this.active = active;
        return this;
    }

    public boolean getActive() {
        return active;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }

    @Observable
    public TgtVehicle setPurchasePrice(final BigDecimal purchasePrice) {
        this.purchasePrice = purchasePrice;
        return this;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Observable
    public TgtVehicle setPrice(final BigDecimal price) {
        this.price = price;
        return this;
    }

    public TgtVehicleModel getModel() {
        return model;
    }

    @Observable
    public TgtVehicle setModel(final TgtVehicleModel model) {
        this.model = model;
        return this;
    }

    public TgtStation getStation() {
        return station;
    }

    @Observable
    public TgtVehicle setStation(final TgtStation station) {
        this.station = station;
        return this;
    }

    public Date getInitDate() {
        return initDate;
    }

    @Observable
    public TgtVehicle setInitDate(final Date initDate) {
        this.initDate = initDate;
        return this;
    }

    public TgtVehicle getReplacedBy() {
        return replacedBy;
    }

    @Observable
    public TgtVehicle setReplacedBy(final TgtVehicle replacedBy) {
        this.replacedBy = replacedBy;
        return this;
    }
}