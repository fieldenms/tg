package ua.com.fielden.platform.persistence.types;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.IMoneyUserType;

/**
 * Class that helps Hibernate to map {@link Money} class into database.
 * <br>Note : copied from auction.persistence.MonetaryAmountCompositeUserType class
 * <br>Note : {@link Money} class instance is mapped into 2 columns in the database : one of NUMERIC type (amount), another of VARCHAR(or LONGVARCHAR) type (currency).
 *
 * @author Yura
 */
public class MoneyUserType implements CompositeUserType, IMoneyUserType {

    public Class<Money> returnedClass() {
	return Money.class;
    }

    public boolean isMutable() {
	return false;
    }

    public Object deepCopy(final Object value) {
	return value;
    }

    public Serializable disassemble(final Object value, final SessionImplementor session) {
	return (Serializable) value;
    }

    public Object assemble(final Serializable cached, final SessionImplementor session, final Object owner) {
	return cached;
    }

    public Object replace(final Object original, final Object target, final SessionImplementor session, final Object owner) {
	return original;
    }

    public boolean equals(final Object x, final Object y) {
	if (x == y) {
	    return true;
	}
	if (x == null || y == null) {
	    return false;
	}
	return x.equals(y);
    }

    public int hashCode(final Object x) {
	return x.hashCode();
    }

    public Object nullSafeGet(final ResultSet resultSet, final String[] names, final SessionImplementor session, final Object owner) throws SQLException {
	/*
	 *  It is very important to call resultSet.getXXX before checking resultSet.wasNull(). Please refer {@link ResultSet#wasNull()} for more details.
	 */
	final BigDecimal amount = resultSet.getBigDecimal(names[0]);
	if (resultSet.wasNull()) {
	    return null;
	}
	final Currency currency = Currency.getInstance(resultSet.getString(names[1]));
	return new Money(amount, currency);
    }

    @Override
    public Object instantiate(final Map<String, Object> arguments) {
	return new Money((BigDecimal) arguments.get("amount"), (Currency) arguments.get("currency"));
    }

    public void nullSafeSet(final PreparedStatement statement, final Object value, final int index, final SessionImplementor session) throws SQLException {
	if (value == null) {
	    statement.setNull(index, Hibernate.BIG_DECIMAL.sqlType());
	    statement.setNull(index + 1, Hibernate.CURRENCY.sqlType());
	} else {
	    final Money amount = (Money) value;
	    final String currencyCode = amount.getCurrency().getCurrencyCode();
	    statement.setBigDecimal(index, amount.getAmount());
	    statement.setString(index + 1, currencyCode);
	}
    }

    public String[] getPropertyNames() {
	return new String[] { "amount", "currency" };
    }

    public Type[] getPropertyTypes() {
	return new Type[] { Hibernate.BIG_DECIMAL, Hibernate.CURRENCY };
    }

    public Object getPropertyValue(final Object component, final int property) {
	final Money monetaryAmount = (Money) component;
	if (property == 0) {
	    return monetaryAmount.getAmount();
	} else {
	    return monetaryAmount.getCurrency();
	}
    }

    public void setPropertyValue(final Object component, final int property, final Object value) {
	throw new UnsupportedOperationException("Monetary type is immutable");
    }
}
