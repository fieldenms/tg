package ua.com.fielden.platform.persistence.types;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import ua.com.fielden.platform.persistence.types.exceptions.UserTypeException;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.types.markers.IRichTextType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import static ua.com.fielden.platform.types.RichText._coreText;
import static ua.com.fielden.platform.types.RichText._formattedText;

public final class RichTextType extends AbstractCompositeUserType implements IRichTextType {

    public static final RichTextType INSTANCE = new RichTextType();
    
    @Override
    public Class<RichText> returnedClass() {
        return RichText.class;
    }

    @Override
    public Object nullSafeGet(final ResultSet resultSet,
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
        return new RichText(formattedText, coreText);
    }

    @Override
    public Object instantiate(final Map<String, Object> arguments) {
        if (allArgumentsAreNull(arguments)) {
            return null;
        }
        return new RichText((String) arguments.get(_formattedText),
                            (String) arguments.get(_coreText));
    }

    @Override
    public void nullSafeSet(final PreparedStatement statement,
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

    @Override
    public String[] getPropertyNames() {
        return new String[] { _formattedText, _coreText };
    }

    @Override
    public Type[] getPropertyTypes() {
        return new Type[] { StringType.INSTANCE, StringType.INSTANCE };
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
