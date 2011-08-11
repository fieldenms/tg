package ua.com.fielden.platform.example.swing.ei;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.Proxy;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.Max;
import ua.com.fielden.platform.types.Money;

/**
 * An entity used for demonstration of a custom entity inspector.
 *
 * TODO: add collectional property.
 *
 * @author 01es
 *
 */
@KeyType(String.class)
public class InspectedEntity extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    private Integer intProperty;
    @IsProperty
    private BigDecimal decimalProperty;
    @IsProperty
    private Money moneyProperty;
    @IsProperty
    private Date dateProperty;
    @IsProperty
    private Boolean booleanProperty = false;
    @IsProperty
    private InspectedEntity entityPropertyOne;
    @IsProperty
    private InspectedEntity entityPropertyTwo;
    @IsProperty(Integer.class)
    private List<Integer> collectionalProperty = new ArrayList<Integer>();

    protected InspectedEntity() {
	collectionalProperty = new ArrayList<Integer>();
	setCollectionalProperty(Arrays.asList(new Integer[] {23, 2, 6, 8, 9, 89, 87, 45}));
    }

    @Max(150)
    @Observable
    @Override
    public InspectedEntity setDesc(final String desc) {
	super.setDesc(desc);
        return this;
    }

    public Integer getIntProperty() {
	return intProperty;
    }

    @Observable
    public void setIntProperty(final Integer intProperty) {
	this.intProperty = intProperty;
    }

    public BigDecimal getDecimalProperty() {
	return decimalProperty;
    }

    @Observable
    public void setDecimalProperty(final BigDecimal decimalProperty) {
	this.decimalProperty = decimalProperty;
    }

    public Money getMoneyProperty() {
	return moneyProperty;
    }

    @Observable
    public void setMoneyProperty(final Money moneyProperty) {
	this.moneyProperty = moneyProperty;
    }

    public Date getDateProperty() {
	return dateProperty;
    }

    @Observable
    public void setDateProperty(final Date dateProperty) {
	this.dateProperty = dateProperty;
    }

    public Boolean getBooleanProperty() {
	return booleanProperty;
    }

    @Observable
    public void setBooleanProperty(final Boolean booleanProperty) {
	this.booleanProperty = booleanProperty;
    }

    @Proxy
    public InspectedEntity getEntityPropertyOne() {
	return entityPropertyOne;
    }

    @Observable
    @EntityExists(InspectedEntity.class)
    public void setEntityPropertyOne(final InspectedEntity entityPropertyOne) {
	this.entityPropertyOne = entityPropertyOne;
    }

    @Proxy
    public InspectedEntity getEntityPropertyTwo() {
	return entityPropertyTwo;
    }

    @Observable
    @EntityExists(InspectedEntity.class)
    public void setEntityPropertyTwo(final InspectedEntity entityPropertyTwo) {
	this.entityPropertyTwo = entityPropertyTwo;
    }

    public List<Integer> getCollectionalProperty() {
        return collectionalProperty;
    }

    @Observable
    public void setCollectionalProperty(final List<Integer> collectionalProperty) {
	this.collectionalProperty.clear();
        this.collectionalProperty.addAll(collectionalProperty);
    }

}
