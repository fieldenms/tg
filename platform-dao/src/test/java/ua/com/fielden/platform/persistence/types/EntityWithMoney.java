package ua.com.fielden.platform.persistence.types;

import ua.com.fielden.platform.dao.IEntityWithMoney;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.entity.annotation.CritOnly.Type;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.types.Money;

import java.math.BigDecimal;
import java.util.Date;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.error.Result.failure;

/**
 * This is a test entity, which is currently used for testing of classes {@link Money} and {@link HibernateValueMatcher}.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@DescTitle("Description")
@MapEntityTo("MONEY_CLASS_TABLE")
@CompanionObject(IEntityWithMoney.class)
public class EntityWithMoney extends AbstractEntity<String> {

    @IsProperty
    @MapTo("MONEY")
    private Money money;
    
    @IsProperty
    @MapTo("DATE_TIME")
    private Date dateTimeProperty;

    @IsProperty
    @Calculated
    private BigDecimal calculatedProperty;
    protected static final ExpressionModel calculatedProperty_ = expr().prop("money.amount").add().prop("money.amount").model();

    @IsProperty(assignBeforeSave = true)
    @MapTo("TRANS_DATE_TIME")
    private Date transDate;
    
    @IsProperty
    @CritOnly(Type.SINGLE)
    @Title("Required Crit Only")
    @Required
    private Integer requiredCritOnly;

    @IsProperty(length = 5)
    @MapTo
    private String shortComment;

    protected EntityWithMoney() {}

    public EntityWithMoney(final String key, final String desc, final Money money) {
        super(null, key, desc);
        setMoney(money);
    }

    @Override
    @Observable
    public EntityWithMoney setDesc(String desc) {
        super.setDesc(desc);
        return this;
    }
    
    @Observable
    public EntityWithMoney setMoney(final Money money) {
        if (money == null) {
            throw failure("money should not be null");
        }
        this.money = money;
        return this;
    }

    public Money getMoney() {
        return money;
    }

    public BigDecimal getCalculatedProperty() {
        return calculatedProperty;
    }

    @Observable
    public EntityWithMoney setCalculatedProperty(final BigDecimal calculatedProperty) {
        this.calculatedProperty = calculatedProperty;
        return this;
    }

    public Date getDateTimeProperty() {
        return dateTimeProperty;
    }

    @Observable
    public EntityWithMoney setDateTimeProperty(final Date dateTime) {
        this.dateTimeProperty = dateTime;
        return this;
    }

    public Date getTransDate() {
        return transDate;
    }

    @Observable
    public EntityWithMoney setTransDate(final Date transDate) {
        this.transDate = transDate;
        return this;
    }
    
    @Observable
    public EntityWithMoney setRequiredCritOnly(final Integer requiredCritOnly) {
        this.requiredCritOnly = requiredCritOnly;
        return this;
    }

    public Integer getRequiredCritOnly() {
        return requiredCritOnly;
    }
    
    @Observable
    public EntityWithMoney setShortComment(final String shortComment) {
        this.shortComment = shortComment;
        return this;
    }

    public String getShortComment() {
        return shortComment;
    }
}
