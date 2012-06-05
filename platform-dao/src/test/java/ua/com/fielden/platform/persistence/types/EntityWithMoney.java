package ua.com.fielden.platform.persistence.types;

import java.math.BigDecimal;
import java.util.Date;

import ua.com.fielden.platform.basic.autocompleter.HibernateValueMatcher;
import ua.com.fielden.platform.dao.EntityWithMoneyDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.Calculated;
import ua.com.fielden.platform.entity.annotation.PersistedType;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.annotation.TransactionDate;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.IMoneyUserType;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;

/**
 * This is a test entity, which is currently used for testing of classes {@link Money} and {@link HibernateValueMatcher}.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@DescTitle("Description")
@MapEntityTo("MONEY_CLASS_TABLE")
@DefaultController(EntityWithMoneyDao.class)
public class EntityWithMoney extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo("MONEY") @PersistedType(userType = IMoneyUserType.class)
    private Money money;
    @IsProperty
    @MapTo("DATE_TIME")
    private Date dateTimeProperty;

    private static final ExpressionModel calculatedProperty_ = expr().prop("money.amount").add().prop("money.amount").model();
    @IsProperty @Calculated
    private BigDecimal calculatedProperty;

    @IsProperty @TransactionDate
    @MapTo("TRANS_DATE_TIME")
    private Date transDate;

    protected EntityWithMoney() {
    }

    public EntityWithMoney(final String key, final String desc, final Money money) {
	super(null, key, desc);
	setMoney(money);
    }

    @Observable
    public EntityWithMoney setMoney(final Money money) {
	if (money == null) {
	    throw new Result(this, new IllegalArgumentException("money should not be null"));
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
}