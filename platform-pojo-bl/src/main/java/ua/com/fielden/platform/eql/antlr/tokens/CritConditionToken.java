package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;

import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.empty;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.CRITCONDITION;

public final class CritConditionToken extends AbstractParameterisedEqlToken {

    /** Can be null. */
    public final ICompoundCondition0<?> collectionQueryStart;
    public final String prop;
    public final String critProp;
    public final Optional<?> defaultValue;

    public CritConditionToken(
            final ICompoundCondition0<?> collectionQueryStart,
            final String prop, final String critProp,
            final Optional<?> defaultValue) {
        super(CRITCONDITION, "critCondition");
        this.collectionQueryStart = collectionQueryStart;
        this.prop = requireNonNull(prop);
        this.critProp = requireNonNull(critProp);
        this.defaultValue = defaultValue;
    }

    public CritConditionToken(final String prop, final String critProp) {
        this(null, prop, critProp, empty());
    }

    @Override
    public String parametersText() {
        final var sb = new StringBuilder();
        if (collectionQueryStart != null) {
            sb.append("query=");
            sb.append(collectionQueryStart);
            sb.append(", ");
        }
        sb.append("prop=%s, critProp=%s, default=%s".formatted(prop, critProp, defaultValue));
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        return this == o || o instanceof CritConditionToken that &&
                Objects.equals(collectionQueryStart, that.collectionQueryStart) &&
                Objects.equals(prop, that.prop) &&
                Objects.equals(critProp, that.critProp) &&
                Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(collectionQueryStart, prop, critProp, defaultValue);
    }

}
