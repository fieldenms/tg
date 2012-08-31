package ua.com.fielden.platform.persistence.types;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
import java.util.Locale;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;

import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.ISimpleMoneyType;

/**
 * Class that helps Hibernate to map {@link Money} class into database without the currency portion, which basically requires only one column for mapping properties of type Money.
 *
 * @author 01es
 */
public class SimpleMoneyType extends AbstractCompositeUserType implements ISimpleMoneyType {

    public Class<Money> returnedClass() {
	return Money.class;
    }

    public Object nullSafeGet(final ResultSet resultSet, final String[] names, final SessionImplementor session, final Object owner) throws SQLException {
	/*
	 *  It is very important to call resultSet.getXXX before checking resultSet.wasNull(). Please refer {@link ResultSet#wasNull()} for more details.
	 */
	final BigDecimal amount = resultSet.getBigDecimal(names[0]);
	if (resultSet.wasNull()) {
	    return null;
	}
	return new Money(amount);
    }

    @Override
    public Object instantiate(final Map<String, Object> arguments) {
	if (allArgumentsAreNull(arguments)) {
	    return null;
	}
	return new Money((BigDecimal) arguments.get("amount"), Currency.getInstance(Locale.getDefault()));
    }

    public void nullSafeSet(final PreparedStatement statement, final Object value, final int index, final SessionImplementor session) throws SQLException {
	if (value == null) {
	    statement.setNull(index, Hibernate.BIG_DECIMAL.sqlType());
	} else {
	    final Money amount = (Money) value;
	    statement.setBigDecimal(index, amount.getAmount());
	}
    }

    public String[] getPropertyNames() {
	return new String[] { "amount" };
    }

    public Type[] getPropertyTypes() {
	return new Type[] { Hibernate.BIG_DECIMAL };
    }

    public Object getPropertyValue(final Object component, final int property) {
	final Money monetaryAmount = (Money) component;
	return monetaryAmount.getAmount();
    }
}
