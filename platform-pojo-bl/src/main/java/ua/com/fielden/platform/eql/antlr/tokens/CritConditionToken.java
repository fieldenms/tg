package ua.com.fielden.platform.eql.antlr.tokens;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;

import java.util.Optional;

import static java.util.Optional.empty;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.CRITCONDITION;

public final class CritConditionToken extends AbstractParameterisedEqlToken {

    /** Can be null. */
    public final ICompoundCondition0 collectionQueryStart;
    public final String prop;
    public final String critProp;
    public final Optional<Object> defaultValue;

    public CritConditionToken(
            final ICompoundCondition0 collectionQueryStart,
            final String prop, final String critProp,
            final Optional<Object> defaultValue) {
        super(CRITCONDITION, "critCondition");
        this.collectionQueryStart = collectionQueryStart;
        this.prop = prop;
        this.critProp = critProp;
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

}
