package ua.com.fielden.platform.types;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StringNVarcharType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.persistence.types.AbstractCompositeUserType;
import ua.com.fielden.platform.persistence.types.exceptions.UserTypeException;
import ua.com.fielden.platform.types.markers.IRichTextType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static ua.com.fielden.platform.types.RichText._coreText;
import static ua.com.fielden.platform.types.RichText._formattedText;

/**
 * Hibernate type mapping for composite type {@link RichText}.
 * <p>
 * Users of this class should not instantiate it directly, but use {@link #getInstance(DbVersion)}.
 * The constructor is made public to satisfy the requirements for Hibernate custom types.
 * <p>
 * {@link RichTextType} has subtypes for those databases whose JDBC drivers require special handling:
 * <ul>
 *   <li> PostgreSQL - {@link RichTextPostgresqlType}.
 * </ul>
 * For all other databases {@link RichTextType} is used.
 * <p>
 * <b>Implementation remark:</b>
 * <i>
 *  The platform guarantees that all RichText values are sanitised prior to being persisted.
 *  Therefore, we avoid sanitisation when instantiating RichText from persisted values.
 *  Note that this is not simply a performance optimisation, but also a way to preserve the integrity of persisted values.
 *  It is not know what might happen if we sanitise an already sanitised text and perform extraction of core text again,
 *  which would preserve data integrity only if both the sanitiser and core text extractor are idempotent
 *  (we use 3rd party dependencies for both, so there is no guarantee they are and would stay idempotent).
 *  In any case, {@link RichText} is designed to prohibit instantiation without sanitisation.
 *  The only way to persist a dangerous {@link RichText} value is to write to the DB directly, which would indicate a compromise of a much greater scale.
 *  </i>
 */
public sealed class RichTextType extends AbstractCompositeUserType implements IRichTextType
        permits RichTextPostgresqlType
{

    private static final RichTextType INSTANCE = new RichTextType();

    /**
     * Returns an instance of {@link RichTextType} that is supported for the specified database.
     * <p>
     * See the documentation of {@link RichTextType} for an overview of supported databases.
     */
    public static RichTextType getInstance(final DbVersion dbVersion) {
        return switch (dbVersion) {
            case POSTGRESQL -> RichTextPostgresqlType.INSTANCE;
            default -> INSTANCE;
        };
    }

    /**
     * <b>Do not use this contructor directly</b>! Use {@link #getInstance(DbVersion)}.
     */
    public RichTextType() {}

    @Override
    public Class<RichText> returnedClass() {
        return RichText.class;
    }

    @Override
    public Object nullSafeGet(
            final ResultSet resultSet,
            final String[] names,
            final SharedSessionContractImplementor session,
            final Object owner)
            throws SQLException
    {
        final String formattedText = resultSet.getNString(names[0]);
        if (resultSet.wasNull()) {
            return null;
        }
        final String coreText = resultSet.getNString(names[1]);
        if (resultSet.wasNull()) {
            throw new UserTypeException("Core text is null when formatted text is present. Formatted text:\n%s".formatted(formattedText));
        }
        return new RichText.Persisted(formattedText, coreText);
    }

    @Override
    public Object instantiate(final Map<String, Object> arguments) {
        if (allArgumentsAreNull(arguments)) {
            return null;
        }
        return new RichText.Persisted((String) arguments.get(_formattedText),
                                      (String) arguments.get(_coreText));
    }

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
            statement.setNString(index, richText.formattedText());
            statement.setNString(index + 1, richText.coreText());
        }
    }

    @Override
    public String[] getPropertyNames() {
        return new String[] { _formattedText, _coreText };
    }

    @Override
    public Type[] getPropertyTypes() {
        return new Type[] { StringNVarcharType.INSTANCE, StringNVarcharType.INSTANCE };
    }

    @Override
    public Object getPropertyValue(final Object component, final int property) {
        final var richText = (RichText) component;
        if (property == 0) {
            return richText.formattedText();
        } else {
            return richText.coreText();
        }
    }

}
