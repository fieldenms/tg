package ua.com.fielden.platform.types;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StringNVarcharType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.persistence.types.exceptions.UserTypeException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Custom Hibernate type for {@link RichText}, designed specifically for PostgreSQL.
 * <p>
 * Users of this class should not instantiate it directly, but use {@link RichTextType#getInstance(DbVersion)}.
 * The constructor is made public to satisfy the requirements for Hibernate custom types.
 */
public final class RichTextPostgresqlType extends RichTextType {

    static final RichTextPostgresqlType INSTANCE = new RichTextPostgresqlType();

    /**
     * <b>Do not use this contructor directly</b>! Use {@link RichTextType#getInstance(DbVersion)}.
     */
    public RichTextPostgresqlType() {}

    /**
     * Uses {@link ResultSet#getString(int)} because {@link ResultSet#getNString(int)} is unsupported by the PostgreSQL JDBC driver.
     */
    @Override
    public Object nullSafeGet(
            final ResultSet resultSet,
            final String[] names,
            final SharedSessionContractImplementor session,
            final Object owner)
            throws SQLException
    {
        final String formattedText = resultSet.getString(names[0]);
        if (resultSet.wasNull()) {
            return null;
        }
        final String coreText = resultSet.getString(names[1]);
        if (resultSet.wasNull()) {
            throw new UserTypeException("Core text is null when formatted text is present. Formatted text:\n%s".formatted(formattedText));
        }
        return new RichText.Persisted(formattedText, coreText);
    }

    /**
     * Uses {@link PreparedStatement#setString(int, String)} because {@link PreparedStatement#setNString(int, String)} is unsupported by the PostgreSQL JDBC driver.
     */
    @Override
    public void nullSafeSet(
            final PreparedStatement statement,
            final Object value, final int index,
            final SharedSessionContractImplementor session)
            throws SQLException
    {
        if (value == null) {
            statement.setNull(index, StringType.INSTANCE.sqlType());
            statement.setNull(index + 1, StringType.INSTANCE.sqlType());
        } else {
            final var richText = (RichText) value;
            statement.setString(index, richText.formattedText());
            statement.setString(index + 1, richText.coreText());
        }
    }

    /**
     * Uses {@link StringType} instead of {@link StringNVarcharType} because NVARCHAR is unsupported by PostgreSQL.
     */
    @Override
    public Type[] getPropertyTypes() {
        return new Type[] { StringType.INSTANCE, StringType.INSTANCE };
    }

}
