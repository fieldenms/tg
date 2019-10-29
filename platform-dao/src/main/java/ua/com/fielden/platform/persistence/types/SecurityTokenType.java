package ua.com.fielden.platform.persistence.types;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.types.markers.ISecurityTokenType;

/**
 * Class that helps Hibernate to map {@link ISecurityToken} class into database.
 * 
 * @author TG Team
 */
public class SecurityTokenType implements UserType, ISecurityTokenType {

    private static final int[] SQL_TYPES = { Types.VARCHAR };

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Class<?> returnedClass() {
        return ISecurityToken.class;
    }

    @Override
    public Object nullSafeGet(final ResultSet resultSet, final String[] names, final SharedSessionContractImplementor session, final Object owner) throws SQLException {
        final String name = resultSet.getString(names[0]);
        Object result = null;
        if (!resultSet.wasNull()) {
            try {
                result = Class.forName(name);
            } catch (final ClassNotFoundException e) {
                throw new HibernateException("Security token for value '" + name + "' could not be found");
            }
        }
        return result;
    }

    @Override
    public Object instantiate(final Object argument, final EntityFactory factory) {
        try {
            return Class.forName((String) argument);
        } catch (final Exception e) {
            throw new RuntimeException("Could not instantiate instance of '" + SecurityTokenType.class.getName() + " with value [" + argument + "] due to: " + e.getMessage());
        }
    }

    @Override
    public void nullSafeSet(final PreparedStatement preparedStatement, final Object value, final int index, final SharedSessionContractImplementor session) throws SQLException {
        if (null == value) {
            preparedStatement.setNull(index, Types.VARCHAR);
        } else {
            preparedStatement.setString(index, value instanceof String ? (String) value : ((Class<?>) value).getName());
        }
    }

    @Override
    public Object deepCopy(final Object value) {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object assemble(final Serializable cached, final Object owner) {
        return cached;
    }

    @Override
    public Serializable disassemble(final Object value) {
        return (Serializable) value;
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) {
        return original;
    }

    @Override
    public int hashCode(final Object x) {
        return x.hashCode();
    }

    @Override
    public boolean equals(final Object x, final Object y) {
        if (x == y) {
            return true;
        }
        if (null == x || null == y) {
            return false;
        }
        return x.equals(y);
    }
}