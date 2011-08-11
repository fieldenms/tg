package ua.com.fielden.platform.persistence.types;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;

import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.ISimplyMoneyWithTaxAmountType;
import static java.util.Currency.getInstance;
import static org.hibernate.Hibernate.BIG_DECIMAL;

/**
 * Hibernate type for storing a simplified tax sensitive instances of type {@link Money} not requiring currency to be persisted.
 * This type expects that {@link Money} is mapped into two columns -- one for storing full amount and second for storing tax amount.
 *
 * @author 01es
 */
public class SimplyMoneyWithTaxAmountType implements CompositeUserType, ISimplyMoneyWithTaxAmountType {

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
	final BigDecimal taxAmount = resultSet.getBigDecimal(names[1]);
	return new Money(amount, taxAmount, getInstance(Locale.getDefault()));
    }

    @Override
    public Object instantiate(final Map<String, Object> arguments) {
	return new Money((BigDecimal) arguments.get("amount"), (BigDecimal) arguments.get("taxAmount"), getInstance(Locale.getDefault()));
    }

    public void nullSafeSet(final PreparedStatement statement, final Object value, final int index, final SessionImplementor session) throws SQLException {
	if (value == null) {
	    statement.setNull(index, BIG_DECIMAL.sqlType());
	    statement.setNull(index + 1,  BIG_DECIMAL.sqlType());
	} else {
	    final Money amount = (Money) value;
	    statement.setBigDecimal(index, amount.getAmount());
	    statement.setBigDecimal(index + 1, amount.getTaxAmount());
	}
    }

    public String[] getPropertyNames() {
	return new String[] { "amount", "taxAmount" };
    }

    public Type[] getPropertyTypes() {
	return new Type[] { BIG_DECIMAL, BIG_DECIMAL };
    }

    public Object getPropertyValue(final Object component, final int property) {
	final Money monetaryAmount = (Money) component;
	if (property == 0) {
	    return monetaryAmount.getAmount();
	} else {
	    return monetaryAmount.getTaxAmount();
	}
    }

    public void setPropertyValue(final Object component, final int property, final Object value) {
	throw new UnsupportedOperationException("Monetary type is immutable");
    }
}
