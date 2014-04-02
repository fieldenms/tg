package ua.com.fielden.platform.persistence.types;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.Hibernate;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.Type;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;

public class UnionEntityType extends AbstractCompositeUserType {

    public Class<AbstractUnionEntity> returnedClass() {
        return AbstractUnionEntity.class;
    }

    public Object nullSafeGet(final ResultSet resultSet, final String[] names, final SessionImplementor session, final Object owner) throws SQLException {
        //	/*
        //	 *  It is very important to call resultSet.getXXX before checking resultSet.wasNull(). Please refer {@link ResultSet#wasNull()} for more details.
        //	 */
        //	final BigDecimal amount = resultSet.getBigDecimal(names[0]);
        //	if (resultSet.wasNull()) {
        //	    return null;
        //	}
        //	final BigDecimal taxAmount = resultSet.getBigDecimal(names[1]);
        //	final Currency currency = Currency.getInstance(resultSet.getString(names[2]));
        //	return new Money(amount, taxAmount, currency);
        return null;
    }

    //    public Object instantiate(final Map<String, Object> arguments) {
    //	if (allArgumentsAreNull(arguments)) {
    //	    return null;
    //	}
    //	return new Money((BigDecimal) arguments.get("amount"), (BigDecimal) arguments.get("taxAmount"), (Currency) arguments.get("currency"));
    //    }

    public void nullSafeSet(final PreparedStatement statement, final Object value, final int index, final SessionImplementor session) throws SQLException {
        if (value == null) {
            statement.setNull(index, Hibernate.STRING.sqlType());
            statement.setNull(index + 1, Hibernate.LONG.sqlType());
        } else {
            final AbstractUnionEntity unionEntity = (AbstractUnionEntity) value;
            //	    System.out.println("=================================================== " + unionEntity.activePropertyName());
            //	    System.out.println("=================================================== " + unionEntity.get(unionEntity.activePropertyName()));
            statement.setString(index, unionEntity.activePropertyName());//unionEntity.getKey());
            statement.setLong(index + 1, ((AbstractEntity) unionEntity.get(unionEntity.activePropertyName())).getId());//unionEntity.getId());
        }
    }

    public String[] getPropertyNames() {
        return new String[] { "prop", "value" };
    }

    public Type[] getPropertyTypes() {
        return new Type[] { Hibernate.STRING, Hibernate.LONG };
    }

    public Object getPropertyValue(final Object component, final int property) {
        final AbstractUnionEntity unionEntity = (AbstractUnionEntity) component;
        if (property == 0) {
            return unionEntity.getKey();
        } else {
            return unionEntity.getId();
        }
    }
}