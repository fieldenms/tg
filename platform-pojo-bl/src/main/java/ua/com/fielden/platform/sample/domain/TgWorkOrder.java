package ua.com.fielden.platform.sample.domain;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
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
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.types.Money;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@KeyTitle(value = "Wo No")
@Ignore
public class TgWorkOrder extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @Title(value = "Vehicle", desc = "Vehicle under repair")
    @MapTo
    private TgVehicle vehicle;

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

    @IsProperty(TgWorkOrder.class)
    @MapTo
    @Title(value = "Important Property", desc = "Property that has a special meaning")
    private PropertyDescriptor<TgWorkOrder> importantProperty;

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

    @Observable
    public TgWorkOrder setIntRange(final Integer intRange) {
        this.intRange = intRange;
        return this;
    }

    public Integer getIntRange() {
        return intRange;
    }




    @Observable
    public TgWorkOrder setIntSingle(final Integer intSingle) {
        this.intSingle = intSingle;
        return this;
    }

    public Integer getIntSingle() {
        return intSingle;
    }




    @Observable
    public TgWorkOrder setOrgunitCritOnlySingle(final TgOrgUnit1 orgunitCritOnlySingle) {
        this.orgunitCritOnlySingle = orgunitCritOnlySingle;
        return this;
    }

    public TgOrgUnit1 getOrgunitCritOnlySingle() {
        return orgunitCritOnlySingle;
    }

    @Observable
    public TgWorkOrder setOrgunitCritOnly(final TgOrgUnit1 orgunitCritOnly) {
        this.orgunitCritOnly = orgunitCritOnly;
        return this;
    }

    public TgOrgUnit1 getOrgunitCritOnly() {
        return orgunitCritOnly;
    }


    @Observable
    public TgWorkOrder setOrgUnit1(final TgOrgUnit1 orgUnit1) {
        this.orgUnit1 = orgUnit1;
        return this;
    }

    public TgOrgUnit1 getOrgUnit1() {
        return orgUnit1;
    }

    @Observable
    public TgWorkOrder setYearlyCost(final Money yearlyCost) {
        this.yearlyCost = yearlyCost;
        return this;
    }

    public Money getYearlyCost() {
        return yearlyCost;
    }

    @Observable
    public TgWorkOrder setEstCost(final Money estCost) {
        this.estCost = estCost;
        return this;
    }

    public Money getEstCost() {
        return estCost;
    }

    @Observable
    public TgWorkOrder setActCost(final Money actCost) {
        this.actCost = actCost;
        return this;
    }

    public Money getActCost() {
        return actCost;
    }

    public PropertyDescriptor<TgWorkOrder> getImportantProperty() {
        return importantProperty;
    }

    @Observable
    public void setImportantProperty(final PropertyDescriptor<TgWorkOrder> importantProperty) {
        this.importantProperty = importantProperty;
    }

    public TgVehicle getVehicle() {
        return vehicle;
    }

    @Observable
    @EntityExists(TgVehicle.class)
    public void setVehicle(final TgVehicle vehicle) {
        this.vehicle = vehicle;
    }
}
