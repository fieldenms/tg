package ua.com.fielden.platform.swing.review.report.analysis.lifecycle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Dependent;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.Invisible;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Monitoring;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Readonly;
import ua.com.fielden.platform.entity.annotation.Required;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.validation.annotation.DomainValidation;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.entity.validation.annotation.GreaterOrEqual;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.types.Money;

/**
 * Entity class used for testing.
 *
 * @author 01es
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Entity No", desc = "Key Property")
@DescTitle(value = "Description", desc = "Description Property")
@DescRequired
public class TimelineEntity extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    public static final String NOT_NULL_MSG = "Missing value";

    @IsProperty
    @Required
    @Readonly
    @Title(value = "First Property", desc = "used for testing")
    private Integer firstProperty = null;
    @IsProperty
    @Title("Observable Property")
    private Double observableProperty = 0.0;
    @IsProperty
    @Monitoring(Categorizer.class)
    private String monitoring;
    @IsProperty
    private Double observablePropertyInitialisedAsNull;
    @IsProperty
    @Invisible
    private Double finalProperty;
    @IsProperty(value = Double.class, linkProperty = "--stub to pass tests--")
    private List<Double> doubles = new ArrayList<Double>();
    @IsProperty(TimelineEntity.class)
    private List<TimelineEntity> entities = new ArrayList<TimelineEntity>();
    @IsProperty
    private TimelineEntity entity;
    @IsProperty
    private Date date;
    @IsProperty
    private Money money;
    @IsProperty
    private Integer number;
    @IsProperty
    @Dependent("dependent")
    private Integer main = 20;
    @IsProperty
    private Integer dependent = 10;
    @IsProperty
    private String strProp;

    @IsProperty(TimelineEntity.class)
    private PropertyDescriptor<TimelineEntity> propertyDescriptor;

    protected TimelineEntity() {

    }

    public Integer getFirstProperty() {
        return firstProperty;
    }

    @NotNull
    @GreaterOrEqual(50)
    @DomainValidation
    @Observable
    public TimelineEntity setFirstProperty(final Integer property) {
        this.firstProperty = property;
        return this;
    }

    public Double getObservableProperty() {
        return observableProperty;
    }

    @Observable
    @NotNull(NOT_NULL_MSG)
    public TimelineEntity setObservableProperty(final Double observableProperty) {
        this.observableProperty = observableProperty;
        return this;
    }

    public Double getFinalProperty() {
        return finalProperty;
    }

    @Final
    @Observable
    public void setFinalProperty(final Double finalProperty) {
        this.finalProperty = finalProperty;
    }

    public List<Double> getDoubles() {
        return doubles;
    }

    @Observable
    @NotNull
    @DomainValidation
    public TimelineEntity setDoubles(final List<Double> doubles) {
        this.doubles.clear();
        this.doubles.addAll(doubles);
        return this;
    }

    @Observable
    @NotNull
    public TimelineEntity addToDoubles(final Double value) {
        doubles.add(value);
        return this;
    }

    @Observable
    @DomainValidation
    public TimelineEntity removeFromDoubles(final Double value) {
        doubles.remove(value);
        return this;
    }

    public List<TimelineEntity> getEntities() {
        return entities;
    }

    @Observable
    public void setEntities(final List<TimelineEntity> entities) {
        this.entities.clear();
        this.entities.addAll(entities);
    }

    public Double getObservablePropertyInitialisedAsNull() {
        return observablePropertyInitialisedAsNull;
    }

    @Observable
    @NotNull
    public void setObservablePropertyInitialisedAsNull(final Double observablePropertyInitialisedAsNull) {
        this.observablePropertyInitialisedAsNull = observablePropertyInitialisedAsNull;
    }

    public TimelineEntity getEntity() {
        return entity;
    }

    @Observable
    public void setEntity(final TimelineEntity entity) {
        this.entity = entity;
    }

    public Date getDate() {
        return date;
    }

    @Observable
    public void setDate(final Date date) {
        this.date = date;
    }

    public Money getMoney() {
        return money;
    }

    @Observable
    public void setMoney(final Money money) {
        this.money = money;
    }

    public Integer getNumber() {
        return number;
    }

    @Observable
    @DomainValidation
    public void setNumber(final Integer number) throws Result {
        number.toString(); // need to enforce NPE when number is null for testing purposes
        if (number.equals(50)) { // causes IllegalArgumentException :
            throw new IllegalArgumentException("The value of 50 is not permitted");
        }
        if (number.equals(100)) { // DYNAMIC validation :
            throw new Result("The value of 100 is not permitted", new Exception("The value of 100 is not permitted"));
        }
        this.number = number;
        if (number.equals(777)) { // DYNAMIC warning generation :
            throw new Warning("DYNAMIC validation : The value of 777 is dangerous.");
        }
    }

    public Integer getDependent() {
        return dependent;
    }

    @Observable
    public void setDependent(final Integer dependent) throws Result {
        if (main != null && dependent != null && dependent > main) {
            throw new Result("", new Exception("The property [dependent] cannot be > [main]"));
        }
        this.dependent = dependent;
    }

    public Integer getMain() {
        return main;
    }

    @Observable
    public void setMain(final Integer main) {
        this.main = main;
    }

    @Observable
    public void setStrProp(final String strProp) {
        this.strProp = strProp;
    }

    public String getStrProp() {
        return strProp;
    }

    public PropertyDescriptor<TimelineEntity> getPropertyDescriptor() {
        return propertyDescriptor;
    }

    @Observable
    public void setPropertyDescriptor(final PropertyDescriptor<TimelineEntity> propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    public String getMonitoring() {
        return monitoring;
    }

    @Observable
    public void setMonitoring(final String monitoring) {
        this.monitoring = monitoring;
    }

}