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

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;

public class EnumUserType<E extends Enum<E>> implements UserType, IUserTypeInstantiate {
    private Class<E> clazz = null;

    protected EnumUserType(final Class<E> c) {
        this.clazz = c;
    }

    private static final int[] SQL_TYPES = { Types.VARCHAR };

    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @SuppressWarnings("unchecked")
    public Class returnedClass() {
        return clazz;
    }

    public Object nullSafeGet(final ResultSet resultSet, final String[] names, final Object owner) throws HibernateException, SQLException {
        final String name = resultSet.getString(names[0]);
        E result = null;
        if (!resultSet.wasNull()) {
            result = Enum.valueOf(clazz, name);
        }
        return result;
    }

    public Object instantiate(final Object argument, final EntityFactory factory) {
        return argument == null ? null : Enum.valueOf(clazz, argument.toString() /*(String) argument*/);
    }

    @SuppressWarnings("unchecked")
    public void nullSafeSet(final PreparedStatement preparedStatement, final Object value, final int index) throws HibernateException, SQLException {
        if (null == value) {
            preparedStatement.setNull(index, Types.VARCHAR);
        } else {
            preparedStatement.setString(index, ((Enum) value).name());
        }
    }

    public Object deepCopy(final Object value) throws HibernateException {
        return value;
    }

    public boolean isMutable() {
        return false;
    }

    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return cached;
    }

    public Serializable disassemble(final Object value) throws HibernateException {
        return (Serializable) value;
    }

    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return original;
    }

    public int hashCode(final Object x) throws HibernateException {
        return x.hashCode();
    }

    public boolean equals(final Object x, final Object y) throws HibernateException {
        if (x == y) {
            return true;
        }
        if (null == x || null == y) {
            return false;
        }
        return x.equals(y);
    }
}