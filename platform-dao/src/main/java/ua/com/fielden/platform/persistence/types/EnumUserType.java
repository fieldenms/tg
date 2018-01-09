package ua.com.fielden.platform.persistence.types;

/*
 * Created by Vincent at JBoss.
 * Adapted for FMS's purposes by 01es at FMS.ua
 * Pls note that properties of enum E (if such exist) cannot be used in HQL queries.
 * If this is required then CompositeUserType should be used instead of UserType.
 */
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.usertype.UserType;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;

public class EnumUserType<E extends Enum<E>> implements UserType, IUserTypeInstantiate {
    private Class<E> clazz = null;

    protected EnumUserType(final Class<E> c) {
        this.clazz = c;
    }

    private static final int[] SQL_TYPES = { Types.VARCHAR };

    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Class<?> returnedClass() {
        return clazz;
    }

    @Override
    public Object instantiate(final Object argument, final EntityFactory factory) {
        return argument == null ? null : Enum.valueOf(clazz, argument.toString() /*(String) argument*/);
    }

    @Override
    public Object nullSafeGet(final ResultSet resultSet, final String[] names, final SessionImplementor session, final Object owner) throws SQLException {
        final String name = resultSet.getString(names[0]);
        E result = null;
        if (!resultSet.wasNull()) {
            result = Enum.valueOf(clazz, name);
        }
        return result;
    }

    @Override
    public void nullSafeSet(final PreparedStatement preparedStatement, final Object value, final int index, final SessionImplementor session) throws SQLException {
        if (null == value) {
            preparedStatement.setNull(index, Types.VARCHAR);
        } else {
            preparedStatement.setString(index, ((Enum<?>) value).name());
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