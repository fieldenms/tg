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

import static ua.com.fielden.platform.types.RichText.*;

/**
 * Hibernate type mapping for component type {@link RichText}.
 * <p>
 * Users of this class should not instantiate it directly, but use {@link #getInstance(DbVersion)}.
 * The constructor is made public to satisfy the requirements for Hibernate custom types.
 * <p>
 * {@link RichTextType} has subtypes for the databases where JDBC drivers require special handling:
 * <ul>
 *   <li> PostgreSQL - {@link RichTextPostgresqlType}.
 * </ul>
 * For all other databases {@link RichTextType} is used.
 * <p>
 * <b>Implementation remark:</b>
 * <i>
 *  The platform guarantees that all `RichText` values are sanitised prior to being persisted.
 *  Therefore, we avoid sanitisation when instantiating RichText from persisted values.
 *  Note that this is not simply a performance optimisation, but also a way to preserve the integrity of persisted values.
 *  It is not know what might happen if we sanitise an already sanitised text and perform extraction of the core text again,
 *  which would preserve data integrity only if both the sanitiser and core text extractor are idempotent.
 *  A 3rd party library is used for both, and there is no guarantee they are and would stay idempotent.
 *  In any case, {@link RichText} is designed to prohibit instantiation without sanitisation.
 *  The only way to persist a dangerous {@link RichText} value is to write it to the DB directly, which would indicate a compromise of a much greater scale.
 *  </i>
 */
public sealed class RichTextType extends AbstractCompositeUserType implements IRichTextType
        permits RichTextPostgresqlType
{

    public static final String ERR_TEXT_IS_NULL = "%s text is null when formatted text is present. Formatted text:%n%s";

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
     * Defines a type for text values, which influences how text values are read from a result set and assigned to statement parameters.
     */
    private final Type textType;

    /**
     * <b>Do not use this constructor directly</b>! Use {@link #getInstance(DbVersion)}.
     */
    public RichTextType() {
        this.textType = StringNVarcharType.INSTANCE;
    }

    protected RichTextType(final Type textType) {
        this.textType = textType;
    }

    /**
     * Defines how text values are read from {@code rs}.
     *
     * @param rs
     * @param columnLabel
     * @return
     * @throws SQLException
     */
    protected String getText(final ResultSet rs, final String columnLabel) throws SQLException {
        if (textType instanceof StringNVarcharType) {
            return rs.getNString(columnLabel);
        }
        else {
            return rs.getString(columnLabel);
        }
    }

    /**
     * Defines how text parameters are assigned to {@code pst}.
     *
     * @param pst
     * @param parameterIndex
     * @param value
     * @throws SQLException
     */
    protected void setText(final PreparedStatement pst, final int parameterIndex, final String value) throws SQLException {
        if (textType instanceof StringNVarcharType) {
            pst.setNString(parameterIndex, value);
        }
        else {
            pst.setString(parameterIndex, value);;
        }
    }

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
        final String formattedText = getText(resultSet, names[0]);
        if (resultSet.wasNull()) {
            return null;
        }
        final String coreText = getText(resultSet, names[1]);
        if (resultSet.wasNull()) {
            throw new UserTypeException(ERR_TEXT_IS_NULL.formatted("Core", formattedText));
        }
        final String searchText = getText(resultSet, names[2]);
        if (!resultSet.wasNull()) {
            throw new UserTypeException("Search text should never be retrieved.");
        }
        return new RichText.Persisted(formattedText, coreText);
    }

    @Override
    public Object instantiate(final Map<String, Object> arguments) {
        if (allArgumentsAreNull(arguments)) {
            return null;
        }
        return new RichText.Persisted((String) arguments.get(FORMATTED_TEXT),
                                      (String) arguments.get(CORE_TEXT));
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
            statement.setNull(index + 2, StringType.INSTANCE.sqlType());
        } else {
            final var richText = (RichText) value;
            setText(statement, index, richText.formattedText());
            setText(statement,index + 1, richText.coreText());
            setText(statement,index + 2, RichText.makeSearchText(richText));
        }
    }

    @Override
    public String[] getPropertyNames() {
        return new String[] { FORMATTED_TEXT, CORE_TEXT, SEARCH_TEXT };
    }

    @Override
    public Type[] getPropertyTypes() {
        return new Type[] { textType, textType, textType };
    }

    @Override
    public Object getPropertyValue(final Object component, final int property) {
        final var richText = (RichText) component;
        return switch (property) {
            case 0 -> richText.formattedText();
            case 1 -> richText.coreText();
            default -> null;
        };
    }

}
