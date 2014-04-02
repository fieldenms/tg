/**
 *
 */
package ua.com.fielden.platform.persistence.types;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;

import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.ISimplyMoneyWithTaxAndExTaxAmountType;
import static java.util.Currency.getInstance;
import static org.hibernate.Hibernate.BIG_DECIMAL;

/**
 * Hibernate user type for storing {@link Money} instances with taxes (persisting/retrieving problems may occur, if tax is not specified in {@link Money} instance). This class
 * stores two values to two database columns : amount excluding tax and tax itself.
 * 
 * @author Yura
 */
public class SimplyMoneyWithTaxAndExTaxAmountType extends AbstractCompositeUserType implements ISimplyMoneyWithTaxAndExTaxAmountType {

    @Override
    public String[] getPropertyNames() {
        return new String[] { "exTaxAmount", "taxAmount" };
    }

    @Override
    public Type[] getPropertyTypes() {
        return new Type[] { BIG_DECIMAL, BIG_DECIMAL };
    }

    @Override
    public Object getPropertyValue(final Object value, final int property) throws HibernateException {
        final Money amount = (Money) value;
        if (property == 0) {
            return amount.getExTaxAmount();
        } else {
            return amount.getTaxAmount();
        }
    }

    @Override
    public Object nullSafeGet(final ResultSet resultSet, final String[] names, final SessionImplementor implementor, final Object owner) throws SQLException {
        /*
         *  It is very important to call resultSet.getXXX before checking resultSet.wasNull(). Please refer {@link ResultSet#wasNull()} for more details.
         */
        final BigDecimal exTaxAmount = resultSet.getBigDecimal(names[0]);
        if (resultSet.wasNull()) {
            return null;
        }
        final BigDecimal taxAmount = resultSet.getBigDecimal(names[1]);
        return new Money(exTaxAmount.add(taxAmount), taxAmount, getInstance(Locale.getDefault()));
    }

    @Override
    public Object instantiate(final Map<String, Object> arguments) {
        if (allArgumentsAreNull(arguments)) {
            return null;
        }
        final BigDecimal taxAmount = (BigDecimal) arguments.get("taxAmount");
        final BigDecimal exTaxAmount = (BigDecimal) arguments.get("exTaxAmount");
        return new Money(exTaxAmount.add(taxAmount), taxAmount, getInstance(Locale.getDefault()));
    }

    @Override
    public void nullSafeSet(final PreparedStatement statement, final Object value, final int index, final SessionImplementor sessionImplementor) throws SQLException {
        if (value == null) {
            statement.setNull(index, BIG_DECIMAL.sqlType());
            statement.setNull(index + 1, BIG_DECIMAL.sqlType());
        } else {
            final Money amount = (Money) value;
            statement.setBigDecimal(index, amount.getExTaxAmount());
            statement.setBigDecimal(index + 1, amount.getTaxAmount());
        }
    }

    @Override
    public Class<Money> returnedClass() {
        return Money.class;
    }
}