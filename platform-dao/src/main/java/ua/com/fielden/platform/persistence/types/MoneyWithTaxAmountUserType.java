package ua.com.fielden.platform.persistence.types;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
import java.util.Map;

import org.hibernate.Hibernate;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;

import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.IMoneyWithTaxAmountUserType;

/**
 * Hibernate type for storing tax sensitive instances of type {@link Money}. This type expects that {@link Money} is mapped into three columns -- one for storing full amount,
 * second for storing tax amount and thirds for currency
 * 
 * @author 01es
 */
public class MoneyWithTaxAmountUserType extends AbstractCompositeUserType implements IMoneyWithTaxAmountUserType {

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
        final BigDecimal taxAmount = resultSet.getBigDecimal(names[1]);
        final Currency currency = Currency.getInstance(resultSet.getString(names[2]));
        return new Money(amount, taxAmount, currency);
    }

    @Override
    public Object instantiate(final Map<String, Object> arguments) {
        if (allArgumentsAreNull(arguments)) {
            return null;
        }
        return new Money((BigDecimal) arguments.get("amount"), (BigDecimal) arguments.get("taxAmount"), (Currency) arguments.get("currency"));
    }

    public void nullSafeSet(final PreparedStatement statement, final Object value, final int index, final SessionImplementor session) throws SQLException {
        if (value == null) {
            statement.setNull(index, Hibernate.BIG_DECIMAL.sqlType());
            statement.setNull(index + 1, Hibernate.BIG_DECIMAL.sqlType());
            statement.setNull(index + 2, Hibernate.CURRENCY.sqlType());
        } else {
            final Money amount = (Money) value;
            final String currencyCode = amount.getCurrency().getCurrencyCode();
            statement.setBigDecimal(index, amount.getAmount());
            statement.setBigDecimal(index + 1, amount.getTaxAmount());
            statement.setString(index + 2, currencyCode);
        }
    }

    public String[] getPropertyNames() {
        return new String[] { "amount", "taxAmount", "currency" };
    }

    public Type[] getPropertyTypes() {
        return new Type[] { Hibernate.BIG_DECIMAL, Hibernate.BIG_DECIMAL, Hibernate.CURRENCY };
    }

    public Object getPropertyValue(final Object component, final int property) {
        final Money monetaryAmount = (Money) component;
        if (property == 0) {
            return monetaryAmount.getAmount();
        } else if (property == 1) {
            return monetaryAmount.getTaxAmount();
        } else {
            return monetaryAmount.getCurrency();
        }
    }
}