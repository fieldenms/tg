package ua.com.fielden.platform.eql.antlr.tokens;

import org.antlr.v4.runtime.CommonToken;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.eql.antlr.EQLLexer;

import java.util.Optional;

public final class CritConditionToken extends CommonToken {

    /** Can be null. */
    public final ICompoundCondition0 collectionQueryStart;
    public final String prop;
    public final String critProp;
    public final Optional<Object> defaultValue;

    public CritConditionToken(
            final ICompoundCondition0 collectionQueryStart,
            final String prop, final String critProp,
            final Optional<Object> defaultValue)
    {
        super(EQLLexer.CRITCONDITION, "critCondition");
        this.collectionQueryStart = collectionQueryStart;
        this.prop = prop;
        this.critProp = critProp;
        this.defaultValue = defaultValue;
    }

    public CritConditionToken(final String prop, final String critProp) {
        this(null, prop, critProp, Optional.empty());
    }

}
