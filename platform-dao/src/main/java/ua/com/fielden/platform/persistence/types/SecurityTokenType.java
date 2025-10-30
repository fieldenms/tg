package ua.com.fielden.platform.persistence.types;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider;
import ua.com.fielden.platform.security.provider.ISecurityTokenProvider.MissingSecurityTokenPlaceholder;
import ua.com.fielden.platform.types.markers.ISecurityTokenType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

/// Class that helps Hibernate to map [ISecurityToken] class into database.
///
@Singleton
public class SecurityTokenType implements UserType, ISecurityTokenType {

    public static final SecurityTokenType INSTANCE = new SecurityTokenType();

    private static final int[] SQL_TYPES = { Types.VARCHAR };

    /// A security token provider is required to locate token types by name.
    /// It must be injected statically because Hibernate requires user types to have a public default constructor,
    /// implying that they may be constructed reflectively, which disables instance-level injection.
    ///
    @Inject
    private static ISecurityTokenProvider securityTokenProvider;

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
        final var maybeSecurityToken = securityTokenProvider.getTokenByName(name);
        return maybeSecurityToken.isPresent() ? maybeSecurityToken.get() : MissingSecurityTokenPlaceholder.class;
    }

    @Override
    public Object instantiate(final Object argument, final EntityFactory factory) {
        final var name = (String) argument;
        final var maybeSecurityToken = securityTokenProvider.getTokenByName(name);
        return maybeSecurityToken.isPresent() ? maybeSecurityToken.get() : MissingSecurityTokenPlaceholder.class;
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
