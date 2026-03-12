package ua.com.fielden.platform.persistence.types;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.persistence.types.exceptions.UserTypeException;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.markers.IHyperlinkType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static java.lang.String.format;
import static ua.com.fielden.platform.persistence.types.exceptions.UserTypeException.invalidPersistedRepresentation;

/**
 * This is a user type to assist Hibernated in mapping properties of type {@link Hyperlink}.
 *  
 * @author TG Team
 *
 */
public class HyperlinkType implements UserType, IHyperlinkType {

    public static final HyperlinkType INSTANCE = new HyperlinkType();
    
	private static final int[] SQL_TYPES = { Types.VARCHAR };

	@Override
	public int[] sqlTypes() {
		return SQL_TYPES;
	}

	@Override
	public Class<?> returnedClass() {
		return Hyperlink.class;
	}

	@Override
	public Hyperlink instantiate(final Object argument, final EntityFactory factory) {
        return switch (argument) {
            case String s -> {
                try {
                    yield new Hyperlink(s);
                } catch (final Exception ex) {
                    throw new UserTypeException(format("Hyperlink could not be instantiated from [%s].", s), ex);
                }
            }
            case null -> null;
            default -> throw invalidPersistedRepresentation("Hyperlink", argument);
        };
	}

	@Override
    public Object nullSafeGet(final ResultSet resultSet, final String[] names, final SharedSessionContractImplementor session, final Object owner) throws SQLException {
        final String name = resultSet.getString(names[0]);
        if (resultSet.wasNull()) {
            return null;
        }
        try {
            return new Hyperlink(name);
        } catch (final Exception ex) {
            throw new UserTypeException(format("Hyperlink could not be instantiated from [%s].", name), ex);
        }
    }

	@Override
	public void nullSafeSet(final PreparedStatement preparedStatement, final Object value, final int index, final SharedSessionContractImplementor session) throws SQLException {
		if (value == null) {
			preparedStatement.setNull(index, Types.VARCHAR);
		} else {
			preparedStatement.setString(index, value.toString());
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
