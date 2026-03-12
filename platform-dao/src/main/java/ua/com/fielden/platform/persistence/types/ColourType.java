package ua.com.fielden.platform.persistence.types;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.persistence.types.exceptions.UserTypeException;
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.markers.IColourType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static ua.com.fielden.platform.persistence.types.exceptions.UserTypeException.invalidPersistedRepresentation;

/**
 * This is a user type to assist Hibernated in mapping properties of type {@link Colour}.
 *  
 * @author TG Team
 *
 */
public class ColourType implements UserType, IColourType {

    public static final ColourType INSTANCE = new ColourType();
    
	private static final int[] SQL_TYPES = { Types.VARCHAR };

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	@Override
	public Class<?> returnedClass() {
		return Colour.class;
	}

	@Override
    public Colour instantiate(final Object argument, final EntityFactory factory) {
        return switch (argument) {
            case String s -> {
                try {
                    yield new Colour(s);
                } catch (final Exception ex) {
                    throw new UserTypeException("Colour could not be instantiated from [%s].".formatted(s), ex);
                }
            }
            case null -> null;
            default -> throw invalidPersistedRepresentation("Colour", argument);
        };
    }

	@Override
	public Object nullSafeGet(final ResultSet resultSet, final String[] names, final SharedSessionContractImplementor session, final Object owner) throws SQLException {
		final String name = resultSet.getString(names[0]);
        if (resultSet.wasNull()) {
            return null;
        }
        try {
            return new Colour(name);
        } catch (final Exception ex) {
            throw new UserTypeException(format("Colour could not be instantiated from [%s].", name), ex);
        }
    }

	@Override
    public void nullSafeSet(final PreparedStatement preparedStatement, final Object value, final int index, final SharedSessionContractImplementor session) throws SQLException {
        switch (value) {
            case Colour colour when isEmpty(colour.hashlessUppercasedColourValue) ->
                    preparedStatement.setNull(index, Types.VARCHAR);
            case null -> preparedStatement.setNull(index, Types.VARCHAR);
            default -> preparedStatement.setString(index, value.toString());
        }
    }

	@Override
	public Object assemble(final Serializable cached, final Object owner) {
		return cached;
	}

	@Override
	public Object deepCopy(final Object value) {
		return value;
	}

	@Override
	public Serializable disassemble(final Object value) {
		return (Serializable) value;
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

	@Override
	public int hashCode(final Object x) {
		return x.hashCode();
	}

	@Override
	public boolean isMutable() {
		return false;
	}

	@Override
	public Object replace(final Object original, final Object target, final Object owner) {
		return original;
	}
}
