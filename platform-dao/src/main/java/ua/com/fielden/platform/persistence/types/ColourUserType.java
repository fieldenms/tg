package ua.com.fielden.platform.persistence.types;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.types.markers.IColourType;

public class ColourUserType implements UserType, IColourType {

	private static final int[] SQL_TYPES = { Types.VARCHAR };

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	@Override
	public Class<?> returnedClass() {
		return IColourType.class;
	}

	@Override
	public Object instantiate(final Object argument, final EntityFactory factory) {
		try {
			return Class.forName((String) argument);
		} catch (final Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not instantiate instance of '"
					+ ColourUserType.class.getName() + " with value ["
					+ argument + "] due to: " + e.getMessage());
		}
	}

	@Override
	public Object nullSafeGet(final ResultSet resultSet, final String[] names,
			final Object owner) throws HibernateException, SQLException {
		final String name = resultSet.getString(names[0]);
		Object result = null;
		if (!resultSet.wasNull()) {
			try {
				result = Class.forName(name);
			} catch (final ClassNotFoundException e) {
				e.printStackTrace();
				throw new HibernateException("Colour for value '" + name
						+ "' could not be found");
			}
		}
		return result;
	}

	@Override
	public void nullSafeSet(final PreparedStatement preparedStatement,
			final Object value, final int index) throws HibernateException,
			SQLException {
		if (null == value) {
			preparedStatement.setNull(index, Types.VARCHAR);
		} else {
			preparedStatement.setString(
					index,
					value instanceof String ? (String) value : ((Class) value)
							.getName());
		}
	}

	@Override
	public Object assemble(final Serializable cached, final Object owner)
			throws HibernateException {
		return cached;
	}

	@Override
	public Object deepCopy(final Object value) throws HibernateException {
		return value;
	}

	@Override
	public Serializable disassemble(final Object value)
			throws HibernateException {
		return (Serializable) value;
	}

	@Override
	public boolean equals(final Object x, final Object y)
			throws HibernateException {
		if (x == y) {
			return true;
		}
		if (null == x || null == y) {
			return false;
		}
		return x.equals(y);
	}

	@Override
	public int hashCode(final Object x) throws HibernateException {
		return x.hashCode();
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Object replace(final Object original, final Object target,
			final Object owner) throws HibernateException {
		return original;
	}

}
