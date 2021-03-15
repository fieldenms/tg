package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.CritOnly;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.types.Money;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@KeyTitle(value = "Wo No")
@Ignore
@CompanionObject(ITeWorkOrder.class)
public class TeWorkOrder extends AbstractEntity<String> {

    @IsProperty
    @MapTo
    private TeVehicle vehicle;
    
    @IsProperty
    @Calculated
    private TeVehicle vehicleReplacedBy;
    protected static final ExpressionModel vehicleReplacedBy_ = expr().model(select(TeVehicle.class).where().prop("id").eq().extProp("vehicle").yield().prop("replacedBy").modelAsEntity(TeVehicle.class)).model();
    
    @IsProperty
    @Calculated
    private TeVehicleModel vehicleModel;
    protected static final ExpressionModel vehicleModel_ = expr().prop("vehicle.model").model();

    @IsProperty
    @Calculated
    private TeVehicleMake vehicleMake;
    protected static final ExpressionModel vehicleMake_ = expr().prop("vehicle.modelMake").model();

    @IsProperty
    @Calculated
    private String makeKey;
    protected static final ExpressionModel makeKey_ = expr().model(select(TeVehicle.class).where().prop("id").eq().extProp("vehicle").yield().prop("model.make.key").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    private TeVehicleMake replacedByMake;
    protected static final ExpressionModel replacedByMake_ = expr().model(select(TeVehicle.class).where().prop("id").eq().extProp("vehicle.replacedBy").yield().prop("model.make").modelAsEntity(TeVehicleMake.class)).model();

    @IsProperty
    @Calculated
    private TeVehicleMake replacedByTwiceMake;
    protected static final ExpressionModel replacedByTwiceMake_ = expr().model(select(TeVehicle.class).where().prop("id").eq().extProp("vehicle.replacedByTwice").yield().prop("model.make").modelAsEntity(TeVehicleMake.class)).model();

    @IsProperty
    @Calculated
    private String makeKey2;
    protected static final ExpressionModel makeKey2_ = expr().model(select(TeVehicle.class).where().prop("id").eq().extProp("vehicle").yield().prop("modelMakeKey4").modelAsPrimitive()).model();

    @IsProperty
    @Calculated
    private TeVehicleMake make;
    protected static final ExpressionModel make_ = expr().model(select(TeVehicle.class).where().prop("id").eq().extProp("vehicle").yield().prop("model.make").modelAsEntity(TeVehicleMake.class)).model();

    @IsProperty
    @Calculated
    private TeVehicleModel model;
    protected static final ExpressionModel model_ = expr().model(select(TeVehicle.class).where().prop("id").eq().extProp("vehicle").yield().prop("model").modelAsEntity(TeVehicleModel.class)).model();

    @IsProperty
    @MapTo
    @Title(value = "Act.Cost", desc = "Actual Cost")
    private Money actCost;

    @IsProperty
    @MapTo
    @Title(value = "Est.Cost", desc = "Estimated Cost")
    private Money estCost;

    @IsProperty
    @MapTo
    @Title(value = "Yearly Cost", desc = "Yearly Cost")
    private Money yearlyCost;

//    @IsProperty(TeWorkOrder.class)
//    @MapTo
//    @Title(value = "Important Property", desc = "Property that has a special meaning")
//    private PropertyDescriptor<TeWorkOrder> importantProperty;

    @IsProperty
    @Title(value = "Org Unit", desc = "Desc")
    private TgOrgUnit1 orgUnit1;

    @IsProperty
    @CritOnly(Type.RANGE)
    @Title(value = "OrgUnit1", desc = "A range crit only property")
    private TgOrgUnit1 orgunitCritOnly;

    @IsProperty
    @CritOnly(Type.SINGLE)
    @Title(value = "OrgUnit1 Single", desc = "A single crit only property")
    private TgOrgUnit1 orgunitCritOnlySingle;

    @IsProperty
    @CritOnly(Type.SINGLE)
    @Title(value = "Int Single", desc = "A single crit only property of type integer")
    private Integer intSingle;

    @IsProperty
    @CritOnly(Type.RANGE)
    @Title(value = "Int Range", desc = "A range crit only property of type integer")
    private Integer intRange;

    @IsProperty
    @CritOnly(Type.SINGLE)
    @Title(value = "Boolean Single", desc = "A single crit only property of type boolean")
    private boolean boolSingle = false;

    @IsProperty
    @CritOnly(Type.SINGLE)
    @Title(value = "String Single", desc = "A single crit only property of type String")
    private String stringSingle;

    @IsProperty
    @CritOnly(Type.SINGLE)
    @Title(value = "Money Single", desc = "A single crit only property of type Money")
    private Money moneySingle;

    @IsProperty
    @CritOnly(Type.SINGLE)
    @Title(value = "BigDecimal Single", desc = "A single crit only property of type BigDecimal")
    private BigDecimal bigDecimalSingle;

    @IsProperty
    @CritOnly(Type.SINGLE)
    @Title(value = "Date Single", desc = "A single crit only property of type Date.")
    private Date dateSingle;

    @Observable
    public TeWorkOrder setDateSingle(final Date dateSingle) {
        this.dateSingle = dateSingle;
        return this;
    }

    public Date getDateSingle() {
        return dateSingle;
    }

    @Observable
    public TeWorkOrder setBigDecimalSingle(final BigDecimal bigDecimalSingle) {
        this.bigDecimalSingle = bigDecimalSingle;
        return this;
    }

    public BigDecimal getBigDecimalSingle() {
        return bigDecimalSingle;
    }


    @Observable
    public TeWorkOrder setMoneySingle(final Money moneySingle) {
        this.moneySingle = moneySingle;
        return this;
    }

    public Money getMoneySingle() {
        return moneySingle;
    }

    @Observable
    public TeWorkOrder setStringSingle(final String stringSingle) {
        this.stringSingle = stringSingle;
        return this;
    }

    public String getStringSingle() {
        return stringSingle;
    }

    @Observable
    public TeWorkOrder setBoolSingle(final boolean boolSingle) {
        this.boolSingle = boolSingle;
        return this;
    }

    public boolean getBoolSingle() {
        return boolSingle;
    }

    @Observable
    public TeWorkOrder setIntRange(final Integer intRange) {
        this.intRange = intRange;
        return this;
    }

    public Integer getIntRange() {
        return intRange;
    }

    @Observable
    public TeWorkOrder setIntSingle(final Integer intSingle) {
        this.intSingle = intSingle;
        return this;
    }

    public Integer getIntSingle() {
        return intSingle;
    }

    @Observable
    public TeWorkOrder setOrgunitCritOnlySingle(final TgOrgUnit1 orgunitCritOnlySingle) {
        this.orgunitCritOnlySingle = orgunitCritOnlySingle;
        return this;
    }

    public TgOrgUnit1 getOrgunitCritOnlySingle() {
        return orgunitCritOnlySingle;
    }

    @Observable
    public TeWorkOrder setOrgunitCritOnly(final TgOrgUnit1 orgunitCritOnly) {
        this.orgunitCritOnly = orgunitCritOnly;
        return this;
    }

    public TgOrgUnit1 getOrgunitCritOnly() {
        return orgunitCritOnly;
    }

    @Observable
    public TeWorkOrder setOrgUnit1(final TgOrgUnit1 orgUnit1) {
        this.orgUnit1 = orgUnit1;
        return this;
    }

    public TgOrgUnit1 getOrgUnit1() {
        return orgUnit1;
    }

    @Observable
    public TeWorkOrder setYearlyCost(final Money yearlyCost) {
        this.yearlyCost = yearlyCost;
        return this;
    }

    public Money getYearlyCost() {
        return yearlyCost;
    }

    @Observable
    public TeWorkOrder setEstCost(final Money estCost) {
        this.estCost = estCost;
        return this;
    }

    public Money getEstCost() {
        return estCost;
    }

    @Observable
    public TeWorkOrder setActCost(final Money actCost) {
        this.actCost = actCost;
        return this;
    }

    public Money getActCost() {
        return actCost;
    }

//    public PropertyDescriptor<TeWorkOrder> getImportantProperty() {
//        return importantProperty;
//    }
//
//    @Observable
//    public TeWorkOrder setImportantProperty(final PropertyDescriptor<TeWorkOrder> importantProperty) {
//        this.importantProperty = importantProperty;
//        return this;
//    }

    public TeVehicle getVehicle() {
        return vehicle;
    }

    @Observable
    public TeWorkOrder setVehicle(final TeVehicle vehicle) {
        this.vehicle = vehicle;
        return this;
    }
    
    @Observable
    protected TeWorkOrder setVehicleModel(final TeVehicleModel vehicleModel) {
        this.vehicleModel = vehicleModel;
        return this;
    }

    public TeVehicleModel getVehicleModel() {
        return vehicleModel;
    }

    @Observable
    protected TeWorkOrder setVehicleMake(final TeVehicleMake vehicleMake) {
        this.vehicleMake = vehicleMake;
        return this;
    }

    public TeVehicleMake getVehicleMake() {
        return vehicleMake;
    }

    @Observable
    protected TeWorkOrder setMakeKey(final String makeKey) {
        this.makeKey = makeKey;
        return this;
    }

    public String getMakeKey() {
        return makeKey;
    }

    @Observable
    protected TeWorkOrder setReplacedByMake(final TeVehicleMake replacedByMake) {
        this.replacedByMake = replacedByMake;
        return this;
    }

    public TeVehicleMake getReplacedByMake() {
        return replacedByMake;
    }

    @Observable
    protected TeWorkOrder setReplacedByTwiceMake(final TeVehicleMake replacedByTwiceMake) {
        this.replacedByTwiceMake = replacedByTwiceMake;
        return this;
    }

    public TeVehicleMake getReplacedByTwiceMake() {
        return replacedByTwiceMake;
    }

    @Observable
    protected TeWorkOrder setMakeKey2(final String makeKey2) {
        this.makeKey2 = makeKey2;
        return this;
    }

    public String getMakeKey2() {
        return makeKey2;
    }
    
    @Observable
    protected TeWorkOrder setMake(final TeVehicleMake make) {
        this.make = make;
        return this;
    }

    public TeVehicleMake getMake() {
        return make;
    }

    @Observable
    protected TeWorkOrder setModel(final TeVehicleModel model) {
        this.model = model;
        return this;
    }

    public TeVehicleModel getModel() {
        return model;
    }
    
    @Observable
    protected TeWorkOrder setVehicleReplacedBy(final TeVehicle vehicleReplacedBy) {
        this.vehicleReplacedBy = vehicleReplacedBy;
        return this;
    }

    public TeVehicle getVehicleReplacedBy() {
        return vehicleReplacedBy;
    }

}