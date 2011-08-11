package ua.com.fielden.platform.expression.ast.visitor.entities;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.types.Money;

@KeyType(String.class)
public class EntityLevel1 extends AbstractEntity<String> {

    @IsProperty
    private String strProperty;

    @IsProperty
    private EntityLevel2 entityProperty;

    @IsProperty
    private EntityLevel1 selfProperty;

    @IsProperty(EntityLevel2.class)
    private List<EntityLevel2> collectional;

    @IsProperty
    private Money moneyProperty;

    @IsProperty
    private Integer intProperty;

    @IsProperty
    private Date dateProperty;

    @IsProperty
    private BigDecimal decimalProperty;

    public String getStrProperty() {
        return strProperty;
    }
    @Observable
    public void setStrProperty(final String strProperty) {
        this.strProperty = strProperty;
    }

    public EntityLevel2 getEntityProperty() {
        return entityProperty;
    }
    @Observable
    public void setEntityProperty(final EntityLevel2 entityProperty) {
        this.entityProperty = entityProperty;
    }

    public EntityLevel1 getSelfProperty() {
        return selfProperty;
    }
    @Observable
    public void setSelfProperty(final EntityLevel1 selfProperty) {
        this.selfProperty = selfProperty;
    }

    public List<EntityLevel2> getCollectional() {
        return collectional;
    }
    @Observable
    public void setCollectional(final List<EntityLevel2> collectional) {
        this.collectional = collectional;
    }

    public Money getMoneyProperty() {
        return moneyProperty;
    }
    @Observable
    public void setMoneyProperty(final Money moneyProperty) {
        this.moneyProperty = moneyProperty;
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

    public Date getDateProperty() {
        return dateProperty;
    }
    @Observable
    public void setDateProperty(final Date dateProperty) {
        this.dateProperty = dateProperty;
    }
}
