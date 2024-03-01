package ua.com.fielden.platform.eql.antlr;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.N;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.Y;

abstract class AbstractEqlVisitor<T> extends EQLBaseVisitor<T> {

    protected final QueryModelToStage1Transformer transformer;

    AbstractEqlVisitor(QueryModelToStage1Transformer transformer) {
        this.transformer = transformer;
    }


    protected Object getParamValue(final String paramName) {
        final Object paramValue = transformer.getParamValue(paramName);
        if (paramValue != null) {
            return preprocessValue(paramValue);
        } else {
            //TODO throw exception for all contexts except YIELD (as NULL operands can't be used in SQL conditions, groupings, etc).
            return null;
        }
    }

    protected static Object preprocessValue(final Object value) {
        if (value != null && (value.getClass().isArray() || value instanceof Collection<?>)) {
            final Iterable<?> iterable = value.getClass().isArray() ? Arrays.asList((Object[]) value) : (Collection<?>) value;
            final List<Object> values = new ArrayList<>();
            for (final Object object : iterable) {
                final Object furtherPreprocessed = preprocessValue(object);
                if (furtherPreprocessed instanceof List) {
                    values.addAll((List<?>) furtherPreprocessed);
                } else {
                    values.add(furtherPreprocessed);
                }
            }
            return values;
        } else {
            return convertValue(value);
        }
    }

    /** Ensures that values of boolean types are converted properly. */
    protected static Object convertValue(final Object value) {
        if (value instanceof Boolean) {
            return (boolean) value ? Y : N;
        }
        return value;
    }

    // a generic return type is needed to be able to use this method in switch expressions
    protected static <T> T unexpectedToken(final Token token) throws EqlParseException {
        throw new EqlParseException("Unexpected token: %s".formatted(token.getText()));
    }

}
