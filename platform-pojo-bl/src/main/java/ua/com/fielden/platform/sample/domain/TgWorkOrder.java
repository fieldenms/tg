package ua.com.fielden.platform.sample.domain;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
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

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgWorkOrder() {
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
