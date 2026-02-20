package ua.com.fielden.platform.test_entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.validation.annotation.DomainValidation;
import ua.com.fielden.platform.entity.validation.annotation.Final;
import ua.com.fielden.platform.entity.validation.annotation.GreaterOrEqual;
import ua.com.fielden.platform.equery.lifecycle.Categorizer;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.warning;

/**
 * Entity class used for testing.
 *
 * @author TG Team
 *
 */
@KeyType(value = String.class, descendingOrder = true)
@KeyTitle(value = "Entity No", desc = "Key Property")
@DescTitle(value = "Description", desc = "Description Property")
@DescRequired("Property \"{{prop-title}}\" in entity \"{{entity-title}}\" does not permit blank values.")
public class Entity extends AbstractEntity<String> {

    public static final String NOT_NULL_MSG = "Missing value";

    @IsProperty
    @Required
    @Readonly
    @Title(value = "First Property", desc = "used for testing")
    @Calculated
    private Integer firstProperty = null;
    protected static ExpressionModel firstProperty_ = expr().val(null).model();

    @IsProperty
    @Title("Observable Property")
    @Required(NOT_NULL_MSG)
    @CritOnly
    private BigDecimal observableProperty = BigDecimal.ZERO;

    @IsProperty
    @Monitoring(Categorizer.class)
    private String monitoring;

    @IsProperty
    @MapTo
    private BigDecimal observablePropertyInitialisedAsNull;

    @IsProperty
    @Invisible
    @Final(persistedOnly = false)
    private BigDecimal finalProperty;
    
    @IsProperty(value = BigDecimal.class, linkProperty = "--stub to pass tests--")
    @Required
    private List<BigDecimal> bigDecimals = new ArrayList<>();
    
    @IsProperty(Entity.class)
    private List<Entity> entities = new ArrayList<Entity>();
    
    @IsProperty
    private Entity entity;
    
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

    @IsProperty
    private ClassWithMap classWithMapProp;

    @IsProperty(Entity.class)
    private PropertyDescriptor<Entity> propertyDescriptor;

    public Integer getFirstProperty() {
        return firstProperty;
    }

    @GreaterOrEqual(50)
    @DomainValidation
    @Observable
    public Entity setFirstProperty(final Integer property) {
        this.firstProperty = property;
        return this;
    }

    public BigDecimal getObservableProperty() {
        return observableProperty;
    }

    @Observable
    public Entity setObservableProperty(final BigDecimal observableProperty) {
        this.observableProperty = observableProperty;
        return this;
    }

    public BigDecimal getFinalProperty() {
        return finalProperty;
    }

    @Observable
    public void setFinalProperty(final BigDecimal finalProperty) {
        this.finalProperty = finalProperty;
    }

    public List<BigDecimal> getBigDecimals() {
        return bigDecimals;
    }

    @Observable
    @DomainValidation
    public Entity setBigDecimals(final List<BigDecimal> bigDecimals) {
        this.bigDecimals.clear();
        this.bigDecimals.addAll(bigDecimals);
        return this;
    }

    @Observable
    public Entity addToBigDecimals(final BigDecimal value) {
        bigDecimals.add(value);
        return this;
    }

    @Observable
    @DomainValidation
    public Entity removeFromBigDecimals(final BigDecimal value) {
        bigDecimals.remove(value);
        return this;
    }

    public List<Entity> getEntities() {
        return entities;
    }

    @Observable
    public void setEntities(final List<Entity> entities) {
        this.entities.clear();
        this.entities.addAll(entities);
    }

    public BigDecimal getObservablePropertyInitialisedAsNull() {
        return observablePropertyInitialisedAsNull;
    }

    @Observable
    public void setObservablePropertyInitialisedAsNull(final BigDecimal observablePropertyInitialisedAsNull) {
        this.observablePropertyInitialisedAsNull = observablePropertyInitialisedAsNull;
    }

    public Entity getEntity() {
        return entity;
    }

    @Observable
    public Entity setEntity(final Entity entity) {
        this.entity = entity;
        return this;
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
            throw failure("The value of 100 is not permitted");
        }
        this.number = number;
        if (number.equals(777)) { // DYNAMIC warning generation :
            throw warning("DYNAMIC validation : The value of 777 is dangerous.");
        }
    }

    public Integer getDependent() {
        return dependent;
    }

    @Observable
    public void setDependent(final Integer dependent) throws Result {
        if (main != null && dependent != null && dependent > main) {
            throw failure("The property [dependent] cannot be > [main]");
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

    public PropertyDescriptor<Entity> getPropertyDescriptor() {
        return propertyDescriptor;
    }

    @Observable
    public void setPropertyDescriptor(final PropertyDescriptor<Entity> propertyDescriptor) {
        this.propertyDescriptor = propertyDescriptor;
    }

    public ClassWithMap getClassWithMapProp() {
        return classWithMapProp;
    }

    @Observable
    public void setClassWithMapProp(final ClassWithMap classWithMapProp) {
        this.classWithMapProp = classWithMapProp;
    }

    public String getMonitoring() {
        return monitoring;
    }

    @Observable
    public void setMonitoring(final String monitoring) {
        this.monitoring = monitoring;
    }
    
    @Override
    public void setVersion(Long ver) {
        super.setVersion(ver);
    }
    
    @Override
    public void setId(Long id) {
        super.setId(id);
    }

    @Observable
    @Override
    public Entity setKey(String key) {
        super.setKey(key);
        return this;
    }

    @Override
    @Observable
    public Entity setDesc(String desc) {
        return super.setDesc(desc);
    }
}
