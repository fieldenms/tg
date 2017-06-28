package ua.com.fielden.platform.persistence.types;

import static java.lang.String.*;
import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import ua.com.fielden.platform.entity.exceptions.EntityException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.types.markers.IPropertyDescriptorType;

/**
 * Class that helps Hibernate to map {@link PropertyDescriptor} class into the database.
 *
 * @author TG Team
 */
public class PropertyDescriptorType implements UserType, IPropertyDescriptorType {
    private static final Logger LOGGER = Logger.getLogger(PropertyDescriptorType.class);
    
    private static final int[] SQL_TYPES = { Types.VARCHAR };
    
    @Override
    public int[] sqlTypes() {
        return SQL_TYPES;
    }

    @Override
    public Class<?> returnedClass() {
        return PropertyDescriptor.class;
    }

    @Override
    public Object nullSafeGet(final ResultSet resultSet, final String[] names, final Object owner) throws HibernateException, SQLException {
        final String propertyDescriptor = resultSet.getString(names[0]);
        Object result = null;
        if (!resultSet.wasNull()) {
            try {
                result = PropertyDescriptor.fromString(propertyDescriptor, Optional.empty()).beginInitialising();
            } catch (final Exception ex) {
                LOGGER.fatal(ex);
                throw new HibernateException(format("Could not restore [%s] due to: %s", propertyDescriptor, ex.getMessage()));
            }
        }
        return result;
    }

    @Override
    public Object instantiate(final Object argument, final EntityFactory factory) {
        if (argument == null) {
            return null;
        }

        try {
            return PropertyDescriptor.fromString((String) argument, Optional.of(factory)).beginInitialising();
        } catch (final Exception ex) {
            LOGGER.fatal(ex);
            throw new EntityException(format("Could not instantiate instance of [%s] with value [%s] due to: %s", PropertyDescriptor.class.getName(), argument, ex.getMessage()));
        }
    }

    @Override
    public void nullSafeSet(final PreparedStatement preparedStatement, final Object value, final int index) throws HibernateException, SQLException {
        if (null == value) {
            preparedStatement.setNull(index, Types.VARCHAR);
        } else {
            preparedStatement.setString(index, value.toString());
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