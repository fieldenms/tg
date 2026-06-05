package ua.com.fielden.platform.eql.antlr;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.eql.antlr.exceptions.EqlCompilationException;
import ua.com.fielden.platform.eql.antlr.exceptions.EqlSyntaxException;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.persistence.HibernateConstants;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static ua.com.fielden.platform.eql.stage1.operands.Value1.nullValue;
import static ua.com.fielden.platform.eql.stage1.operands.Value1.value;
import static ua.com.fielden.platform.persistence.HibernateConstants.N;
import static ua.com.fielden.platform.persistence.HibernateConstants.Y;

abstract class AbstractEqlVisitor<T> extends StrictEQLBaseVisitor<T> {

    protected final QueryModelToStage1Transformer transformer;

    AbstractEqlVisitor(final QueryModelToStage1Transformer transformer) {
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

    protected Object requireParamValue(final String paramName) {
        if (!transformer.hasParam(paramName)) {
            throw new EqlCompilationException("The query is missing required parameter [%s].".formatted(paramName));
        }
        return getParamValue(paramName);
    }

    /**
     * Returns operands that should be used in place of the given parameter, which is typically used to denote multiple
     * values to be supplied to {@code anyOf} / {@code allOf}.
     */
    protected Stream<? extends ISingleOperand1<? extends ISingleOperand2<?>>> substParam(final String param, final boolean ignoreNull) {
        return switch (getParamValue(param)) {
            case List<?> list -> list.stream().map(o -> value(o, ignoreNull));
            case Object o -> Stream.of(value(o, ignoreNull));
            case null -> Stream.of(nullValue(ignoreNull));
        };
    }

    protected static Object preprocessValue(final Object value) {
        // TODO Consider if value processing should be aligned with ValuePreprocessor.convertValue(value).
        if (value != null) {
            if (value.getClass().isArray()) {
                return preprocessValues(Arrays.asList((Object[]) value));
            } else if (value instanceof Collection<?> collection) {
                return preprocessValues(collection);
            }
        }
        return preprocessScalarValue(value);
    }

    protected static List<Object> preprocessValues(final Collection<?> values) {
        return values.stream()
                .map(AbstractEqlVisitor::preprocessValue)
                .mapMulti((val, sink) -> {
                    if (val instanceof List<?> list) {
                        list.forEach(sink);
                    } else {
                        sink.accept(val);
                    }
                })
                .toList();
    }

    /** Ensures that values of boolean types are converted properly. */
    protected static Object preprocessScalarValue(final Object value) {
        if (value instanceof Boolean) {
            return (boolean) value ? HibernateConstants.Y : HibernateConstants.N;
        }
        return value;
    }

    // a generic return type is needed to be able to use this method in switch expressions
    protected static <T> T unexpectedToken(final Token token) throws EqlSyntaxException {
        throw new EqlSyntaxException("Unexpected token: %s".formatted(token.getText()));
    }

}
