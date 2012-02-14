package ua.com.fielden.platform.persistence.types;

import java.util.Date;

import ua.com.fielden.platform.basic.autocompleter.HibernateValueMatcher;
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
import ua.com.fielden.platform.entity.annotation.TransactionDate;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.IMoneyUserType;

/**
 * This is a test entity, which is currently used for testing of classes {@link Money} and {@link HibernateValueMatcher}.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@DescTitle("Description")
@MapEntityTo("MONEY_CLASS_TABLE")
public class EntityWithMoney extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    @IsProperty
    @MapTo(value = "MONEY", userType = IMoneyUserType.class)
    private Money money;
    @IsProperty
    @MapTo("DATE_TIME")
    private Date dateTimeProperty;
    @IsProperty @Calculated(contextPath = "", contextualExpression = "", attribute = CalculatedPropertyAttribute.NO_ATTR, origination = "", category = CalculatedPropertyCategory.EXPRESSION)
    private String calculatedProperty;

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
    public void setMoney(final Money money) {
	if (money == null) {
	    throw new IllegalArgumentException("money should not be null");
	}
	this.money = money;
    }

    public Money getMoney() {
	return money;
    }

    public String getCalculatedProperty() {
        return calculatedProperty;
    }

    @Observable
    public void setCalculatedProperty(final String calculatedProperty) {
        this.calculatedProperty = calculatedProperty;
    }

    public Date getDateTimeProperty() {
        return dateTimeProperty;
    }

    @Observable
    public void setDateTimeProperty(final Date dateTime) {
        this.dateTimeProperty = dateTime;
    }

    public Date getTransDate() {
        return transDate;
    }
    @Observable
    public void setTransDate(final Date transDate) {
        this.transDate = transDate;
    }
}
