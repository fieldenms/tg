package ua.com.fielden.platform.sample.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.Ignore;

import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
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

    private static final ExpressionModel calc1_ = expr().prop("price.amount").add().prop("purchasePrice.amount").model();
    private static final ExpressionModel calc2_ = expr().model(select(TgFuelUsage.class).yield().sumOf().prop("qty").modelAsPrimitive()).model();
    private static final ExpressionModel calc3_ = expr().model(select(TgFuelUsage.class).where().prop("date").lt().extProp("initDate").yield().sumOf().prop("qty").modelAsPrimitive()).model();
    private static final ExpressionModel calc4_ = expr().model(select(TgFuelUsage.class).where().prop("qty").lt().prop("calc2").yield().sumOf().prop("qty").modelAsPrimitive()).model();
    private static final ExpressionModel calc5_ = expr().prop("calc1").add().prop("calc1").model();
    private static final ExpressionModel calc6_ = expr().prop("calc1").div().prop("calc3").model();


    @IsProperty
    @Calculated(attribute = CalculatedPropertyAttribute.NO_ATTR, category = CalculatedPropertyCategory.EXPRESSION, contextPath = "", contextualExpression = "", origination = "")
    @Title(value = "Calc1", desc = "Calculated property 1")
    private BigDecimal calc1;

    @Observable
    public TgVehicle setCalc1(final BigDecimal calc1) {
	this.calc1 = calc1;
	return this;
    }

    public BigDecimal getCalc1() {
	return calc1;
    }

    @IsProperty
    @Calculated(attribute = CalculatedPropertyAttribute.NO_ATTR, category = CalculatedPropertyCategory.EXPRESSION, contextPath = "", contextualExpression = "", origination = "")
    @Title(value = "Calc2", desc = "Calc2")
    private BigDecimal calc2;

    @Observable
    public TgVehicle setCalc2(final BigDecimal calc2) {
	this.calc2 = calc2;
	return this;
    }

    public BigDecimal getCalc2() {
	return calc2;
    }

    @IsProperty
    @Calculated(attribute = CalculatedPropertyAttribute.NO_ATTR, category = CalculatedPropertyCategory.EXPRESSION, contextPath = "", contextualExpression = "", origination = "")
    @Title(value = "Calc3", desc = "Calc3")
    private BigDecimal calc3;

    @Observable
    public TgVehicle setCalc3(final BigDecimal calc3) {
	this.calc3 = calc3;
	return this;
    }

    public BigDecimal getCalc3() {
	return calc3;
    }

//    @IsProperty
//    @Calculated(attribute = CalculatedPropertyAttribute.NO_ATTR, category = CalculatedPropertyCategory.EXPRESSION, contextPath = "", contextualExpression = "", origination = "")
//    @Title(value = "Calc4", desc = "Calc4")
    private BigDecimal calc4;

    @Observable
    public TgVehicle setCalc4(final BigDecimal calc4) {
	this.calc4 = calc4;
	return this;
    }

    public BigDecimal getCalc4() {
	return calc4;
    }

    @IsProperty
    @Calculated(attribute = CalculatedPropertyAttribute.NO_ATTR, category = CalculatedPropertyCategory.EXPRESSION, contextPath = "", contextualExpression = "", origination = "")
    @Title(value = "Calc5", desc = "Calc5")
    private BigDecimal calc5;

    @Observable
    public TgVehicle setCalc5(final BigDecimal calc5) {
	this.calc5 = calc5;
	return this;
    }

    public BigDecimal getCalc5() {
	return calc5;
    }

    @IsProperty
    @Calculated(attribute = CalculatedPropertyAttribute.NO_ATTR, category = CalculatedPropertyCategory.EXPRESSION, contextPath = "", contextualExpression = "", origination = "")
    @Title(value = "Calc6", desc = "Calc6")
    private BigDecimal calc6;

    @Observable
    public TgVehicle setCalc6(final BigDecimal calc6) {
	this.calc6 = calc6;
	return this;
    }

    public BigDecimal getCalc6() {
	return calc6;
    }

    @IsProperty @MapTo
    private Date initDate;

    @IsProperty @MapTo
    private TgVehicle replacedBy;

    @IsProperty @MapTo
    private TgOrgUnit5 station;

    @IsProperty @MapTo @Required @Title(value = "Model", desc = "Model")
    private TgVehicleModel model;

    @IsProperty @MapTo @Title(value = "Price", desc = "Price")
    private Money price;

    @IsProperty @MapTo(userType = ISimpleMoneyType.class)
    private Money purchasePrice;

    @IsProperty @MapTo @Title(value = "Active", desc = "Active")
    private boolean active;

    @IsProperty @MapTo @Title(value = "Leased", desc = "Leased?")
    private boolean leased;

    @IsProperty(value = TgFuelUsage.class, linkProperty = "vehicle") @Title(value = "Fuel usages", desc = "Fuel usages")
    private Set<TgFuelUsage> fuelUsages = new HashSet<TgFuelUsage>();
    public Set<TgFuelUsage> getFuelUsages() { return fuelUsages; }

    @IsProperty
    @MapTo(length = 10, precision = 3, scale = 10)
    @Title(value = "Last Meter Reading", desc = "Last meter reading")
    private BigDecimal lastMeterReading;

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

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgVehicle() {
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
    public void setInitDate(final Date initDate) {
        this.initDate = initDate;
    }

    public TgVehicle getReplacedBy() {
        return replacedBy;
    }

    @Observable  @EntityExists(TgVehicle.class)
    public void setReplacedBy(final TgVehicle replacedBy) {
        this.replacedBy = replacedBy;
    }
}