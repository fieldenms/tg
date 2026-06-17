package ua.com.fielden.platform.persistence.types;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.CurrencyType;
import org.hibernate.type.Type;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.markers.IMoneyType;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
import java.util.Map;

import static ua.com.fielden.platform.types.Money.AMOUNT;
import static ua.com.fielden.platform.types.Money.CURRENCY;

/**
 * Class that helps Hibernate to map {@link Money} class into database. <br>
 * Note : copied from auction.persistence.MonetaryAmountCompositeUserType class <br>
 * Note : {@link Money} class instance is mapped into 2 columns in the database : one of NUMERIC type (amount), another of VARCHAR(or LONGVARCHAR) type (currency).
 *
 * @author Yura
 */
public class MoneyType extends AbstractCompositeUserType implements IMoneyType {

    public static final MoneyType INSTANCE = new MoneyType();
    
    @Override
    public Class<Money> returnedClass() {
        return Money.class;
    }

    @Override
    public Object nullSafeGet(final ResultSet resultSet, final String[] names, final SharedSessionContractImplementor session, final Object owner) throws SQLException {
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
        final var amount = (BigDecimal) arguments.get(AMOUNT);
        if (amount == null) {
            return null;
        }
        final var currency = (Currency) arguments.get(CURRENCY);
        return new Money(amount, currency);
    }

    @Override
    public void nullSafeSet(final PreparedStatement statement, final Object value, final int index, final SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            
            statement.setNull(index, BigDecimalType.INSTANCE.sqlType());
            statement.setNull(index + 1, CurrencyType.INSTANCE.sqlType());
        } else {
            final Money amount = (Money) value;
            final String currencyCode = amount.getCurrency().getCurrencyCode();
            statement.setBigDecimal(index, amount.getAmount());
            statement.setString(index + 1, currencyCode);
        }
    }

    @Override
    public String[] getPropertyNames() {
        return new String[] { AMOUNT, CURRENCY };
    }

    @Override
    public Type[] getPropertyTypes() {
        return new Type[] { BigDecimalType.INSTANCE, CurrencyType.INSTANCE };
    }

    @Override
    public Object getPropertyValue(final Object component, final int property) {
        final Money monetaryAmount = (Money) component;
        if (property == 0) {
            return monetaryAmount.getAmount();
        } else {
            return monetaryAmount.getCurrency();
        }
    }
}
